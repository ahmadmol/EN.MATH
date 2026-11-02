package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.features.courses.*
import org.junit.Assert.*
import org.junit.Test

class CurriculumPresentationTest {
    @Test fun completionUsesOnlyLessonsInTheSelectedBook() {
        val state = LearningState(completed = emptySet())
        val book = state.books.last()
        val updated = state.copy(completed = setOf(book.lessons.first().id, "unrelated"))
        val progress = updated.completion(book.lessons)
        assertEquals(1, progress.completed)
        assertEquals(book.lessons.size, progress.total)
        assertEquals(kotlin.math.round(100f / book.lessons.size).toInt(), progress.percent)
        assertEquals(0, state.completion(book.lessons).percent)
    }

    @Test fun continuationResumesUnfinishedLessonAndStaysInBook() {
        val state = LearningState(completed = emptySet())
        val book = state.books.last()
        val target = book.lessons.last()
        assertEquals(target, state.copy(lastLessonId = target.id).continueLesson(book.lessons))
        val completed = state.copy(lastLessonId = target.id, completed = setOf(target.id))
        assertEquals(book.lessons.first(), completed.continueLesson(book.lessons))
        assertEquals(book.lessons.first(), state.copy(lastLessonId = state.books.first().lessons.first().id).continueLesson(book.lessons))
    }

    @Test fun completedBookOffersReviewAndEmptyBookHasNoDestination() {
        val state = LearningState()
        val book = state.books.first()
        val updated = state.copy(completed = book.lessons.map { it.id }.toSet())
        assertEquals(100, updated.completion(book.lessons).percent)
        assertEquals(book.lessons.first(), updated.continueLesson(book.lessons))
        assertNull(updated.continueLesson(emptyList()))
        assertEquals(0f, updated.completion(emptyList()).fraction, 0f)
    }
}
