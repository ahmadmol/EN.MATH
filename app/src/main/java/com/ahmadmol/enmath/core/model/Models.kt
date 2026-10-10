package com.ahmadmol.enmath.core.model

data class Copy(val en: String, val ar: String)

enum class Language {
    English,
    Arabic,
}

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class AccountType {
    Student,
    Teacher,
    Guest,
}

data class User(val name: String, val email: String, val type: AccountType)

data class Lesson(
    val id: String,
    val title: Copy,
    val minutes: Int,
    val introduction: Copy,
    val example: String,
    val explanation: Copy,
    val objectives: Copy =
        Copy("Understand the rule and justify each step.", "افهم القاعدة وبرّر كل خطوة."),
    val rule: Copy = explanation,
    val mistake: Copy =
        Copy(
            "Check the domain before applying a rule.",
            "تحقّق من مجال التعريف قبل تطبيق القاعدة.",
        ),
    val checkpoint: String = "",
    val checkpointAnswer: String = "",
)

data class Topic(val id: String, val title: Copy, val symbol: String, val lessons: List<Lesson>)

data class Book(val id: String, val title: Copy, val subtitle: Copy, val topics: List<Topic>) {
    val lessons
        get() = topics.flatMap { it.lessons }
}

data class ProgressSnapshot(
    val completedLessons: Int,
    val totalLessons: Int,
    val exercises: Int,
    val weeklyActivity: List<Int>,
    val streak: Int,
    val dailyGoal: Int,
    val todayCompleted: Int,
) {
    val fraction
        get() = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons
}

data class SolutionStep(
    val title: Copy,
    val expression: String,
    val explanation: Copy,
    val reason: Copy = title,
)

data class SolutionMethod(val name: Copy, val steps: List<SolutionStep>)

data class Solution(
    val expression: String,
    val answer: String?,
    val steps: List<SolutionStep>,
    val isDemo: Boolean = true,
    val topicId: String = "derivatives",
    val methods: List<SolutionMethod> = emptyList(),
)

data class ProblemExample(val category: Copy, val expression: String)
