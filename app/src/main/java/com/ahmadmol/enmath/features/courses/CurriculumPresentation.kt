package com.ahmadmol.enmath.features.courses

import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.model.Lesson
import kotlin.math.roundToInt

/** Lesson completion, intentionally distinct from quiz-based mastery. */
data class CurriculumProgress(val completed: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else completed.toFloat() / total
    val percent: Int get() = (fraction * 100).roundToInt()
}

fun LearningState.completion(lessons: List<Lesson>) =
    CurriculumProgress(lessons.count { it.id in completed }, lessons.size)

fun LearningState.continueLesson(lessons: List<Lesson>): Lesson? =
    lessons.find { it.id == lastLessonId && it.id !in completed }
        ?: lessons.firstOrNull { it.id !in completed }
        ?: lessons.firstOrNull()
