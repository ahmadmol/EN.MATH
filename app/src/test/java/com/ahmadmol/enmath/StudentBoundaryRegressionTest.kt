package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

/** Repository boundary regressions, independent of Compose. */
class StudentBoundaryRegressionTest {
    private fun repo(): LocalDemoRepository {
        val stores = mutableMapOf<String, FixturePreferences>()
        return LocalDemoRepository(false) { stores.getOrPut(it) { FixturePreferences() }.prefs }
    }
    @Test fun directLessonWritesCannotCrossStage() {
        val r = repo(); r.createLocalProfile("G9", StudyStage.G9, false)
        val before = r.state.value
        r.openLesson("limits-practice"); r.completeLesson("limits-practice")
        assertEquals(before, r.state.value)
    }
    @Test fun directLearningTransformCannotInjectOtherStageAttemptOrReward() {
        val r = repo(); val p = r.createLocalProfile("G9", StudyStage.G9, false)
        assertFalse(r.recordLearning(p.id, "injection") { it.copy(attempts = listOf(
            Attempt(ProductSeed.sets.first().id, emptyMap(), 0, 1, 1, 1))) })
        assertTrue(r.state.value.product.attempts.isEmpty())
    }
    @Test fun anonymousCatalogDoesNotExposeBac() {
        val r = repo()
        assertTrue(r.state.value.books.isEmpty())
        assertTrue(r.state.value.practiceSets.isEmpty())
    }
    @Test fun editionMismatchAndOldGenerationCannotReadOrWriteLessons() {
        val r = repo(); val a = r.createLocalProfile("A", StudyStage.BAC, false)
        val scope = StudentCatalog.scope(r.state.value)!!
        val ref = StudentCatalog.ref(r.state.value, "limits-practice")!!
        assertNotNull(r.lesson(scope, ref))
        assertNull(r.lesson(scope, ref.copy(edition = "other")))
        assertFalse(r.recordScopedLearning(scope, ref.copy(stage = StudyStage.G9), "bad") { it })
        r.createLocalProfile("B", StudyStage.BAC, true); r.selectLocalProfile(a.id)
        assertNull(r.lesson(scope, ref))
        assertFalse(r.recordScopedLearning(scope, ref, "stale") { it })
    }
    @Test fun delayedUiTicketCannotWriteAfterAtoBtoA() {
        val r = repo(); val a = r.createLocalProfile("A", StudyStage.BAC, false)
        val ticket = LearningRules.sessionOwner(r.state.value)
        r.createLocalProfile("B", StudyStage.BAC, true); r.selectLocalProfile(a.id)
        assertFalse(r.recordLearning(ticket, "late", "limits-practice") { it })
        assertTrue(r.state.value.completed.isEmpty())
    }
    @Test fun newUnresolvedLegacyCannotBeClaimedByStageSelection() {
        val r = repo()
        assertThrows(IllegalArgumentException::class.java) { r.importConfirmedLegacy("unknown", StudyStage.G9, "Name") }
        assertTrue(r.state.value.localProfiles.isEmpty())
    }
    @Test fun g9CannotPersistSolverHistoryThroughRawRepository() {
        val r = repo(); r.createLocalProfile("G9", StudyStage.G9, false)
        assertThrows(IllegalStateException::class.java) { r.changeProduct { it.copy(history = listOf(HistoryItem("x", "lim x", "limits", 1))) } }
        assertTrue(r.state.value.product.history.isEmpty())
    }
    @Test fun leavingLegacyTeacherEntryAlsoHidesCatalogUntilAnotherFileIsChosen() {
        val stores = mutableMapOf<String, FixturePreferences>()
        val r = LocalDemoRepository(true) { stores.getOrPut(it) { FixturePreferences() }.prefs }
        r.signIn(User("Teacher", "teacher@example.test", AccountType.Teacher)); r.logout()
        assertNull(r.state.value.user); assertTrue(r.state.value.books.isEmpty())
    }
}
