package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest
import java.util.UUID

class W1CompletionIdentityTest {
    private class Stores {
        val files = mutableMapOf<String, FixturePreferences>()
        var crashOnSnapshot = false
        val preferences = { name: String ->
            files.getOrPut(name) { FixturePreferences() }.also { f ->
                if (crashOnSnapshot && name.startsWith("enmath_learner_")) f.afterCommit = {
                    crashOnSnapshot = false; throw IllegalStateException("fixture process death after copy")
                }
            }.prefs
        }
        fun repo() = LocalDemoRepository(false, preferences)
        fun store() = LocalLearnerStore(preferences)
        fun restart() {
            files.replaceAll { _, old -> FixturePreferences(old.file) }
            crashOnSnapshot = false
        }
        fun legacy(): String {
            val owner = "student:legacy-owner@example.test"
            val hash = MessageDigest.getInstance("SHA-256").digest(owner.toByteArray())
                .joinToString("") { "%02x".format(it) }
            val name = "enmath_personal_$hash"
            val p = preferences(name)
            ProductCodec.save(p, ProductState(savedLessons = setOf("limits-practice"),
                searches = listOf("legacy search"), attempts = listOf(Attempt("legacy-set", mapOf("q" to "answer"), 1, 1, 10, 1000))))
            p.edit().putString("record_owner", owner).putStringSet("completed", setOf("limits-practice"))
                .putString("last", "limits-practice").commit()
            preferences("enmath_demo").edit().putStringSet("student_snapshot_files", setOf(name)).commit()
            return name
        }
    }
    @Test fun uuidStageAndPersonalDataSurviveRestartAndOtherStageCannotReadThem() {
        val s = Stores(); var repo = s.repo()
        val bac = repo.createLocalProfile("Same name", StudyStage.BAC, false)
        UUID.fromString(bac.id)
        val app = AppViewModel(repo)
        assertTrue(app.complete("limits-practice", LearningRules.sessionOwner(repo.state.value)))
        assertTrue(app.bookmark("limits-practice", startedBy = LearningRules.sessionOwner(repo.state.value)))
        val g9 = repo.createLocalProfile("Same name", StudyStage.G9, false)
        assertNotEquals(bac.id, g9.id)
        assertTrue(repo.state.value.completed.isEmpty()); assertTrue(repo.state.value.product.savedLessons.isEmpty())
        assertTrue(repo.state.value.books.isEmpty()); assertTrue(repo.state.value.practiceSets.isEmpty())
        assertFalse(app.complete("limits-practice", LearningRules.sessionOwner(repo.state.value)))
        assertFalse(app.recordPractice(ProductSeed.sets.first().id, emptyMap(), 10, LearningRules.sessionOwner(repo.state.value), "wrong-stage"))
        assertNotEquals(LocalGemsPolicy.owner(bac.user()), LocalGemsPolicy.owner(g9.user()))
        s.restart(); repo = s.repo()
        assertEquals(g9.id, repo.state.value.user?.localId)
        repo.selectLocalProfile(bac.id)
        assertEquals(StudyStage.BAC, repo.state.value.user?.studyStage)
        assertEquals(setOf("limits-practice"), repo.state.value.completed)
        assertEquals(setOf("limits-practice"), repo.state.value.product.savedLessons)
        assertEquals(bac.id, repo.state.value.activeStudentId)
    }
    @Test fun guestScopesArePerStageStableAndNeverHaveWalletOrPendingRewards() {
        val s = Stores(); var repo = s.repo()
        val bac = repo.createLocalProfile("Guest", StudyStage.BAC, true)
        AppViewModel(repo).complete("limits-practice", LearningRules.sessionOwner(repo.state.value))
        val g9 = repo.createLocalProfile("Guest", StudyStage.G9, true)
        assertNotEquals(bac.id, g9.id); assertTrue(repo.state.value.completed.isEmpty())
        assertNull(LocalGemsPolicy.owner(g9.user()))
        val again = repo.createLocalProfile("Different label", StudyStage.BAC, true)
        assertEquals(bac, again); assertEquals(setOf("limits-practice"), repo.state.value.completed)
        assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
        s.restart(); repo = s.repo()
        assertEquals(bac.id, repo.state.value.user?.localId)
        assertNull(LocalGemsPolicy.owner(repo.state.value.user))
    }
    @Test fun localStageRoleAndUuidCannotBeChangedThroughSignInCopy() {
        val s = Stores(); val repo = s.repo(); val p = repo.createLocalProfile("Student", StudyStage.G9, false)
        val before = repo.state.value
        assertThrows(IllegalArgumentException::class.java) { repo.signIn(p.user().copy(studyStage = StudyStage.BAC)) }
        assertThrows(IllegalArgumentException::class.java) { repo.signIn(p.user().copy(type = AccountType.Guest)) }
        assertEquals(before, repo.state.value)
    }
    @Test fun oldSessionCannotWritePracticeCompletionBookmarkOrHistoryIntoSelectedProfile() {
        val s = Stores(); val repo = s.repo(); val a = repo.createLocalProfile("A", StudyStage.BAC, false)
        val app = AppViewModel(repo)
        repo.createLocalProfile("B", StudyStage.BAC, false)
        val before = repo.state.value
        assertFalse(app.complete("limits-practice", a.id))
        assertFalse(app.recordPractice(ProductSeed.sets.first().id, emptyMap(), 10, a.id))
        assertFalse(app.bookmark("limits-practice", startedBy = a.id))
        assertEquals("", app.history("old input", "limits", a.id))
        assertEquals(before, repo.state.value)
    }
    @Test fun practiceAndItsRewardReceiptAreOneAcknowledgedSnapshotAndReplayIsIdempotent() {
        val s = Stores(); var repo = s.repo(); val p = repo.createLocalProfile("A", StudyStage.BAC, false)
        val set = ProductSeed.sets.first(); val answers = set.questionIds.associateWith { ProductSeed.question(it)!!.answer }
        assertTrue(AppViewModel(repo).recordPractice(set.id, answers, 60, LearningRules.sessionOwner(repo.state.value), "durable-command"))
        val saved = repo.state.value
        assertEquals(1, saved.product.attempts.size); assertEquals(1, saved.product.pendingGemEvents.size)
        assertEquals(p.id, saved.product.pendingGemEvents.single().owner)
        s.restart(); repo = s.repo()
        assertTrue(AppViewModel(repo).recordPractice(set.id, answers, 60, LearningRules.sessionOwner(repo.state.value), "durable-command"))
        assertEquals(saved.product, repo.state.value.product)
    }
    @Test fun failedSnapshotCannotPublishSuccessAndRestartAllowsSameCommandToRetry() {
        val s = Stores(); var repo = s.repo(); val p = repo.createLocalProfile("A", StudyStage.BAC, false)
        val f = s.files.getValue("enmath_learner_${p.id}"); val raw = f.file.readBytes()
        val before = repo.state.value
        f.fail = true
        val set = ProductSeed.sets.first()
        assertFalse(AppViewModel(repo).recordPractice(set.id, emptyMap(), 10, LearningRules.sessionOwner(repo.state.value), "retry-command"))
        assertTrue(repo.state.value.product.attempts.isEmpty())
        assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
        assertEquals(before.completed, repo.state.value.completed)
        assertArrayEquals(raw, f.file.readBytes())
        s.restart(); repo = s.repo()
        assertTrue(AppViewModel(repo).recordPractice(set.id, emptyMap(), 10, LearningRules.sessionOwner(repo.state.value), "retry-command"))
        assertEquals(1, repo.state.value.product.attempts.size)
    }
    @Test fun confirmingScopedLegacyRequiresExplicitStageAndKeepsOriginalWalletKeyAndBytes() {
        val s = Stores(); val source = s.legacy(); val original = s.files.getValue(source).file.readBytes()
        val repo = s.repo(); assertTrue(repo.state.value.localProfiles.isEmpty())
        assertTrue(repo.state.value.legacyCandidates.any { it.source == source && it.canConfirm })
        assertThrows(IllegalArgumentException::class.java) { repo.importConfirmedLegacy(source, StudyStage.BAC, "Unreviewed") }
        // Simulate a profile imported by the older release; retain its UUID/wallet and all raw history.
        val p = s.store().migrate(source, StudyStage.BAC, "Imported")
        repo.selectLocalProfile(p.id)
        assertEquals("legacy-owner@example.test", LocalGemsPolicy.owner(p.user()))
        assertNotEquals(p.id, LocalGemsPolicy.owner(p.user()))
        assertEquals(p.id, LearningRules.commandOwner(repo.state.value))
        assertEquals(setOf("limits-practice"), repo.state.value.completed)
        assertEquals(1, repo.state.value.product.attempts.size)
        assertTrue(repo.state.value.reviewRequired)
        assertTrue(StudentCatalog.attempts(repo.state.value).isEmpty())
        assertTrue(StudentCatalog.completed(repo.state.value).isEmpty())
        assertEquals(setOf("limits-practice"), repo.state.value.product.savedLessons)
        assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
        assertArrayEquals(original, s.files.getValue(source).file.readBytes())
        assertEquals(p, repo.importConfirmedLegacy(source, StudyStage.BAC, "No duplicate"))
        assertThrows(IllegalArgumentException::class.java) { repo.importConfirmedLegacy(source, StudyStage.G9, "Wrong stage") }
        s.restart(); val restored = s.repo()
        assertEquals(p.user(), restored.state.value.user)
        assertEquals("legacy-owner@example.test", LocalGemsPolicy.owner(restored.state.value.user))
        assertArrayEquals(original, s.files.getValue(source).file.readBytes())
    }
    @Test fun missingOrConflictingOwnerMarkersStayUnclaimedAndSourceIsUnchanged() {
        val s = Stores(); val source = s.legacy()
        s.preferences(source).edit().putString("record_owner", "student:someone-else@example.test").commit()
        val original = s.files.getValue(source).file.readBytes()
        val repo = s.repo()
        assertFalse(repo.state.value.legacyCandidates.first { it.source == source }.canConfirm)
        assertThrows(IllegalArgumentException::class.java) { repo.importConfirmedLegacy(source, StudyStage.BAC, "Unproven") }
        assertTrue(repo.state.value.localProfiles.isEmpty())
        assertArrayEquals(original, s.files.getValue(source).file.readBytes())
        repo.createLocalProfile("Safe independent file", StudyStage.G9, true)
        assertTrue(repo.state.value.completed.isEmpty())
    }
    @Test fun migrationResumesAfterPreparedJournalWithoutClaimingTwice() {
        val s = Stores(); val source = s.legacy(); val original = s.files.getValue(source).file.readBytes()
        s.preferences("enmath_local_identities_v1")
        s.files.getValue("enmath_local_identities_v1").afterCommit = { throw IllegalStateException("fixture crash after prepare") }
        assertThrows(IllegalStateException::class.java) { s.store().migrate(source, StudyStage.G9, "Explicit G9") }
        s.restart(); val store = s.store(); store.resumeMigrations(); store.resumeMigrations()
        val p = store.profiles().single()
        assertEquals(StudyStage.G9, p.stage); assertEquals(source, p.legacySource)
        assertEquals(1, store.load(p.id).product.attempts.size)
        assertArrayEquals(original, s.files.getValue(source).file.readBytes())
    }
    @Test fun migrationResumesAfterCopyBeforeRegistryPublicationWithoutReset() {
        val s = Stores(); val source = s.legacy(); val original = s.files.getValue(source).file.readBytes()
        s.crashOnSnapshot = true
        assertThrows(IllegalStateException::class.java) { s.store().migrate(source, StudyStage.BAC, "Legacy") }
        s.restart(); val store = s.store(); store.resumeMigrations(); store.resumeMigrations()
        val p = store.profiles().single()
        assertEquals(setOf("limits-practice"), store.load(p.id).completed)
        assertEquals(1, s.files.keys.count { it.startsWith("enmath_learner_") })
        assertArrayEquals(original, s.files.getValue(source).file.readBytes())
    }
    @Test fun changedSourceSuspendsPreparedMigrationAndDoesNotClaimOwner() {
        val s = Stores(); val source = s.legacy(); s.preferences("enmath_local_identities_v1")
        s.files.getValue("enmath_local_identities_v1").afterCommit = { throw IllegalStateException("fixture crash") }
        assertThrows(IllegalStateException::class.java) { s.store().migrate(source, StudyStage.BAC, "Legacy") }
        s.preferences(source).edit().putStringSet("completed", setOf("changed-source")).commit()
        val original = s.files.getValue(source).file.readBytes()
        s.restart(); val store = s.store(); store.resumeMigrations()
        assertTrue(store.profiles().isEmpty())
        assertArrayEquals(original, s.files.getValue(source).file.readBytes())
        assertTrue(s.files.getValue("enmath_local_identities_v1").prefs.getString("registry", "")!!.contains("SUSPENDED"))
    }
    @Test fun failedJournalCommitCannotActivateOrConsumeLegacySource() {
        val s = Stores(); val source = s.legacy(); s.preferences("enmath_local_identities_v1")
        s.files.getValue("enmath_local_identities_v1").fail = true
        assertThrows(LocalDataWriteFailed::class.java) { s.store().migrate(source, StudyStage.BAC, "Legacy") }
        s.restart(); val store = s.store()
        assertTrue(store.profiles().isEmpty())
        assertEquals(source, store.migrate(source, StudyStage.BAC, "Retry").legacySource)
        assertEquals(1, store.profiles().size)
    }
    @Test fun corruptLegacyDoesNotBlockCreatingAnIndependentFileOrOverwriteOriginal() {
        val s = Stores(); val old = s.preferences("enmath_demo")
        old.edit().putString("product_v1", "{corrupt").putStringSet("completed", setOf("unowned")).commit()
        val repo = s.repo(); assertEquals("LEGACY_CORRUPT_PRESERVED", repo.state.value.storageIssue)
        repo.createLocalProfile("Independent", StudyStage.BAC, false)
        assertTrue(AppViewModel(repo).complete("limits-practice"))
        assertEquals("{corrupt", old.getString("product_v1", null))
        assertEquals(setOf("unowned"), old.getStringSet("completed", null))
    }
    @Test fun corruptProfileIsPreservedAndDoesNotPreventOpeningAnotherProfile() {
        val s = Stores(); val repo = s.repo(); val first = repo.createLocalProfile("A", StudyStage.BAC, false)
        val second = repo.createLocalProfile("B", StudyStage.G9, false)
        s.preferences("enmath_learner_${second.id}").edit().putString("snapshot", "{broken").commit()
        s.restart(); val restored = s.repo()
        assertNull(restored.state.value.user)
        assertEquals("CORRUPT_PROFILE_PRESERVED", restored.state.value.storageIssue)
        restored.selectLocalProfile(first.id)
        assertEquals(first.id, restored.state.value.user?.localId)
        assertEquals("{broken", s.preferences("enmath_learner_${second.id}").getString("snapshot", null))
    }
    @Test fun copiedSnapshotCannotBeReadAsAnotherOwnerEvenWithoutPendingEvents() {
        val s = Stores(); val repo = s.repo()
        val a = repo.createLocalProfile("A", StudyStage.BAC, false)
        repo.recordScopedLearning(StudentCatalog.scope(repo.state.value)!!, StudentCatalog.ref(repo.state.value, "limits-practice"), "fixture-complete") { it }
        val b = repo.createLocalProfile("B", StudyStage.BAC, false)
        val raw = s.preferences("enmath_learner_${a.id}").getString("snapshot", null)
        s.preferences("enmath_learner_${b.id}").edit().putString("snapshot", raw).commit()
        assertThrows(CorruptLocalData::class.java) { s.store().load(b.id) }
        assertEquals(raw, s.preferences("enmath_learner_${b.id}").getString("snapshot", null))
    }
    @Test fun verifiedCompletionQualifiesInEitherOrderAndRetryKeepsOneDurableEvidenceEvent() {
        for (verifyFirst in listOf(true, false)) {
            val s = Stores(); val repo = s.repo(); val p = repo.createLocalProfile("A", StudyStage.BAC, false)
            val lesson = repo.state.value.lessons.first { it.checkpoint.isNotBlank() }
            val app = AppViewModel(repo)
            if (verifyFirst) app.verifyLesson(lesson.id, lesson.checkpointAnswer, LearningRules.sessionOwner(repo.state.value))
            assertTrue(app.complete(lesson.id, LearningRules.sessionOwner(repo.state.value)))
            if (!verifyFirst) app.verifyLesson(lesson.id, lesson.checkpointAnswer, LearningRules.sessionOwner(repo.state.value))
            val product = repo.state.value.product
            assertEquals(1, product.pendingGemEvents.count { it.answers["checkpoint"] == lesson.checkpointAnswer })
            app.verifyLesson(lesson.id, lesson.checkpointAnswer, LearningRules.sessionOwner(repo.state.value))
            assertTrue(app.complete(lesson.id, LearningRules.sessionOwner(repo.state.value)))
            assertEquals(product, repo.state.value.product)
            s.restart()
            assertEquals(product, s.repo().state.value.product)
        }
    }
    @Test fun confirmingOrReopeningImportedCompletionCannotCreateRetroactiveGrants() {
        val s = Stores(); val source = s.legacy(); val repo = s.repo()
        val p = s.store().migrate(source, StudyStage.BAC, "Confirmed")
        repo.selectLocalProfile(p.id)
        val app = AppViewModel(repo); val lesson = repo.state.value.lesson("limits-practice")!!
        assertTrue(StudentCatalog.completed(repo.state.value).isEmpty())
        assertTrue(app.complete(lesson.id, LearningRules.sessionOwner(repo.state.value)))
        assertEquals(setOf(lesson.id), StudentCatalog.completed(repo.state.value))
        app.verifyLesson(lesson.id, lesson.checkpointAnswer, LearningRules.sessionOwner(repo.state.value))
        assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun assignmentCommandAndPendingRewardSurviveRestartWithoutDuplicatingSubmission() {
        val s = Stores(); var repo = s.repo(); val p = repo.createLocalProfile("A", StudyStage.BAC, false)
        val q = ProductSeed.questions.first()
        val assignment = Assignment("fixture-assignment", Copy("Fixture", "اختبار"), Copy("Local", "محلي"),
            "fixture-class", listOf(q.id), Long.MAX_VALUE, attempts = 2)
        repo.changeScoped(StudentCatalog.scope(repo.state.value)!!) { it.copy(assignments = listOf(assignment)) }
        val result = AppViewModel(repo).submitAssignment(assignment.id, mapOf(q.id to q.answer), 10, LearningRules.sessionOwner(repo.state.value), "submission-command")
        assertTrue(result is com.ahmadmol.enmath.core.AssignmentSubmissionResult.Submitted)
        val saved = repo.state.value.product
        assertEquals("ASSIGNMENT", saved.pendingGemEvents.single().kind)
        s.restart(); repo = s.repo()
        assertEquals(com.ahmadmol.enmath.core.AssignmentSubmissionResult.CommandConflict, AppViewModel(repo).submitAssignment(assignment.id, mapOf(q.id to "different retry"), 10, LearningRules.sessionOwner(repo.state.value), "submission-command"))
        assertEquals(saved, repo.state.value.product)
    }
    @Test fun globalDisplayPreferencesPersistWithoutRewritingLegacyProductOrLocalProgress() {
        val s = Stores(); val repo = s.repo(); val p = repo.createLocalProfile("A", StudyStage.BAC, false)
        val global = s.preferences("enmath_demo").getString("product_v1", null)
        repo.recordScopedLearning(StudentCatalog.scope(repo.state.value)!!, StudentCatalog.ref(repo.state.value, "limits-practice"), "fixture-complete") { it }
        repo.setLanguage(Language.English); repo.setTheme(ThemeMode.Dark)
        s.restart(); val restored = s.repo().state.value
        assertEquals(p.id, restored.user?.localId); assertEquals(ThemeMode.Dark, restored.theme)
        assertEquals(Language.English, restored.language); assertEquals(setOf("limits-practice"), restored.completed)
        assertEquals(global, s.preferences("enmath_demo").getString("product_v1", null))
    }
}
