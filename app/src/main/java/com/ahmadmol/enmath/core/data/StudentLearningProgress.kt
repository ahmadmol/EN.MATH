package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.Copy
import com.ahmadmol.enmath.core.navigation.Routes

/**
 * Pedagogical progression statuses for lessons / topics.
 * Practice completion is strictly separated from certified syllabus mastery.
 */
enum class LessonProgressStatus {
    NOT_STARTED,
    STARTED,
    PRACTICE_COMPLETED,
    NEEDS_REVIEW,
    UNAVAILABLE
}

/**
 * Detailed metrics for a single practice session (Draft or Completed Attempt).
 */
data class SessionProgressMetrics(
    val totalQuestions: Int,
    val passedCount: Int,
    val firstTryCorrectCount: Int,
    val retryNeededCount: Int,
    val hintsUsedCount: Int,
    val totalMistakesCount: Int,
    val firstAttemptAccuracyPercent: Float?,
    val isCompleted: Boolean
)

/**
 * Historical comparison between practice sessions for the same lesson.
 */
sealed interface ImprovementTrend {
    data class Improved(val previousFirstTry: Int, val currentFirstTry: Int, val total: Int) : ImprovementTrend
    data class Steady(val firstTry: Int, val total: Int) : ImprovementTrend
    data class ReviewSuggested(val previousFirstTry: Int, val currentFirstTry: Int, val total: Int) : ImprovementTrend
    data class SingleSession(val firstTry: Int, val total: Int) : ImprovementTrend
    data object InsufficientData : ImprovementTrend
}

/**
 * Comprehensive student learning progress model for Grade 9.
 */
data class Grade9LearningProgress(
    val activeSubject: Copy,
    val activeUnit: Copy,
    val completedSessionsCount: Int,
    val averageFirstAttemptAccuracy: Float?,
    val totalMistakesOverTime: Int,
    val totalHintsOverTime: Int,
    val natureLessonStatus: LessonProgressStatus,
    val natureSessionMetrics: SessionProgressMetrics?,
    val natureTrend: ImprovementTrend,
    val recommendedNextStep: Copy,
    val recommendedRoute: String,
    val encouragementMessage: Copy,
    val availablePracticeProgressText: Copy,
    val availablePracticeFraction: Float
)

/**
 * Deterministic learning progress engine.
 * Computes metrics, first-attempt accuracy, and improvement trends purely from saved data.
 */
object StudentLearningProgressEngine {
    val ALL_NATURE_IDS: List<String> = (1..6).map { "${NaturePracticeGrader.SOURCE_LESSON}-Q${it.toString().padStart(2, '0')}" }

    /**
     * Compute metrics for a finished attempt.
     * In a valid completed attempt, all 6 questions are passed.
     */
    fun computeAttemptMetrics(attempt: NatureAttempt): SessionProgressMetrics {
        val passedCount = ALL_NATURE_IDS.count { it in attempt.answers }
        val firstTryCorrect = ALL_NATURE_IDS.count { id ->
            id in attempt.answers && (attempt.missed[id] ?: 0) == 0 && id !in attempt.hints
        }
        val retried = ALL_NATURE_IDS.count { (attempt.missed[it] ?: 0) > 0 }
        val hintsCount = attempt.hints.size
        val totalMistakes = attempt.missed.values.sum()
        val accuracy = (firstTryCorrect.toFloat() / 6f) * 100f
        return SessionProgressMetrics(
            totalQuestions = 6,
            passedCount = passedCount,
            firstTryCorrectCount = firstTryCorrect,
            retryNeededCount = retried,
            hintsUsedCount = hintsCount,
            totalMistakesCount = totalMistakes,
            firstAttemptAccuracyPercent = accuracy,
            isCompleted = true
        )
    }

    /**
     * Compute metrics for an active draft.
     * Only attempted questions are counted in the accuracy denominator.
     */
    fun computeDraftMetrics(draft: NatureDraft): SessionProgressMetrics {
        val attempted = ALL_NATURE_IDS.filter { id ->
            id in draft.passed || (draft.missed[id] ?: 0) > 0 || id in draft.answers
        }
        val passedCount = draft.passed.size
        val firstTryCorrect = attempted.count { id ->
            id in draft.passed && (draft.missed[id] ?: 0) == 0 && id !in draft.hints
        }
        val retried = attempted.count { (draft.missed[it] ?: 0) > 0 }
        val hintsCount = draft.hints.size
        val totalMistakes = draft.missed.values.sum()
        val accuracy = if (attempted.isEmpty()) null else {
            (firstTryCorrect.toFloat() / attempted.size.toFloat()) * 100f
        }
        return SessionProgressMetrics(
            totalQuestions = 6,
            passedCount = passedCount,
            firstTryCorrectCount = firstTryCorrect,
            retryNeededCount = retried,
            hintsUsedCount = hintsCount,
            totalMistakesCount = totalMistakes,
            firstAttemptAccuracyPercent = accuracy,
            isCompleted = false
        )
    }

    /**
     * Compare student performance across sessions.
     * Evaluates whether first-try correct responses improved between the previous and latest session.
     */
    fun computeTrend(attempts: List<NatureAttempt>): ImprovementTrend {
        if (attempts.isEmpty()) return ImprovementTrend.InsufficientData
        if (attempts.size == 1) {
            val metrics = computeAttemptMetrics(attempts[0])
            return ImprovementTrend.SingleSession(metrics.firstTryCorrectCount, 6)
        }
        val prev = computeAttemptMetrics(attempts[attempts.size - 2]).firstTryCorrectCount
        val curr = computeAttemptMetrics(attempts.last()).firstTryCorrectCount
        return when {
            curr > prev -> ImprovementTrend.Improved(prev, curr, 6)
            curr == prev -> ImprovementTrend.Steady(curr, 6)
            else -> ImprovementTrend.ReviewSuggested(prev, curr, 6)
        }
    }

    /**
     * Evaluate lesson progress status.
     * Never assigns 'Mastery' for pilot practice; strictly separates Practice Completed from Needs Review.
     *
     * Heuristic Review Criteria:
     * Flagged as NEEDS_REVIEW if firstTryCorrectCount < 3 (out of 6) OR totalMistakesCount >= 4.
     * NOTE: These thresholds are formative heuristic rules for friendly assistance only,
     * not a pedagogical diagnosis or proof of student deficit.
     */
    fun computeNatureLessonStatus(data: NaturePracticeData): LessonProgressStatus {
        // A new saved session must remain resumable even when older sessions are complete.
        if (data.draft != null) return LessonProgressStatus.STARTED
        if (data.attempts.isEmpty()) {
            return LessonProgressStatus.NOT_STARTED
        }
        val latest = data.attempts.last()
        val metrics = computeAttemptMetrics(latest)
        return if (metrics.firstTryCorrectCount < 3 || metrics.totalMistakesCount >= 4) {
            LessonProgressStatus.NEEDS_REVIEW
        } else {
            LessonProgressStatus.PRACTICE_COMPLETED
        }
    }

    /**
     * Build the unified Grade 9 learning progress summary.
     */
    fun computeGrade9Progress(data: NaturePracticeData): Grade9LearningProgress {
        val status = computeNatureLessonStatus(data)
        val trend = computeTrend(data.attempts)
        val completedSessions = data.attempts.size
        val totalMistakes = data.attempts.sumOf { it.missed.values.sum() } + (data.draft?.missed?.values?.sum() ?: 0)
        val totalHints = data.attempts.sumOf { it.hints.size } + (data.draft?.hints?.size ?: 0)

        val currentMetrics = when {
            data.draft != null -> computeDraftMetrics(data.draft)
            data.attempts.isNotEmpty() -> computeAttemptMetrics(data.attempts.last())
            else -> null
        }

        val averageAccuracy: Float? = if (data.attempts.isNotEmpty()) {
            data.attempts.mapNotNull { computeAttemptMetrics(it).firstAttemptAccuracyPercent }.average().toFloat()
        } else {
            currentMetrics?.firstAttemptAccuracyPercent
        }

        val availableFraction: Float
        val progressText: Copy
        val encouragement: Copy
        val nextStep: Copy
        val route: String

        when (status) {
            LessonProgressStatus.PRACTICE_COMPLETED -> {
                availableFraction = 1f
                progressText = Copy(
                    "Completed Nature of Numbers practice (available activity in unit)",
                    "أكملت تدريب طبيعة الأعداد (النشاط التفاعلي المتاح بالوحدة)"
                )
                encouragement = when (trend) {
                    is ImprovementTrend.Improved -> Copy(
                        "Well done! Your first-try answers improved from ${trend.previousFirstTry} to ${trend.currentFirstTry}.",
                        "أحسنت! تحسنت إجاباتك من أول محاولة من ${trend.previousFirstTry} إلى ${trend.currentFirstTry}."
                    )
                    is ImprovementTrend.Steady -> Copy(
                        "Great consistency! Keep up the momentum.",
                        "أداء متوازن ومستقر! تابع رحلتك التعليمية بثقة."
                    )
                    else -> Copy(
                        "You completed the lesson activities! Keep practicing to strengthen your understanding.",
                        "أكملت أنشطة الدرس بنجاح! واصل التدريب لترسيخ المفاهيم."
                    )
                }
                nextStep = Copy("Practice again or review", "تدرّب مجددًا أو راجع")
                route = NaturePracticeGrader.ROUTE
            }
            LessonProgressStatus.NEEDS_REVIEW -> {
                availableFraction = 1f
                progressText = Copy(
                    "Nature of Numbers practice completed · Review recommended",
                    "أكملت تدريب طبيعة الأعداد · يُنصح بمراجعة المفاهيم"
                )
                encouragement = Copy(
                    "A short review of hints and explanations will help you master the tricky parts!",
                    "مراجعة سريعة للتلميحات والتفسيرات ستساعدك على ترسيخ الفهم بلا أخطاء!"
                )
                nextStep = Copy("Review tricky questions", "راجع الأسئلة التي احتجت فيها مساعدة")
                route = NaturePracticeGrader.ROUTE
            }
            LessonProgressStatus.STARTED -> {
                val passed = data.draft?.passed?.size ?: 0
                availableFraction = passed / 6f
                progressText = Copy(
                    "Practice in progress: $passed of 6 questions passed",
                    "التدريب قيد التقدم: أنجزت $passed من 6 أسئلة"
                )
                encouragement = Copy(
                    "Your answers are saved. Resume whenever you are ready!",
                    "إجاباتك السابقة محفوظة. تابع التدريب للوصول إلى النتيجة!"
                )
                nextStep = Copy("Resume practice", "أكمل التدريب المحفوظ")
                route = NaturePracticeGrader.ROUTE
            }
            LessonProgressStatus.NOT_STARTED -> {
                availableFraction = 0f
                progressText = Copy(
                    "Nature of Numbers interactive practice is ready (6 activities)",
                    "تدريب طبيعة الأعداد التفاعلي متاح (6 أنشطة)"
                )
                encouragement = Copy(
                    "Start your mathematical journey with interactive challenges!",
                    "ابدأ رحلتك التعليمية مع أنشطة تفاعلية مشوقة!"
                )
                nextStep = Copy("Start practice", "ابدأ التدريب الآن")
                route = NaturePracticeGrader.ROUTE
            }
            LessonProgressStatus.UNAVAILABLE -> {
                availableFraction = 0f
                progressText = Copy("Textbook content under preparation", "محتوى الكتاب قيد الإعداد")
                encouragement = Copy("Content will be available soon", "سيتاح المحتوى قريبًا")
                nextStep = Copy("Browse source reference", "تصفح مرجع المصدر")
                route = Routes.Grade9Source
            }
        }

        return Grade9LearningProgress(
            activeSubject = Copy("Algebra", "الجبر"),
            activeUnit = Copy("Numbers & Fractions", "الأعداد والكسور"),
            completedSessionsCount = completedSessions,
            averageFirstAttemptAccuracy = averageAccuracy,
            totalMistakesOverTime = totalMistakes,
            totalHintsOverTime = totalHints,
            natureLessonStatus = status,
            natureSessionMetrics = currentMetrics,
            natureTrend = trend,
            recommendedNextStep = nextStep,
            recommendedRoute = route,
            encouragementMessage = encouragement,
            availablePracticeProgressText = progressText,
            availablePracticeFraction = availableFraction
        )
    }
}

/** Saved retry/hint evidence only. The store does not retain historical wrong-answer text. */
data class NatureQuestionReview(val questionId: String, val mistakes: Int, val hintedSessions: Int)

fun natureQuestionReview(data: NaturePracticeData): List<NatureQuestionReview> =
    StudentLearningProgressEngine.ALL_NATURE_IDS.mapNotNull { id ->
        val mistakes = data.attempts.sumOf { it.missed[id] ?: 0 } + (data.draft?.missed?.get(id) ?: 0)
        val hints = data.attempts.count { id in it.hints } + if (id in data.draft?.hints.orEmpty()) 1 else 0
        if (mistakes == 0 && hints == 0) null else NatureQuestionReview(id, mistakes, hints)
    }

/**
 * Grammatically sound Arabic noun count formatter.
 * Implements proper Arabic declension for sessions, attempts, hints, and mistakes:
 * 0: zero form
 * 1: singular + واحدة / واحد
 * 2: dual (مثنى مرفوع: جلستان، محاولتان، تلميحان، خطآن)
 * 3..10: count + plural (تمييز جمع مجرور)
 * 11..99: count + singular (تمييز مفرد منصوب)
 * 100+: correct singular/plural based on standard modulo rules.
 */
object ArabicPlurals {
    private fun formatNoun(
        count: Int,
        zeroForm: String,
        oneForm: String,
        twoForm: String,
        pluralFew: String,
        singularMany: String
    ): String {
        if (count <= 0) return zeroForm
        if (count == 1) return oneForm
        if (count == 2) return twoForm
        val rem100 = count % 100
        return when {
            count in 3..10 -> "$count $pluralFew"
            count in 11..99 -> "$count $singularMany"
            rem100 == 0 -> "$count $singularMany"
            rem100 in 3..10 -> "$count $pluralFew"
            else -> "$count $singularMany"
        }
    }

    /**
     * Sessions / الجلسات (جلسة - مؤنث):
     * 0 -> "0 جلسة"
     * 1 -> "جلسة واحدة"
     * 2 -> "جلستان"
     * 3..10 -> "$count جلسات"
     * 11..99 -> "$count جلسة"
     * 100 -> "100 جلسة"
     */
    fun formatSessions(count: Int): String = formatNoun(
        count = count,
        zeroForm = "0 جلسة",
        oneForm = "جلسة واحدة",
        twoForm = "جلستان",
        pluralFew = "جلسات",
        singularMany = "جلسة"
    )

    /**
     * Attempts / المحاولات (محاولة - مؤنث):
     * 0 -> "0 محاولة"
     * 1 -> "محاولة واحدة"
     * 2 -> "محاولتان"
     * 3..10 -> "$count محاولات"
     * 11..99 -> "$count محاولة"
     * 100 -> "100 محاولة"
     */
    fun formatAttempts(count: Int): String = formatNoun(
        count = count,
        zeroForm = "0 محاولة",
        oneForm = "محاولة واحدة",
        twoForm = "محاولتان",
        pluralFew = "محاولات",
        singularMany = "محاولة"
    )

    /**
     * Hints / التلميحات (تلميح - مذكر):
     * 0 -> "0 تلميح"
     * 1 -> "تلميح واحد"
     * 2 -> "تلميحان"
     * 3..10 -> "$count تلميحات"
     * 11..99 -> "$count تلميح"
     * 100 -> "100 تلميح"
     */
    fun formatHints(count: Int): String = formatNoun(
        count = count,
        zeroForm = "0 تلميح",
        oneForm = "تلميح واحد",
        twoForm = "تلميحان",
        pluralFew = "تلميحات",
        singularMany = "تلميح"
    )

    /**
     * Mistakes / الأخطاء (خطأ - مذكر):
     * 0 -> "0 خطأ"
     * 1 -> "خطأ واحد"
     * 2 -> "خطآن"
     * 3..10 -> "$count أخطاء"
     * 11..99 -> "$count خطأ"
     * 100 -> "100 خطأ"
     */
    fun formatMistakes(count: Int): String = formatNoun(
        count = count,
        zeroForm = "0 خطأ",
        oneForm = "خطأ واحد",
        twoForm = "خطآن",
        pluralFew = "أخطاء",
        singularMany = "خطأ"
    )
}

