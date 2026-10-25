package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.AssignmentSubmissionResult
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

class W1IdentityIsolationTest {
    private class Stores {
        val files = mutableMapOf<String, FixturePreferences>()
        fun repo() = LocalDemoRepository { name -> files.getOrPut(name) { FixturePreferences() }.prefs }
        fun restart(): LocalDemoRepository {
            files.replaceAll { _, old -> FixturePreferences(old.file) }
            return repo()
        }
    }
    private val guest = User("Guest", "", AccountType.Guest)
    private val a = User("Same name", "a@example.test", AccountType.Student)
    private val b = User("Same name", "b@example.test", AccountType.Student)
    private val lesson = "limits-practice"

    @Test fun guestProgressBookmarksAndUuidSurviveDiskRestartAndAccountSwitch() {
        val stores = Stores()
        var repo = stores.repo()
        repo.signIn(guest)
        val owner = LearningRules.commandOwner(repo.state.value)
        java.util.UUID.fromString(owner)
        repo.completeLesson(lesson)
        repo.changeProduct { it.copy(savedLessons = setOf(lesson)) }
        repo = stores.restart()
        assertEquals(owner, LearningRules.commandOwner(repo.state.value))
        assertTrue(lesson in repo.state.value.completed)
        assertEquals(setOf(lesson), repo.state.value.product.savedLessons)
        repo.signIn(a)
        assertTrue(repo.state.value.completed.isEmpty())
        assertTrue(repo.state.value.product.savedLessons.isEmpty())
        repo.signIn(guest)
        assertEquals(owner, LearningRules.commandOwner(repo.state.value))
        assertEquals(setOf(lesson), repo.state.value.product.savedLessons)
    }
    @Test fun sameNamesDoNotMergePersonalProgressAndChangedContactDoesNotClaimLegacy() {
        val stores = Stores()
        val repo = stores.repo()
        repo.signIn(a)
        repo.completeLesson(lesson)
        repo.signIn(b)
        assertTrue(repo.state.value.completed.isEmpty())
        repo.signIn(a)
        assertEquals(setOf(lesson), repo.state.value.completed)
        repo.signIn(a.copy(email = "changed@example.test"))
        assertTrue(repo.state.value.completed.isEmpty())
    }
    @Test fun unscopedLegacyIsPreservedAndNeverInferredFromCurrentUserOrOldMarker() {
        val stores = Stores()
        val f = FixturePreferences()
        stores.files["enmath_demo"] = f
        val legacy = ProductState(searches = listOf("unresolved"))
        ProductCodec.save(f.prefs, legacy)
        val raw = f.prefs.getString("product_v1", null)
        f.prefs.edit().putString("name", a.name).putString("email", a.email)
            .putString("role", "Student").putString("personal_legacy_owner", "student:${a.email}")
            .putStringSet("completed", setOf(lesson)).commit()
        val repo = stores.repo()
        assertTrue(repo.state.value.completed.isEmpty())
        assertTrue(repo.state.value.product.searches.isEmpty())
        repo.signIn(a)
        assertTrue(repo.state.value.completed.isEmpty())
        assertEquals(raw, f.prefs.getString("product_v1", null))
    }
    @Test fun staleOrMissingOwnersCannotWriteLessonsPracticeOrAssignmentsIntoAnotherAccount() {
        val repo = TestLearningRepository(LearningState(user = a, completed = emptySet(), product = ProductSeed.seed()))
        val vm = AppViewModel(repo)
        val owner = LearningRules.commandOwner(repo.state.value)
        repo.signIn(b)
        val before = repo.state.value
        for (captured in listOf(owner, null)) {
            vm.complete(lesson, captured)
            vm.completeInteractive(lesson, emptyMap(), captured)
            vm.recordPractice(ProductSeed.sets.first().id, emptyMap(), 10, captured)
            assertEquals(AssignmentSubmissionResult.OwnerChanged,
                vm.submitAssignment(before.product.assignments.first().id, emptyMap(), 10, captured))
            assertEquals(before, repo.state.value)
        }
    }
    @Test fun guestLearningOwnerIsAcceptedButNeverAGemsOwner() {
        val stores = Stores()
        val repo = stores.repo()
        repo.signIn(guest)
        val vm = AppViewModel(repo)
        vm.complete(lesson, LearningRules.commandOwner(repo.state.value))
        assertEquals(setOf(lesson), repo.state.value.completed)
        assertNull(com.ahmadmol.enmath.core.localgems.LocalGemsPolicy.owner(guest))
        repo.logout()
        val before = repo.state.value
        vm.complete(lesson, null)
        assertEquals(before, repo.state.value)
    }
    @Test fun practiceCommandReceiptAndAttemptSurviveRestartAndRejectReplay() {
        val stores = Stores()
        var repo = stores.repo()
        repo.signIn(a)
        val set = ProductSeed.sets.first()
        val answers = set.questionIds.associateWith { ProductSeed.question(it)!!.answer }
        AppViewModel(repo).recordPractice(set.id, answers, 60, a.email, "fixture-command")
        assertEquals(1, repo.state.value.product.attempts.size)
        repo = stores.restart()
        val before = repo.state.value
        AppViewModel(repo).recordPractice(set.id, answers, 60, a.email, "fixture-command")
        assertEquals(before, repo.state.value)
        assertEquals(setOf("fixture-command"), before.product.learningCommands)
    }
    @Test fun incompletePersonalMetadataNeverFallsBackToUnscopedLegacyProgress() {
        val stores = Stores()
        val repo = stores.repo()
        repo.signIn(a)
        val global = stores.files.getValue("enmath_demo")
        global.prefs.edit().putStringSet("completed", setOf("legacy-only"))
            .putString("last", "legacy-only").commit()
        val personal = stores.files.entries.single { it.key != "enmath_demo" }.value
        // The fixture's values model missing legacy keys; persist without touching app storage.
        personal.values.remove("completed")
        personal.values.remove("last")
        personal.prefs.edit().commit()
        val restored = stores.restart().state.value
        assertTrue(restored.completed.isEmpty())
        assertEquals("limits-practice", restored.lastLessonId)
        assertEquals(setOf("legacy-only"), global.prefs.getStringSet("completed", null))
    }
}
