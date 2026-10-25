package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.AssignmentSubmissionResult
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

class AssignmentSubmissionVerificationTest {
    private val assignment = Assignment("verification-assignment", Copy("Test", "اختبار"),
        Copy("Fixture only", "بيانات اختبار معزولة"), "fixture-class", listOf("limits-q1"),
        Long.MAX_VALUE, points = 10, attempts = 1, late = false)
    private val initial = LearningState(
        user = User("Student", "student@example.test", AccountType.Student),
        product = ProductState(assignments = listOf(assignment)),
    )
    private val answers = mapOf("limits-q1" to checkNotNull(ProductSeed.question("limits-q1")).answer)
    private enum class WriteMode { MemoryOnly, ThrowBeforeUpdate, ThrowAfterUpdate, SilentDiskFailure }
    /** Test-only adapter: models failure ordering, not Android SharedPreferences itself. */
    private class StorageFixture(
        initial: LearningState,
        private val mode: WriteMode = WriteMode.MemoryOnly,
        private val backing: TestLearningRepository = TestLearningRepository(initial),
    ) : LearningRepository by backing {
        var writes = 0
        val durableSnapshot = initial
        override fun changeProduct(transform: (ProductState) -> ProductState) {
            writes++
            if (mode == WriteMode.ThrowBeforeUpdate) throw IllegalStateException("fixture storage failure")
            backing.changeProduct(transform)
            if (mode == WriteMode.ThrowAfterUpdate) throw IllegalStateException("fixture storage failure after publication")
            // Neither MemoryOnly nor SilentDiskFailure acknowledges a durable write.
        }
    }
    private fun assertRejected(expected: AssignmentSubmissionResult, state: LearningState = initial,
                               id: String = assignment.id) {
        val repo = StorageFixture(state)
        val before = repo.state.value
        assertEquals(expected, AppViewModel(repo).submitAssignment(id, answers, 60))
        assertEquals(0, repo.writes)
        assertEquals(before, repo.state.value)
    }

    @Test fun submittedPublishesExactlyItsPayloadAndOneNotificationInMemory() {
        val repo = StorageFixture(initial)
        val result = AppViewModel(repo).submitAssignment(assignment.id, answers, 60)
            as AssignmentSubmissionResult.Submitted
        assertEquals(listOf(result.submission), repo.state.value.product.submissions)
        assertEquals(answers, result.submission.answers)
        assertEquals(10, result.submission.score)
        assertEquals("assignments/${assignment.id}/result", repo.state.value.product.notifications.single().target)
        assertEquals(1, repo.writes)
        assertTrue(repo.durableSnapshot.product.submissions.isEmpty())
    }

    @Test fun secondSubmissionAtAttemptLimitDoesNotWriteOrDuplicateNotifications() {
        val repo = StorageFixture(initial)
        val vm = AppViewModel(repo)
        assertTrue(vm.submitAssignment(assignment.id, answers, 60) is AssignmentSubmissionResult.Submitted)
        val afterFirst = repo.state.value
        repeat(3) {
            assertEquals(AssignmentSubmissionResult.AttemptsExhausted,
                vm.submitAssignment(assignment.id, answers, 60))
        }
        assertEquals(afterFirst, repo.state.value)
        assertEquals(1, repo.writes)
        assertEquals(1, repo.state.value.product.submissions.size)
        assertEquals(1, repo.state.value.product.notifications.size)
    }

    @Test fun guestRejectionDoesNotWrite() {
        assertRejected(AssignmentSubmissionResult.GuestUser,
            initial.copy(user = User("Guest", "", AccountType.Guest)))
    }

    @Test fun deadlineRejectionDoesNotWrite() {
        assertRejected(AssignmentSubmissionResult.DeadlinePassed,
            initial.copy(product = initial.product.copy(assignments = listOf(assignment.copy(due = 1L)))))
    }

    @Test fun assignmentNotFoundDoesNotWrite() {
        assertRejected(AssignmentSubmissionResult.AssignmentNotFound, id = "missing-assignment")
    }

    @Test fun allowedLateSubmissionStillUsesExistingPolicy() {
        val repo = StorageFixture(initial.copy(product = initial.product.copy(
            assignments = listOf(assignment.copy(due = 1L, late = true)))))
        assertTrue(AppViewModel(repo).submitAssignment(assignment.id, answers, 60)
            is AssignmentSubmissionResult.Submitted)
        assertEquals(1, repo.writes)
    }

    @Test fun failureBeforeUpdatePropagatesWithoutReturningSubmittedOrChangingState() {
        val repo = StorageFixture(initial, WriteMode.ThrowBeforeUpdate)
        val failure = assertThrows(IllegalStateException::class.java) {
            AppViewModel(repo).submitAssignment(assignment.id, answers, 60)
        }
        assertEquals("fixture storage failure", failure.message)
        assertEquals(initial, repo.state.value)
        assertEquals(1, repo.writes)
    }

    @Test fun failureAfterPublicationCanLeaveMemoryChangedWithoutDurableSave() {
        val repo = StorageFixture(initial, WriteMode.ThrowAfterUpdate)
        assertThrows(IllegalStateException::class.java) {
            AppViewModel(repo).submitAssignment(assignment.id, answers, 60)
        }
        assertEquals(1, repo.state.value.product.submissions.size)
        assertTrue(repo.durableSnapshot.product.submissions.isEmpty())
        assertEquals(1, repo.writes)
    }

    @Test fun silentDiskFailureCannotBeDistinguishedFromSubmittedByCurrentContract() {
        val repo = StorageFixture(initial, WriteMode.SilentDiskFailure)
        assertTrue(AppViewModel(repo).submitAssignment(assignment.id, answers, 60)
            is AssignmentSubmissionResult.Submitted)
        assertEquals(1, repo.state.value.product.submissions.size)
        // An independent reader of the fixture's durable snapshot sees no submission.
        val reopened = TestLearningRepository(repo.durableSnapshot)
        assertTrue(reopened.state.value.product.submissions.isEmpty())
    }
}
