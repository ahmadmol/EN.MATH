package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

class Execution2PersistenceTest {
    private class Stores {
        val files = mutableMapOf<String, FixturePreferences>()
        fun pref(name: String) = files.getOrPut(name) { FixturePreferences() }.prefs
        fun repo() = LocalDemoRepository(false, ::pref)
        fun restart() { files.replaceAll { _, f -> FixturePreferences(f.file) } }
    }
    @Test fun noticeIsPerStageDurableAndDoesNotCreateOrActivateAProfile() {
        val s = Stores(); val r = s.repo()
        r.acknowledgeGuestNotice(StudyStage.BAC)
        assertTrue(r.state.value.localProfiles.isEmpty()); assertNull(r.state.value.user)
        s.restart(); val restored = s.repo()
        assertEquals(setOf(StudyStage.BAC), restored.state.value.acknowledgedGuestStages)
        restored.acknowledgeGuestNotice(StudyStage.G9)
        assertEquals(StudyStage.entries.toSet(), restored.state.value.acknowledgedGuestStages)
    }
    @Test fun failedNoticeCommitDoesNotPublishOrCreateAGuest() {
        val s = Stores(); val r = s.repo()
        s.files.getValue("enmath_local_identities_v1").fail = true
        assertThrows(LocalDataWriteFailed::class.java) { r.acknowledgeGuestNotice(StudyStage.G9) }
        assertTrue(r.state.value.acknowledgedGuestStages.isEmpty()); assertNull(r.state.value.user)
        s.restart(); assertTrue(s.repo().state.value.acknowledgedGuestStages.isEmpty())
    }
    @Test fun commandReceiptsSurviveRestartAndRejectChangedPayloadAtRepositoryBoundary() {
        val s = Stores(); val r = s.repo(); r.createLocalProfile("A", StudyStage.BAC, true)
        val scope = StudentCatalog.scope(r.state.value)!!
        assertTrue(r.recordCommand(scope, null, "request", "bookmark:limits-practice") { it.copy(savedLessons = setOf("limits-practice")) })
        s.restart(); val next = s.repo(); val active = StudentCatalog.scope(next.state.value)!!
        assertTrue(next.recordCommand(active, null, "request", "bookmark:limits-practice") { error("replay must not execute") })
        assertFalse(next.recordCommand(active, null, "request", "different") { error("conflict must not execute") })
        assertEquals(setOf("limits-practice"), next.state.value.product.savedLessons)
        assertFalse(next.recordCommand(scope, null, "late", "late") { it })
    }
    @Test fun contentRevisionAndSourceMismatchRejectReadAndWriteWithoutMutation() {
        val r = Stores().repo(); r.createLocalProfile("A", StudyStage.BAC, true)
        val scope = StudentCatalog.scope(r.state.value)!!; val ref = StudentCatalog.ref(r.state.value,"limits-practice")!!
        val before = r.state.value
        for (invalid in listOf(ref.copy(revision=99), ref.copy(sourceId="unverified"))) {
            assertNull(r.lesson(scope,invalid)); assertFalse(r.recordCommand(scope,invalid,"bad","bad") { it })
        }
        assertEquals(before,r.state.value)
    }
    @Test fun failedCommandSnapshotHasNoReceiptAndSafelyRetriesAfterRestart() {
        val s = Stores(); val r = s.repo(); val p = r.createLocalProfile("A", StudyStage.BAC, true)
        val scope = StudentCatalog.scope(r.state.value)!!
        s.files.getValue("enmath_learner_${p.id}").fail=true
        assertThrows(LocalDataWriteFailed::class.java) { r.recordCommand(scope,null,"id","payload") { it } }
        assertTrue(r.state.value.product.commandReceipts.isEmpty())
        s.restart(); val restored=s.repo()
        assertTrue(restored.recordCommand(StudentCatalog.scope(restored.state.value)!!,null,"id","payload") { it })
    }
    @Test fun unscopedLocalHistoryIsPreservedWithoutInventingEditionEvidence() {
        val s=Stores();val r=s.repo();val p=r.createLocalProfile("A",StudyStage.BAC,false)
        val store=LocalLearnerStore(s::pref);val snapshot=store.load(p.id)
        val old=Attempt(r.state.value.practiceSets.first().id,emptyMap(),0,6,20,100)
        store.save(p.id,snapshot.copy(completed=setOf("limits-practice"),product=snapshot.product.copy(attempts=listOf(old))))
        r.selectLocalProfile(p.id)
        assertEquals(setOf("limits-practice"),r.state.value.completed)
        assertEquals(listOf(old),r.state.value.product.attempts)
        assertTrue(StudentCatalog.completed(r.state.value).isEmpty());assertTrue(StudentCatalog.attempts(r.state.value).isEmpty())
        assertTrue(StudentCatalog.hasUnverifiedHistory(r.state.value))
        assertTrue(com.ahmadmol.enmath.core.AppViewModel(r).complete("limits-practice"))
        assertEquals(setOf("limits-practice"),StudentCatalog.completed(r.state.value))
        assertTrue(r.state.value.product.pendingGemEvents.isEmpty());assertEquals(listOf(old),r.state.value.product.attempts)
    }
    @Test fun plannerCommitSignalAndHistoricalMeasuredProgressStayAccurate() {
        val r=Stores().repo();r.createLocalProfile("A",StudyStage.BAC,true)
        val scope=StudentCatalog.scope(r.state.value)!!
        val task=PlanTask("goal","Read","Mathematics",PlanPeriod.WEEK,
            StudyPeriods.start(System.currentTimeMillis(),PlanPeriod.WEEK),PlanMode.ACHIEVEMENT,1,
            lesson=StudentCatalog.ref(r.state.value,"limits-practice"))
        assertEquals(PlanWrite.SAVED,r.planner.save(scope,0,"add",task));assertEquals(1L,r.planner.changes.value)
        assertTrue(com.ahmadmol.enmath.core.AppViewModel(r).complete("limits-practice"))
        assertEquals(PlanWrite.SAVED,r.planner.remove(scope,1,"remove",task.id));assertEquals(2L,r.planner.changes.value)
        val prior=(r.planner.read(scope) as PlanRead.Ready).plan.history.last().tasks.single()
        assertEquals(1,prior.done);assertEquals("LEARNING_EVENT",prior.source)
    }
}
