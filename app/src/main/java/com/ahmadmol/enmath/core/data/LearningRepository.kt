package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.*
import kotlinx.coroutines.flow.StateFlow

data class LearningState(
    val user: User? = null,
    val completed: Set<String> = DemoContent.initiallyCompleted,
    val lastLessonId: String = "limits-practice",
    val theme: ThemeMode = ThemeMode.System,
    val language: Language = Language.Arabic,
    val onboarded: Boolean = false,
    val product: ProductState = ProductSeed.seed(),
) {
    val books
        get() = DemoContent.books

    val topics
        get() = books.flatMap { it.topics }

    val lessons
        get() = books.flatMap { it.lessons }

    val students
        get() = ProductSeed.students

    val questions
        get() = ProductSeed.questions + product.questions

    val practiceSets
        get() = ProductSeed.sets

    val examples
        get() = DemoContent.examples

    val dailyGoal
        get() = DemoContent.dailyGoal

    fun lesson(id: String) = lessons.find { it.id == id }

    fun topic(id: String) = topics.find { it.id == id }

    fun topicOf(id: String) = topics.find { it.lessons.any { l -> l.id == id } }

    val progress
        get() =
            LearningRules.stats(this).let { stats ->
                ProgressSnapshot(
                    completed.size,
                    lessons.size,
                    stats.exercises,
                    stats.week,
                    stats.streak,
                    DemoContent.dailyGoal,
                    product.activity.count {
                        it.timestamp / 86_400_000 == System.currentTimeMillis() / 86_400_000
                    },
                )
            }
}

interface LearningRepository {
    val state: StateFlow<LearningState>

    fun signIn(user: User)

    fun logout()

    fun finishOnboarding()

    fun completeLesson(id: String)

    fun openLesson(id: String)

    fun setTheme(mode: ThemeMode)

    fun setLanguage(language: Language)

    fun changeProduct(transform: (ProductState) -> ProductState)
}
