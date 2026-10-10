package com.ahmadmol.enmath.core.model

enum class QuestionKind {
    Choice,
    Numeric,
    Expression,
    TrueFalse,
}

data class Question(
    val id: String,
    val topicId: String,
    val prompt: Copy,
    val expression: String,
    val kind: QuestionKind,
    val options: List<String>,
    val answer: String,
    val hint: Copy,
    val explanation: Copy,
)

data class PracticeSet(
    val id: String,
    val topicId: String,
    val title: Copy,
    val questionIds: List<String>,
    val quiz: Boolean = false,
)

data class Attempt(
    val setId: String,
    val answers: Map<String, String>,
    val correct: Int,
    val total: Int,
    val seconds: Int,
    val timestamp: Long,
) {
    val accuracy
        get() = if (total == 0) 0f else correct.toFloat() / total
}

enum class Mastery {
    NotStarted,
    Started,
    Learning,
    Practicing,
    Proficient,
    Mastered,
}

data class TopicMastery(
    val score: Int,
    val stage: Mastery,
    val lessons: Int,
    val accuracy: Float,
    val quiz: Float,
)

data class Classroom(
    val id: String,
    val name: Copy,
    val studentIds: List<String>,
    val code: String,
    val topicId: String = "derivatives",
)

data class Student(
    val id: String,
    val name: Copy,
    val completed: Set<String>,
    val practiceAccuracy: Float,
    val quizAccuracy: Float,
)

data class Assignment(
    val id: String,
    val title: Copy,
    val description: Copy,
    val classId: String,
    val questionIds: List<String>,
    val due: Long,
    val points: Int = 100,
    val attempts: Int = 1,
    val hints: Boolean = true,
    val late: Boolean = true,
)

data class Submission(
    val assignmentId: String,
    val studentId: String,
    val answers: Map<String, String>,
    val score: Int,
    val seconds: Int,
    val timestamp: Long,
    val feedback: Copy = Copy("Review the explanation for each question.", "راجع شرح كل سؤال."),
    val reviewed: Boolean = false,
)

enum class ResourceKind {
    LessonNote,
    FormulaSheet,
    PracticeSet,
    External,
}

data class TeacherResource(
    val id: String,
    val title: Copy,
    val topicId: String,
    val kind: ResourceKind,
    val body: Copy,
    val classIds: Set<String>,
    val updated: Long,
)

data class AppNotification(
    val id: String,
    val title: Copy,
    val body: Copy,
    val target: String,
    val teacher: Boolean,
    val timestamp: Long,
)

data class HistoryItem(
    val id: String,
    val expression: String,
    val topicId: String,
    val timestamp: Long,
)

data class ActivityItem(val id: String, val title: Copy, val timestamp: Long, val minutes: Int)

data class ProductState(
    val questions: List<Question> = emptyList(),
    val attempts: List<Attempt> = emptyList(),
    val classes: List<Classroom> = emptyList(),
    val assignments: List<Assignment> = emptyList(),
    val submissions: List<Submission> = emptyList(),
    val resources: List<TeacherResource> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val readNotifications: Set<String> = emptySet(),
    val savedLessons: Set<String> = emptySet(),
    val savedProblems: Set<String> = emptySet(),
    val history: List<HistoryItem> = emptyList(),
    val searches: List<String> = emptyList(),
    val activity: List<ActivityItem> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val reminder: String = "18:00",
)

data class ProductStats(
    val lessonFraction: Float,
    val exercises: Int,
    val accuracy: Float,
    val minutes: Int,
    val streak: Int,
    val week: List<Int>,
)
