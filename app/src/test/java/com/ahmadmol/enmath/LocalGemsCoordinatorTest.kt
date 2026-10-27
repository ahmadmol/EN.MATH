package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

/** Test-only storage. The actual app always constructs SQLiteLocalGems. */
private class TestGemStorage : LocalGemsStorage {
    val accounts=mutableMapOf<String,GemAccount>(); var failWrites=false; var failActivation=false
    var gate:CountDownLatch?=null;val entered=CountDownLatch(1)
    @Synchronized override fun activate(owner:String,excluded:Set<String>,at:Long):GemAccount {
        if(failActivation) error("Injected initial activation failure")
        if(owner=="a@test") {entered.countDown();gate?.await(5,TimeUnit.SECONDS)}
        return accounts.getOrPut(owner) {GemAccount(owner,at,excluded=excluded)}
    }
    @Synchronized override fun read(owner:String)=accounts.getValue(owner)
    @Synchronized override fun change(owner:String,transform:(GemAccount)->GemAccount):GemAccount {
        if(failWrites) error("Injected write failure")
        return transform(read(owner)).also {LocalGemsPolicy.validate(it);accounts[owner]=it}
    }
}
class LocalGemsCoordinatorTest {
    @Test fun profileBalanceFollowsUuidLegacyGuestSwitchAndCoordinatorRestartWithoutWrites() {
        val storage = TestGemStorage()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val fresh = com.ahmadmol.enmath.core.data.LocalLearner(
                "00000000-0000-4000-8000-000000000011", "Local", StudyStage.BAC, false)
            val legacy = fresh.copy(id = "00000000-0000-4000-8000-000000000012",
                legacyWalletKey = "preserved-wallet@example.test")
            val historical = LocalGemsPolicy.award(GemAccount(legacy.legacyWalletKey!!, now),
                GemReward.LESSON, "historical", now)
            storage.accounts[historical.owner] = historical
            fun snapshot(profile: com.ahmadmol.enmath.core.data.LocalLearner) = LearningState(user = profile.user())
            fun display(g: LocalGemsCoordinator, state: LearningState) =
                com.ahmadmol.enmath.features.profile.profileGemBalanceText(g.ui.value, state.user, Language.English)
            val coordinator = LocalGemsCoordinator(storage, scope, { "fixture" }, { now })
            val newState = snapshot(fresh)
            coordinator.bind(newState); await(coordinator) { it.account?.owner == fresh.id }
            assertEquals("0 gems", display(coordinator, newState))
            val legacyState = snapshot(legacy)
            coordinator.bind(legacyState); await(coordinator) { it.account?.owner == historical.owner }
            assertEquals("10 gems", display(coordinator, legacyState))
            val guestState = snapshot(fresh.copy(guest = true))
            coordinator.bind(guestState)
            assertEquals("N/A", display(coordinator, guestState))
            assertNull(coordinator.ui.value.account)
            val restored = LocalGemsCoordinator(storage, scope, { "fixture" }, { now + 1000 })
            restored.bind(legacyState); await(restored) { it.account?.owner == historical.owner }
            assertEquals("10 gems", display(restored, legacyState))
            restored.bind(newState); await(restored) { it.account?.owner == fresh.id }
            assertEquals("0 gems", display(restored, newState))
            assertEquals(historical, storage.read(historical.owner))
            assertEquals(setOf(fresh.id, historical.owner), storage.accounts.keys)
        } finally { scope.cancel() }
    }

    private fun durablePractice(wallet: String? = null): Pair<LearningState, PendingGemEvent> {
        val id = "00000000-0000-4000-8000-000000000001"
        val set = ProductSeed.sets.first()
        val event = PendingGemEvent("fixture-event", id, "PRACTICE", set.id, answers(set.id), now)
        val state = LearningState(user = User("Local", "", AccountType.Student, id, StudyStage.BAC, wallet),
            completed = emptySet(), product = ProductState(learningCommands = setOf(event.id), pendingGemEvents = listOf(event)))
        return state to event
    }
    @Test fun grantCommittedBeforeFailedAcknowledgmentReplaysOnceAfterCoordinatorRestart() {
        val s = TestGemStorage(); val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val (state, event) = durablePractice()
            val owner = LocalGemsPolicy.owner(state.user)!!
            val g = LocalGemsCoordinator(s, scope, { "fixture" }, { now + 1000 })
            g.bind(state); await(g) { it.account != null }
            g.deliver(state, event) { throw LocalDataWriteFailed() }
            await(g) { it.error != null }
            assertEquals(5, s.read(owner).balance)
            assertEquals(1, s.read(owner).rewards.size)
            val restored = LocalGemsCoordinator(s, scope, { "fixture" }, { now + 2000 })
            restored.bind(state); await(restored) { it.account != null }
            var acknowledgments = 0
            val acknowledged = CountDownLatch(1)
            restored.deliver(state, event) { acknowledgments++; acknowledged.countDown() }
            assertTrue(acknowledged.await(5, TimeUnit.SECONDS))
            await(restored) { it.account?.balance == 5 && it.error == null }
            assertEquals(1, acknowledgments)
            assertEquals(5, s.read(owner).balance)
            assertEquals(1, s.read(owner).rewards.size)
        } finally { scope.cancel() }
    }
    @Test fun failedLedgerDoesNotAcknowledgeAndRetainedEventCanRetry() {
        val s = TestGemStorage(); val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val (state, event) = durablePractice(); val owner = LocalGemsPolicy.owner(state.user)!!
            val g = LocalGemsCoordinator(s, scope, { "fixture" }, { now })
            g.bind(state); await(g) { it.account != null }; s.failWrites = true
            var acknowledged = false
            g.deliver(state, event) { acknowledged = true }
            await(g) { it.error != null }
            assertFalse(acknowledged); assertEquals(0, s.read(owner).balance)
            s.failWrites = false
            g.deliver(state, event) { acknowledged = true }
            await(g) { it.account?.balance == 5 }
            assertTrue(acknowledged)
        } finally { scope.cancel() }
    }
    @Test fun uuidProfileUsesLegacyWalletKeyWithoutCreatingAReplacementAccount() {
        val key = "historical-owner@example.test"
        val s = TestGemStorage(); val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val (state, event) = durablePractice(key)
            val oldGrant = GemRewardEntry("WEEKLY:historical", GemReward.WEEKLY, 15, now, LocalGemsPolicy.day(now), LocalGemsPolicy.week(now))
            val old = GemAccount(key, now, 15, rewards = listOf(oldGrant)).also(LocalGemsPolicy::validate)
            s.accounts[key] = old
            val g = LocalGemsCoordinator(s, scope, { "fixture" }, { now })
            g.bind(state); assertEquals(old, await(g) { it.account != null }.account)
            g.deliver(state, event) {}
            await(g) { it.account?.balance == 20 }
            assertEquals(setOf(key), s.accounts.keys)
            assertEquals(oldGrant, s.read(key).rewards.first())
            assertEquals(1, s.read(key).rewards.count { it.kind == GemReward.WEEKLY })
            assertEquals(state.user!!.localId, event.owner)
        } finally { scope.cancel() }
    }
    @Test fun guestAndForeignUuidEventsCannotReceiveOrAcknowledgeCurrency() {
        val s = TestGemStorage(); val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val (state, event) = durablePractice()
            val g = LocalGemsCoordinator(s, scope, { "fixture" }, { now })
            val guest = state.copy(user = state.user!!.copy(type = AccountType.Guest))
            g.bind(guest); g.deliver(guest, event) { fail("Guest must not acknowledge a currency grant") }
            assertTrue(s.accounts.isEmpty())
            g.bind(state); await(g) { it.account != null }
            g.deliver(state, event.copy(owner = "00000000-0000-4000-8000-000000000002")) { fail("Foreign event") }
            assertEquals(0, s.read(LocalGemsPolicy.owner(state.user)!!).balance)
        } finally { scope.cancel() }
    }
    @Test fun legacyWalletReloadKeepsOwnerLedgerBalanceAndPurchasedContentWithoutDisplayBonus() {
        val owner = "legacy-owner@example.test"
        val s = TestGemStorage()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val item = LocalGemCatalog.items.first()
            val earned = (1..4).fold(GemAccount(owner, now)) { acc, i ->
                LocalGemsPolicy.award(acc, GemReward.LESSON, "historical-$i", now)
            }
            val historical = LocalGemsPolicy.unlock(earned, item, now).copy(
                excluded = setOf("LESSON:legacy-baseline"), lessons = setOf("historical-1"),
                assessments = listOf(GemAssessment("historical-attempt", "limits", false, 1f)),
            ).also(LocalGemsPolicy::validate)
            s.accounts[owner] = historical
            val g = LocalGemsCoordinator(s, scope, { "owned fixture content" }, { now + 1000 })
            val current = state(owner)
            g.bind(current)
            assertEquals(historical, await(g) { it.account != null }.account)
            assertEquals(0, g.ui.value.account!!.balance)
            g.openContent(current, owner, item.id)
            assertEquals("owned fixture content", await(g) { it.content != null }.content)
            g.purchase(current, owner, item.id)
            await(g) { !it.busy }
            assertEquals(historical, s.read(owner))
            val restored = LocalGemsCoordinator(s, scope, { "owned fixture content" }, { now + 2000 })
            restored.bind(current.copy(user = User("Renamed", " LEGACY-OWNER@EXAMPLE.TEST ", AccountType.Student)))
            assertEquals(historical, await(restored) { it.account != null }.account)
            restored.openContent(current, owner, item.id)
            assertEquals("owned fixture content", await(restored) { it.content != null }.content)
            assertEquals(setOf(owner), s.accounts.keys)
            assertEquals(historical, s.read(owner))
        } finally { scope.cancel() }
    }
    @Test fun purchaseAlwaysUsesEarnedLedgerAcrossNormalAndSpecialBoundaryMatrix() {
        val base = 1_792_368_000_000L
        for (owner in listOf("normal@example.test", "legacy-owner@example.test")) {
            for (balance in listOf(0, 39, 40, 119, 120)) {
                for (item in LocalGemCatalog.items) {
                    val s = TestGemStorage()
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
                    try {
                        val state = state(owner)
                        val g = LocalGemsCoordinator(s, scope, { "fixture" }, { base })
                        g.bind(state); await(g) { it.account != null }
                        val rewards = (0 until (balance + 9) / 10).map { index ->
                            val at = base + (index / 4) * 86_400_000L
                            GemRewardEntry("LESSON:fixture-$index", GemReward.LESSON,
                                minOf(10, balance - index * 10), at, LocalGemsPolicy.day(at), LocalGemsPolicy.week(at))
                        }
                        s.accounts[owner] = s.read(owner).copy(balance = balance, rewards = rewards)
                            .also(LocalGemsPolicy::validate)
                        val before = s.read(owner)
                        g.purchase(state, owner, item.id)
                        val ui = await(g) { it.error != null || it.account?.unlocks?.isNotEmpty() == true }
                        if (balance < item.price) {
                            assertEquals("INSUFFICIENT_GEMS", ui.error)
                            assertEquals(before, s.read(owner))
                        } else {
                            val after = s.read(owner)
                            assertEquals(balance - item.price, after.balance)
                            assertEquals(item.price, after.unlocks.single().price)
                            assertEquals(before.rewards, after.rewards)
                            g.purchase(state, owner, item.id)
                            await(g) { !it.busy }
                            assertEquals(after, s.read(owner))
                        }
                        LocalGemsPolicy.validate(s.read(owner))
                    } finally { scope.cancel() }
                }
            }
        }
    }
    private val now=1_792_483_200_000L
    private fun state(owner:String)=LearningState(user=User(owner,owner,AccountType.Student),completed=emptySet(),product=ProductState())
    private fun await(g:LocalGemsCoordinator,p:(LocalGemsUiState)->Boolean)=runBlocking {withTimeout(7000) {g.ui.first(p)}}
    private fun answers(id:String)=ProductSeed.sets.first {it.id==id}.questionIds.associateWith {ProductSeed.question(it)!!.answer}
    @Test fun queuedOldOwnerEventCannotLeakIntoNewOwnerUi() {
        val s=TestGemStorage();val gate=CountDownLatch(1);s.gate=gate;val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val g=LocalGemsCoordinator(s,scope,{"body"},{now});val a=state("a@test");val b=state("b@test");val id=ProductSeed.sets.first().id
            g.bind(a);assertTrue(s.entered.await(5,TimeUnit.SECONDS));g.practice(a,id,answers(id));g.bind(b)
            assertEquals("b@test",g.ui.value.owner);assertNull(g.ui.value.account);gate.countDown()
            assertEquals(0,await(g) {it.owner=="b@test" && it.account!=null}.account!!.balance)
            assertEquals(5,s.read("a@test").balance)
            g.practice(a,id,answers(id));g.refresh(b);await(g) {it.owner=="b@test" && it.account!=null};assertEquals(0,s.read("b@test").balance)
        } finally {gate.countDown();scope.cancel()}
    }
    @Test fun coordinatorRestartAndEmailCasePreserveTheSameWallet() {
        val s=TestGemStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val a=state("a@test");val g=LocalGemsCoordinator(s,scope,{"body"},{now});g.bind(a);await(g) {it.account!=null}
            val id=ProductSeed.sets.first().id;g.practice(a,id,answers(id));await(g) {it.account?.balance==5}
            val restored=LocalGemsCoordinator(s,scope,{"body"},{now+1000});restored.bind(a.copy(user=User("Renamed"," A@TEST ",AccountType.Student)))
            assertEquals(5,await(restored) {it.account!=null}.account!!.balance)
        } finally {scope.cancel()}
    }
    @Test fun failedSaveCannotPublishCurrencyOrSeedBalance() {
        val s=TestGemStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val a=state("a@test");val g=LocalGemsCoordinator(s,scope,{"body"},{now});g.bind(a);await(g) {it.account!=null};s.failWrites=true
            val id=ProductSeed.sets.first().id;g.practice(a,id,answers(id));assertEquals(0,await(g) {it.error!=null}.account!!.balance)
            assertTrue(s.read("a@test").rewards.isEmpty())
        } finally {scope.cancel()}
    }
    @Test fun duplicateSpendAndSwitchKeepContentPrivate() {
        val s=TestGemStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val a=state("a@test");val b=state("b@test");val g=LocalGemsCoordinator(s,scope,{"owner body"},{now});g.bind(a);await(g) {it.account!=null}
            s.change("a@test") {account -> (1..4).fold(account) {v,i->LocalGemsPolicy.award(v,GemReward.LESSON,"l$i",now)}}
            val item=LocalGemCatalog.items.first();g.purchase(a,"a@test",item.id);g.purchase(a,"a@test",item.id);await(g) {it.account?.unlocks?.size==1}
            g.openContent(a,"a@test",item.id);assertEquals("owner body",await(g) {it.content!=null}.content);assertEquals(0,s.read("a@test").balance)
            g.bind(b);assertNull(g.ui.value.content);await(g) {it.owner=="b@test" && it.account!=null}
            g.purchase(b,"a@test",item.id);g.openContent(b,"a@test",item.id);assertNull(g.ui.value.content);assertTrue(s.read("b@test").unlocks.isEmpty())
        } finally {scope.cancel()}
    }
    @Test fun failedInitialActivationCanRetryWithoutTouchingLearningData() {
        val s=TestGemStorage();s.failActivation=true;val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val a=state("a@test");val g=LocalGemsCoordinator(s,scope,{"body"},{now})
            g.bind(a);assertNull(await(g) {it.error!=null}.account)
            s.failActivation=false;g.refresh(a)
            assertEquals(0,await(g) {it.account!=null && it.error==null}.account!!.balance)
            assertTrue(a.completed.isEmpty())
        } finally {scope.cancel()}
    }
    @Test fun teacherAndGuestHaveNoGemsIdentity() {
        val s=TestGemStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val g=LocalGemsCoordinator(s,scope,{"body"},{now});val a=state("a@test")
            g.bind(a.copy(user=a.user!!.copy(type=AccountType.Teacher)));assertNull(g.ui.value.owner)
            g.bind(a.copy(user=a.user!!.copy(type=AccountType.Guest)));assertNull(g.ui.value.owner);assertTrue(s.accounts.isEmpty())
        } finally {scope.cancel()}
    }
}
