package com.ahmadmol.enmath

import androidx.lifecycle.SavedStateHandle
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.features.courses.interactive.*
import com.ahmadmol.enmath.features.practice.QuizViewModel
import org.junit.Assert.*
import org.junit.Test

class InteractiveLessonSessionTest {
    private val blocks = LimitsLessonSequence.blocks(LearningState())
    private val q = blocks[2].question()!!

    @Test fun selectionSwitchesByValueAndCannotAdvanceBeforeSuccessfulVerification() {
        val vm = InteractiveLessonViewModel(SavedStateHandle())
        vm.move(1, blocks)
        vm.move(2, blocks)
        vm.check(q)
        assertFalse(vm.checked.value)
        vm.move(3, blocks)
        assertEquals(2, vm.step.value)
        vm.answer(q.id, "2")
        vm.check(q)
        assertFalse(vm.correct(q))
        vm.move(3, blocks)
        assertEquals(2, vm.step.value)
        vm.answer(q.id, q.answer)
        assertFalse(vm.checked.value)
        assertEquals(1, vm.answers.value.size)
        vm.check(q)
        vm.check(q)
        assertTrue(vm.correct(q))
        vm.answer(q.id, "2")
        assertEquals(q.answer, vm.answers.value[q.id])
        vm.move(3, blocks)
        assertEquals(3, vm.step.value)
    }

    @Test fun reviewedAnswersSurviveBackNavigationAndRestoration() {
        val saved = SavedStateHandle()
        var vm = InteractiveLessonViewModel(saved)
        vm.move(1, blocks)
        vm.move(2, blocks)
        vm.answer(q.id, q.answer)
        vm.check(q)
        vm.move(3, blocks)
        vm = InteractiveLessonViewModel(SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) }))
        vm.move(2, blocks)
        assertTrue(vm.checked.value)
        assertEquals(q.answer, vm.answers.value[q.id])
        vm.move(3, blocks)
        assertEquals(3, vm.step.value)
        assertEquals(q.options, blocks[2].question()!!.options)
    }

    @Test fun editedWrongAnswerDoesNotRestoreStaleFeedback() {
        val vm = InteractiveLessonViewModel(SavedStateHandle())
        vm.move(1, blocks); vm.move(2, blocks)
        vm.answer(q.id, "2"); vm.check(q)
        vm.answer(q.id, q.answer)
        vm.move(1, blocks); vm.move(2, blocks)
        assertFalse(vm.checked.value)
        vm.move(3, blocks)
        assertEquals(2, vm.step.value)
    }

    @Test fun legacySessionCanReviewPassedQuestionsWithoutRecheckingOrChangingOwner() {
        val saved = SavedStateHandle(mapOf("lesson-step" to 3,
            "lesson-answers" to hashMapOf(q.id to q.answer), "local-gems-owner" to "student-a"))
        val vm = InteractiveLessonViewModel(saved)
        vm.restoreLegacyReview(blocks)
        vm.move(2, blocks)
        assertTrue(vm.checked.value)
        vm.answer(q.id, "wrong")
        assertEquals(q.answer, vm.answers.value[q.id])
        assertEquals("student-a", vm.gemsOwner("student-b"))
    }

    @Test fun completionRequiresAllQuestionsAndDispatchesOnlyOnce() {
        val state = LearningState()
        val vm = InteractiveLessonViewModel(SavedStateHandle())
        assertFalse(vm.claimCompletion(blocks))
        vm.move(blocks.lastIndex, blocks)
        assertEquals(0, vm.step.value)
        blocks.forEachIndexed { index, block ->
            block.question()?.let { vm.answer(it.id, it.answer); vm.check(it) }
            if (index < blocks.lastIndex) vm.move(index + 1, blocks)
        }
        assertTrue(vm.claimCompletion(blocks))
        assertFalse(vm.claimCompletion(blocks))
        // Presentation cannot write completion, mastery or gems into the learning state.
        assertEquals(LearningState().completed, state.completed)
        assertTrue(state.product.attempts.isEmpty())
    }

    @Test fun practiceGuardsCheckNextFinishAndRestoresHintAndAnswer() {
        val saved = SavedStateHandle()
        val vm = QuizViewModel(saved)
        vm.check(q)
        assertFalse(vm.checked.value)
        vm.answer(q.id, q.answer)
        vm.hint()
        vm.check(q); vm.check(q)
        vm.answer(q.id, "wrong")
        assertTrue(vm.correct(q))
        val restored = QuizViewModel(SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) }))
        assertTrue(restored.hint.value)
        assertTrue(restored.checked.value)
        assertTrue(vm.claimFinish(listOf(q.id)))
        assertFalse(vm.claimFinish(listOf(q.id)))
        restored.next(0); restored.next(0)
        assertEquals(1, restored.index.value)
        assertFalse(restored.checked.value)
        assertFalse(restored.hint.value)
        restored.retry()
        assertFalse(restored.claimFinish(listOf(q.id, "unanswered")))
    }
}
