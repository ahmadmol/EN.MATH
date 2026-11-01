package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.AccountType
import com.ahmadmol.enmath.core.model.StudyStage
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Senior QA automated test suite for EN.MATH Student Learning Progress & Smart Tracking.
 * Validates session metrics, accuracy formula, retry/hint handling, improvement trends,
 * persistence, student scope isolation, and anti-inflation constraints.
 */
class StudentLearningProgressTest {

    private val disk = mutableMapOf<String, File>()
    private fun reopen() = LocalDemoRepository(false) { key ->
        FixturePreferences(disk.getOrPut(key) { FixturePreferences().file }).prefs
    }
    private fun id(i: Int) = "G9-A-U01-L01-Q${(i + 1).toString().padStart(2, '0')}"
    private fun scope(r: LocalDemoRepository) = StudentCatalog.scope(r.state.value)!!
    private fun data(r: LocalDemoRepository) = (r.naturePractice.read(scope(r)) as NatureRead.Ready).data

    private val good = listOf(
        "2",
        "٣/٨",
        "true",
        "غير منتهية|دورية",
        "٥|integer",
        "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational"
    )
    private val bad = listOf(
        "0",
        "6/16",
        "false",
        "دورية",
        "-5|integer",
        "-3=irrational|2/5=decimal|π=irrational|√49=integer|√2=irrational"
    )

    // ------------------------------------------------------------------------
    // 1. Session progress calculation
    // ------------------------------------------------------------------------
    @Test
    fun sessionProgressCalculatedAccurately() {
        val attempt = NatureAttempt(
            id = "att-001",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = mapOf(id(1) to 2, id(3) to 1),
            hints = setOf(id(2)),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val metrics = StudentLearningProgressEngine.computeAttemptMetrics(attempt)
        assertEquals(6, metrics.totalQuestions)
        assertEquals(6, metrics.passedCount)
        assertEquals(3, metrics.totalMistakesCount) // 2 on Q02 + 1 on Q04
        assertEquals(1, metrics.hintsUsedCount)    // 1 on Q03
        assertEquals(2, metrics.retryNeededCount)  // Q02 and Q04
        // First try correct: Q01, Q05, Q06 (Q02 had retries, Q03 had hint, Q04 had retry) -> exactly 3
        assertEquals(3, metrics.firstTryCorrectCount)
        assertEquals(50.0f, metrics.firstAttemptAccuracyPercent!!, 0.01f)
        assertTrue(metrics.isCompleted)
    }

    // ------------------------------------------------------------------------
    // 2. First Attempt Accuracy calculation: First-Try Correct / Attempted * 100
    // ------------------------------------------------------------------------
    @Test
    fun firstAttemptAccuracyFormulaExact() {
        // 6 passed, 6 first try correct
        val perfectAttempt = NatureAttempt(
            id = "att-perfect",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = emptyMap(),
            hints = emptySet(),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val perfectMetrics = StudentLearningProgressEngine.computeAttemptMetrics(perfectAttempt)
        assertEquals(6, perfectMetrics.firstTryCorrectCount)
        assertEquals(100.0f, perfectMetrics.firstAttemptAccuracyPercent!!, 0.01f)

        // 6 passed, 0 first try correct (all had retries)
        val struggleAttempt = NatureAttempt(
            id = "att-struggle",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = (1..6).associate { id(it - 1) to 1 },
            hints = emptySet(),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val struggleMetrics = StudentLearningProgressEngine.computeAttemptMetrics(struggleAttempt)
        assertEquals(0, struggleMetrics.firstTryCorrectCount)
        assertEquals(0.0f, struggleMetrics.firstAttemptAccuracyPercent!!, 0.01f)
    }

    // ------------------------------------------------------------------------
    // 3. Question correct on first try (no errors, no hints)
    // ------------------------------------------------------------------------
    @Test
    fun questionCorrectOnFirstTryIndependent() {
        val draft = NatureDraft(
            id = "d-1",
            position = 1,
            answers = mapOf(id(0) to good[0]),
            passed = setOf(id(0)),
            missed = emptyMap(),
            hints = emptySet()
        )
        val metrics = StudentLearningProgressEngine.computeDraftMetrics(draft)
        assertEquals(1, metrics.firstTryCorrectCount)
        assertEquals(100.0f, metrics.firstAttemptAccuracyPercent!!, 0.01f)
    }

    // ------------------------------------------------------------------------
    // 4. Question correct after error (NOT first try)
    // ------------------------------------------------------------------------
    @Test
    fun questionCorrectAfterErrorIsNotFirstTry() {
        val draft = NatureDraft(
            id = "d-2",
            position = 1,
            answers = mapOf(id(0) to good[0]),
            passed = setOf(id(0)),
            missed = mapOf(id(0) to 1), // missed once
            hints = emptySet()
        )
        val metrics = StudentLearningProgressEngine.computeDraftMetrics(draft)
        assertEquals(0, metrics.firstTryCorrectCount)
        assertEquals(0.0f, metrics.firstAttemptAccuracyPercent!!, 0.01f)
    }

    // ------------------------------------------------------------------------
    // 5. Question correct after hint (assisted, NOT independent first try)
    // ------------------------------------------------------------------------
    @Test
    fun questionCorrectAfterHintIsNotIndependentFirstTry() {
        val draft = NatureDraft(
            id = "d-3",
            position = 1,
            answers = mapOf(id(0) to good[0]),
            passed = setOf(id(0)),
            missed = emptyMap(),
            hints = setOf(id(0)) // used hint before correct submit
        )
        val metrics = StudentLearningProgressEngine.computeDraftMetrics(draft)
        assertEquals(0, metrics.firstTryCorrectCount)
        assertEquals(0.0f, metrics.firstAttemptAccuracyPercent!!, 0.01f)
    }

    // ------------------------------------------------------------------------
    // 6. Unanswered questions excluded from denominator
    // ------------------------------------------------------------------------
    @Test
    fun unansweredQuestionsExcludedFromDenominator() {
        // Student attempted 2 questions out of 6, both correct on first try
        val draft = NatureDraft(
            id = "d-4",
            position = 2,
            answers = mapOf(id(0) to good[0], id(1) to good[1]),
            passed = setOf(id(0), id(1)),
            missed = emptyMap(),
            hints = emptySet()
        )
        val metrics = StudentLearningProgressEngine.computeDraftMetrics(draft)
        assertEquals(2, metrics.passedCount)
        assertEquals(2, metrics.firstTryCorrectCount)
        // Denominator is 2, NOT 6! Accuracy is 2/2 * 100 = 100%
        assertEquals(100.0f, metrics.firstAttemptAccuracyPercent!!, 0.01f)

        // Brand new draft with 0 attempted questions: denominator is 0, accuracy is null
        val emptyDraft = NatureDraft(id = "d-empty", position = 0)
        val emptyMetrics = StudentLearningProgressEngine.computeDraftMetrics(emptyDraft)
        assertNull(emptyMetrics.firstAttemptAccuracyPercent)
    }

    // ------------------------------------------------------------------------
    // 7. Returning to previous question does not duplicate count
    // ------------------------------------------------------------------------
    @Test
    fun backwardNavigationDoesNotInflateCounts() {
        val r = reopen()
        val p = r.createLocalProfile("طالب التنقل", StudyStage.G9, false)
        r.selectLocalProfile(p.id)
        r.naturePractice.start(scope(r))

        // Answer Q01
        r.naturePractice.submit(scope(r), id(0), good[0])
        r.naturePractice.move(scope(r), 1)

        // Move back to Q01 and move forward to Q02
        r.naturePractice.move(scope(r), 0)
        r.naturePractice.move(scope(r), 1)

        val draft = data(r).draft!!
        assertEquals(1, draft.passed.size)
        val metrics = StudentLearningProgressEngine.computeDraftMetrics(draft)
        assertEquals(1, metrics.passedCount)
        assertEquals(1, metrics.firstTryCorrectCount)
        assertEquals(100.0f, metrics.firstAttemptAccuracyPercent!!, 0.01f)
    }

    // ------------------------------------------------------------------------
    // 8. Retrying practice does not modify previous attempt
    // ------------------------------------------------------------------------
    @Test
    fun retryingPracticePreservesPreviousAttemptIntact() {
        val r = reopen()
        val p = r.createLocalProfile("طالب الإعادة", StudyStage.G9, false)
        r.selectLocalProfile(p.id)
        r.naturePractice.start(scope(r))

        for (i in 0..5) {
            r.naturePractice.submit(scope(r), id(i), good[i])
            if (i < 5) r.naturePractice.move(scope(r), i + 1)
        }
        val finish = r.naturePractice.finish(scope(r))
        assertEquals(PlanWrite.SAVED, finish.status)
        val firstAttempt = data(r).attempts.single()

        // Start new practice session
        r.naturePractice.start(scope(r))
        val newDraft = data(r).draft
        assertNotNull(newDraft)
        assertNotEquals(firstAttempt.id, newDraft?.id)
        // Previous attempt untouched
        assertEquals(listOf(firstAttempt), data(r).attempts)
    }

    // ------------------------------------------------------------------------
    // 9. Improvement calculation between two sessions
    // ------------------------------------------------------------------------
    @Test
    fun improvementBetweenTwoSessions() {
        val attempt1 = NatureAttempt(
            id = "att-1",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = mapOf(id(0) to 1, id(1) to 1, id(2) to 1, id(3) to 1), // 4 missed -> 2 first-try
            hints = emptySet(),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val attempt2 = NatureAttempt(
            id = "att-2",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = mapOf(id(0) to 1), // 1 missed -> 5 first-try
            hints = emptySet(),
            startedAt = 3000L,
            finishedAt = 4000L
        )

        val trend = StudentLearningProgressEngine.computeTrend(listOf(attempt1, attempt2))
        assertTrue(trend is ImprovementTrend.Improved)
        val improved = trend as ImprovementTrend.Improved
        assertEquals(2, improved.previousFirstTry)
        assertEquals(5, improved.currentFirstTry)
        assertEquals(6, improved.total)
    }

    // ------------------------------------------------------------------------
    // 10. No false improvement without sufficient data
    // ------------------------------------------------------------------------
    @Test
    fun noFalseImprovementWithoutSufficientData() {
        // Zero sessions
        val trend0 = StudentLearningProgressEngine.computeTrend(emptyList())
        assertEquals(ImprovementTrend.InsufficientData, trend0)

        // Single session -> reports single session without comparison
        val attempt1 = NatureAttempt(
            id = "att-single",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = mapOf(id(0) to 1),
            hints = emptySet(),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val trend1 = StudentLearningProgressEngine.computeTrend(listOf(attempt1))
        assertTrue(trend1 is ImprovementTrend.SingleSession)
        assertEquals(5, (trend1 as ImprovementTrend.SingleSession).firstTry)
    }

    // ------------------------------------------------------------------------
    // 11. No fake unit mastery percentage, truthful available practice fraction
    // ------------------------------------------------------------------------
    @Test
    fun noFakeUnitPercentageTruthfulAvailablePracticeFraction() {
        // Completed nature practice: available fraction is 1.0f (1/1 available activity), with clear label
        val completedData = NaturePracticeData(
            attempts = listOf(
                NatureAttempt(
                    id = "att-truth",
                    answers = (1..6).associate { id(it - 1) to good[it - 1] },
                    missed = emptyMap(),
                    hints = emptySet(),
                    startedAt = 1000L,
                    finishedAt = 2000L
                )
            )
        )
        val progress = StudentLearningProgressEngine.computeGrade9Progress(completedData)
        assertEquals(1.0f, progress.availablePracticeFraction, 0.01f)
        assertTrue(progress.availablePracticeProgressText.ar.contains("النشاط التفاعلي المتاح"))
        assertFalse(progress.availablePracticeProgressText.ar.contains("100% من المنهاج"))

        // Unstarted: fraction 0.0f
        val unstartedProgress = StudentLearningProgressEngine.computeGrade9Progress(NaturePracticeData())
        assertEquals(0.0f, unstartedProgress.availablePracticeFraction, 0.01f)
    }

    // ------------------------------------------------------------------------
    // 12. Strict separation between Practice Completed and Syllabus Mastery
    // ------------------------------------------------------------------------
    @Test
    fun separationBetweenPracticeCompletedAndMastery() {
        val completedData = NaturePracticeData(
            attempts = listOf(
                NatureAttempt(
                    id = "att-cert",
                    answers = (1..6).associate { id(it - 1) to good[it - 1] },
                    missed = emptyMap(),
                    hints = emptySet(),
                    startedAt = 1000L,
                    finishedAt = 2000L
                )
            )
        )
        val progress = StudentLearningProgressEngine.computeGrade9Progress(completedData)
        // Status is PRACTICE_COMPLETED, NEVER Mastery
        assertEquals(LessonProgressStatus.PRACTICE_COMPLETED, progress.natureLessonStatus)
        assertNotEquals("MASTERY", progress.natureLessonStatus.name)
    }

    // ------------------------------------------------------------------------
    // 13. Progress restored after fresh repository instantiation
    // ------------------------------------------------------------------------
    @Test
    fun progressRestoredAfterFreshRepositoryCreation() {
        var r = reopen()
        val p = r.createLocalProfile("طالب الاستعادة", StudyStage.G9, false)
        r.selectLocalProfile(p.id)
        r.naturePractice.start(scope(r))
        for (i in 0..5) {
            r.naturePractice.submit(scope(r), id(i), good[i])
            if (i < 5) r.naturePractice.move(scope(r), i + 1)
        }
        r.naturePractice.finish(scope(r))

        // Fresh repo instantiation reading disk
        r = reopen()
        r.selectLocalProfile(p.id)
        val restoredData = data(r)
        assertEquals(1, restoredData.attempts.size)
        val progress = StudentLearningProgressEngine.computeGrade9Progress(restoredData)
        assertEquals(1, progress.completedSessionsCount)
        assertEquals(100.0f, progress.averageFirstAttemptAccuracy!!, 0.01f)
        assertEquals(LessonProgressStatus.PRACTICE_COMPLETED, progress.natureLessonStatus)
    }

    // ------------------------------------------------------------------------
    // 14. Data restored after session reopen
    // ------------------------------------------------------------------------
    @Test
    fun dataRestoredAfterSessionReopen() {
        var r = reopen()
        val p = r.createLocalProfile("طالب الجلسة", StudyStage.G9, false)
        r.selectLocalProfile(p.id)
        r.naturePractice.start(scope(r))
        r.naturePractice.submit(scope(r), id(0), bad[0]) // mistake
        r.naturePractice.hint(scope(r))                  // hint

        r = reopen()
        r.selectLocalProfile(p.id)
        val draft = data(r).draft
        assertNotNull(draft)
        assertEquals(1, draft?.missed?.get(id(0)))
        assertTrue(draft?.hints?.contains(id(0)) == true)
    }

    // ------------------------------------------------------------------------
    // 15. Isolation between two distinct students
    // ------------------------------------------------------------------------
    @Test
    fun isolationBetweenTwoStudents() {
        val r = reopen()
        val s1 = r.createLocalProfile("طالب ١", StudyStage.G9, false)
        val s2 = r.createLocalProfile("طالب ٢", StudyStage.G9, false)

        r.selectLocalProfile(s1.id)
        r.naturePractice.start(scope(r))
        r.naturePractice.submit(scope(r), id(0), good[0])

        r.selectLocalProfile(s2.id)
        val s2Data = data(r)
        assertNull(s2Data.draft)
        assertTrue(s2Data.attempts.isEmpty())
        val s2Progress = StudentLearningProgressEngine.computeGrade9Progress(s2Data)
        assertEquals(LessonProgressStatus.NOT_STARTED, s2Progress.natureLessonStatus)
    }

    // ------------------------------------------------------------------------
    // 16. Rejection of operations from stale StudentScope
    // ------------------------------------------------------------------------
    @Test
    fun rejectStaleStudentScopeOperations() {
        val r = reopen()
        val s1 = r.createLocalProfile("طالب أصلي", StudyStage.G9, false)
        val s2 = r.createLocalProfile("طالب لاحق", StudyStage.G9, false)

        r.selectLocalProfile(s1.id)
        val staleScope = scope(r)

        r.selectLocalProfile(s2.id)
        // Stale scope operation must be rejected
        val op = r.naturePractice.submit(staleScope, id(0), good[0])
        assertEquals(PlanWrite.OWNER_CHANGED, op.status)
        assertEquals(NatureRead.OwnerChanged, r.naturePractice.read(staleScope))
    }

    // ------------------------------------------------------------------------
    // 17. Historical attempts preserved over multiple sessions
    // ------------------------------------------------------------------------
    @Test
    fun historicalAttemptsPreservedOverMultipleSessions() {
        val r = reopen()
        val p = r.createLocalProfile("طالب التاريخ", StudyStage.G9, false)
        r.selectLocalProfile(p.id)

        // Session 1
        r.naturePractice.start(scope(r))
        for (i in 0..5) {
            r.naturePractice.submit(scope(r), id(i), good[i])
            if (i < 5) r.naturePractice.move(scope(r), i + 1)
        }
        val r1 = r.naturePractice.finish(scope(r))

        // Session 2
        r.naturePractice.start(scope(r))
        for (i in 0..5) {
            r.naturePractice.submit(scope(r), id(i), good[i])
            if (i < 5) r.naturePractice.move(scope(r), i + 1)
        }
        val r2 = r.naturePractice.finish(scope(r))

        assertEquals(PlanWrite.SAVED, r1.status)
        assertEquals(PlanWrite.SAVED, r2.status)
        assertEquals(2, data(r).attempts.size)
        assertEquals(r1.resultId, data(r).attempts[0].id)
        assertEquals(r2.resultId, data(r).attempts[1].id)
    }

    // ------------------------------------------------------------------------
    // 18. Gems and Planner data untouched by practice training
    // ------------------------------------------------------------------------
    @Test
    fun gemsAndPlannerUntouchedByPractice() {
        val r = reopen()
        val p = r.createLocalProfile("طالب الجواهر", StudyStage.G9, false)
        r.selectLocalProfile(p.id)
        val baselineProduct = r.state.value.product

        r.naturePractice.start(scope(r))
        for (i in 0..5) {
            r.naturePractice.submit(scope(r), id(i), good[i])
            if (i < 5) r.naturePractice.move(scope(r), i + 1)
        }
        r.naturePractice.finish(scope(r))

        // Verify product state is strictly identical
        assertEquals(baselineProduct, r.state.value.product)
        assertTrue(r.state.value.product.pendingGemEvents.isEmpty())
        assertTrue(r.state.value.product.learningCommands.isEmpty())
    }

    // ------------------------------------------------------------------------
    // 19. No duplicate attempts or double submissions
    // ------------------------------------------------------------------------
    @Test
    fun noDuplicateAttemptsOrDoubleSubmissions() {
        val r = reopen()
        val p = r.createLocalProfile("طالب التكرار", StudyStage.G9, false)
        r.selectLocalProfile(p.id)

        r.naturePractice.start(scope(r))
        r.naturePractice.submit(scope(r), id(0), good[0])
        // Submitting already passed question returns ALREADY_SAVED
        val op = r.naturePractice.submit(scope(r), id(0), good[0])
        assertEquals(PlanWrite.ALREADY_SAVED, op.status)

        for (i in 1..5) {
            r.naturePractice.move(scope(r), i)
            r.naturePractice.submit(scope(r), id(i), good[i])
        }
        val fin1 = r.naturePractice.finish(scope(r))
        assertEquals(PlanWrite.SAVED, fin1.status)

        // Attempting to finish again returns ALREADY_SAVED or INVALID because draft is null
        val fin2 = r.naturePractice.finish(scope(r))
        assertEquals(PlanWrite.INVALID, fin2.status)
        assertEquals(1, data(r).attempts.size)
    }

    // ------------------------------------------------------------------------
    // 20. Arabic Plural Rules: Sessions (0, 1, 2, 3, 10, 11, 21, 100)
    // ------------------------------------------------------------------------
    @Test
    fun arabicPluralSessionsCountRules() {
        assertEquals("0 جلسة", ArabicPlurals.formatSessions(0))
        assertEquals("جلسة واحدة", ArabicPlurals.formatSessions(1))
        assertEquals("جلستان", ArabicPlurals.formatSessions(2))
        assertEquals("3 جلسات", ArabicPlurals.formatSessions(3))
        assertEquals("10 جلسات", ArabicPlurals.formatSessions(10))
        assertEquals("11 جلسة", ArabicPlurals.formatSessions(11))
        assertEquals("21 جلسة", ArabicPlurals.formatSessions(21))
        assertEquals("100 جلسة", ArabicPlurals.formatSessions(100))
    }

    // ------------------------------------------------------------------------
    // 21. Arabic Plural Rules: Attempts (0, 1, 2, 3, 10, 11, 21, 100)
    // ------------------------------------------------------------------------
    @Test
    fun arabicPluralAttemptsCountRules() {
        assertEquals("0 محاولة", ArabicPlurals.formatAttempts(0))
        assertEquals("محاولة واحدة", ArabicPlurals.formatAttempts(1))
        assertEquals("محاولتان", ArabicPlurals.formatAttempts(2))
        assertEquals("3 محاولات", ArabicPlurals.formatAttempts(3))
        assertEquals("10 محاولات", ArabicPlurals.formatAttempts(10))
        assertEquals("11 محاولة", ArabicPlurals.formatAttempts(11))
        assertEquals("21 محاولة", ArabicPlurals.formatAttempts(21))
        assertEquals("100 محاولة", ArabicPlurals.formatAttempts(100))
    }

    // ------------------------------------------------------------------------
    // 22. Arabic Plural Rules: Hints (0, 1, 2, 3, 10, 11, 21, 100)
    // ------------------------------------------------------------------------
    @Test
    fun arabicPluralHintsCountRules() {
        assertEquals("0 تلميح", ArabicPlurals.formatHints(0))
        assertEquals("تلميح واحد", ArabicPlurals.formatHints(1))
        assertEquals("تلميحان", ArabicPlurals.formatHints(2))
        assertEquals("3 تلميحات", ArabicPlurals.formatHints(3))
        assertEquals("10 تلميحات", ArabicPlurals.formatHints(10))
        assertEquals("11 تلميح", ArabicPlurals.formatHints(11))
        assertEquals("21 تلميح", ArabicPlurals.formatHints(21))
        assertEquals("100 تلميح", ArabicPlurals.formatHints(100))
    }

    // ------------------------------------------------------------------------
    // 23. Arabic Plural Rules: Mistakes (0, 1, 2, 3, 10, 11, 21, 100)
    // ------------------------------------------------------------------------
    @Test
    fun arabicPluralMistakesCountRules() {
        assertEquals("0 خطأ", ArabicPlurals.formatMistakes(0))
        assertEquals("خطأ واحد", ArabicPlurals.formatMistakes(1))
        assertEquals("خطآن", ArabicPlurals.formatMistakes(2))
        assertEquals("3 أخطاء", ArabicPlurals.formatMistakes(3))
        assertEquals("10 أخطاء", ArabicPlurals.formatMistakes(10))
        assertEquals("11 خطأ", ArabicPlurals.formatMistakes(11))
        assertEquals("21 خطأ", ArabicPlurals.formatMistakes(21))
        assertEquals("100 خطأ", ArabicPlurals.formatMistakes(100))
    }

    // ------------------------------------------------------------------------
    // 24. NEEDS_REVIEW formative heuristic thresholds verification (4 mistakes or <3 first-try)
    // ------------------------------------------------------------------------
    @Test
    fun needsReviewFormativeHeuristicThresholds() {
        // Case A: 4 or more mistakes triggers NEEDS_REVIEW (even if 3 questions were first-try)
        val highMistakesAttempt = NatureAttempt(
            id = "att-review-1",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = mapOf(id(0) to 4), // 4 mistakes on Q01
            hints = emptySet(),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val statusA = StudentLearningProgressEngine.computeNatureLessonStatus(
            NaturePracticeData(attempts = listOf(highMistakesAttempt))
        )
        assertEquals(LessonProgressStatus.NEEDS_REVIEW, statusA)

        // Case B: Less than 3 first-try correct (e.g. 2 first-try, each had 1 mistake: total 4 mistakes)
        val lowFirstTryAttempt = NatureAttempt(
            id = "att-review-2",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = mapOf(id(0) to 1, id(1) to 1, id(2) to 1, id(3) to 1), // 4 questions had retry -> 2 first-try
            hints = emptySet(),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val statusB = StudentLearningProgressEngine.computeNatureLessonStatus(
            NaturePracticeData(attempts = listOf(lowFirstTryAttempt))
        )
        assertEquals(LessonProgressStatus.NEEDS_REVIEW, statusB)

        // Case C: 3 first-try correct with 3 mistakes (< 4 mistakes, >= 3 first-try) -> PRACTICE_COMPLETED
        val passingAttempt = NatureAttempt(
            id = "att-passing",
            answers = (1..6).associate { id(it - 1) to good[it - 1] },
            missed = mapOf(id(0) to 1, id(1) to 1, id(2) to 1), // 3 questions had retry -> exactly 3 first-try, total 3 mistakes
            hints = emptySet(),
            startedAt = 1000L,
            finishedAt = 2000L
        )
        val statusC = StudentLearningProgressEngine.computeNatureLessonStatus(
            NaturePracticeData(attempts = listOf(passingAttempt))
        )
        assertEquals(LessonProgressStatus.PRACTICE_COMPLETED, statusC)
    }
}

