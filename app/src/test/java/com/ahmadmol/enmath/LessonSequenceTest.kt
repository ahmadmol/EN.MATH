package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.features.courses.interactive.*
import org.junit.Assert.*
import org.junit.Test

class LessonSequenceTest {
    @Test
    fun sequenceReferencesExistingQuestionsAndRequiresCheckpointBeforeCompletion() {
        val state = LearningState()
        val blocks = LimitsLessonSequence.blocks(state)
        assertEquals(blocks.size, blocks.map { it.id }.distinct().size)
        assertTrue(blocks.first() is LessonBlock.Explanation)
        assertTrue(blocks[1] is LessonBlock.Visual)
        assertTrue(blocks.last() is LessonBlock.Completion)
        assertTrue(blocks[blocks.lastIndex - 1] is LessonBlock.Checkpoint)
        blocks
            .mapNotNull { it.question() }
            .forEach { q ->
                assertEquals(state.questions.first { it.id == q.id }, q)
                assertTrue(LearningRules.correct(q, q.answer))
                assertFalse(LearningRules.correct(q, "wrong"))
            }
        assertEquals(
            state.lesson(LimitsLessonSequence.lessonId)?.example,
            blocks.filterIsInstance<LessonBlock.WorkedExample>().single().expression,
        )
    }

    @Test
    fun presentationDoesNotCreateAnIndependentProgressRecord() {
        val state = LearningState()
        val score = LearningRules.topic(state, "limits").score
        LimitsLessonSequence.blocks(state)
        assertEquals(score, LearningRules.topic(state, "limits", "ahmed").score)
        assertEquals(state.completed, LearningState().completed)
    }
}
