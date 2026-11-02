package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.features.explore.DemoGraphEngine
import com.ahmadmol.enmath.features.solver.DemoSolverEngine
import org.junit.Assert.*
import org.junit.Test

class StudentCampaignRegressionTest {
    private val ids = StudentLearningProgressEngine.ALL_NATURE_IDS
    private fun attempt() = NatureAttempt("saved-session", ids.associateWith { "saved" },
        emptyMap(), emptySet(), 100, 200)

    @Test fun unfinishedRetryOverridesCompletedSessionAndPreservesHistoricalEvidence() {
        val old = attempt()
        val data = NaturePracticeData(NatureDraft("new-draft", answers = mapOf(ids[0] to "2"),
            passed = setOf(ids[0])), listOf(old))
        val progress = StudentLearningProgressEngine.computeGrade9Progress(data)
        assertEquals(LessonProgressStatus.STARTED, progress.natureLessonStatus)
        assertEquals(1f / 6f, progress.availablePracticeFraction, .0001f)
        assertEquals(1, progress.completedSessionsCount)
        assertEquals(NaturePracticeGrader.ROUTE, progress.recommendedRoute)
        assertEquals(listOf(old), data.attempts)
    }

    @Test fun reviewContainsOnlyQuestionsWithSavedMistakesOrAssistance() {
        val data = NaturePracticeData(NatureDraft("draft", missed = mapOf(ids[1] to 1), hints = setOf(ids[2])),
            listOf(attempt().copy(missed = mapOf(ids[1] to 2), hints = setOf(ids[1]))))
        assertEquals(listOf(NatureQuestionReview(ids[1], 3, 1), NatureQuestionReview(ids[2], 0, 1)), natureQuestionReview(data))
        assertTrue(natureQuestionReview(NaturePracticeData()).isEmpty())
        assertTrue(natureQuestionReview(NaturePracticeData(attempts = listOf(attempt()))).isEmpty())
    }

    @Test fun hintWithoutAnAnswerDoesNotInventAnAttemptOrLowerAccuracy() {
        val hinted = NatureDraft("draft", hints = setOf(ids[1]))
        val empty = StudentLearningProgressEngine.computeDraftMetrics(hinted)
        assertNull(empty.firstAttemptAccuracyPercent)
        assertEquals(1, empty.hintsUsedCount)
        val answered = StudentLearningProgressEngine.computeDraftMetrics(hinted.copy(
            answers = mapOf(ids[0] to "2"), passed = setOf(ids[0])))
        assertEquals(100f, answered.firstAttemptAccuracyPercent!!, .001f)
        assertEquals(1, answered.firstTryCorrectCount)
    }

    @Test fun graphRejectsMissingMultiplicationOperandAndNonFiniteCoordinates() {
        val graph = DemoGraphEngine()
        for (bad in listOf("*x", "+*x", "-*x", "2+*x", "x^9", "1/x", "sin(x)+1"))
            assertNull(bad, graph.evaluate(bad, 2.0))
        for (x in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY))
            for (s in listOf("sin(x)", "cos(x)", "abs(x)", "sqrt(x)", "x^2")) assertNull(graph.evaluate(s, x))
        assertEquals(5.0, graph.evaluate("2*x+1", 2.0)!!, .0001)
        assertEquals(-2.0, graph.evaluate("-x", 2.0)!!, .0001)
        assertNull(graph.evaluate("sqrt(x)", -1.0))
        assertNull(graph.evaluate("tan(x)", Math.PI / 2))
    }

    @Test fun curatedBaccalaureateSolverReturnsKnownResultsAndRejectsChangedInputs() {
        val solver = DemoSolverEngine()
        val examples = mapOf("det[[1,2],[3,4]]" to "−2", "d/dx(x^2)" to "2x", "∫2xdx" to "x² + C",
            "P(even on fair die)" to "1/2", "lim x→1 (x^2-1)/(x-1)" to "2")
        examples.forEach { (input, answer) -> assertEquals(answer, solver.solve(input).answer) }
        for (s in listOf("det[[2,2],[3,4]]", "∫3xdx", "d/dx(x^3)", "1/0", ""))
            assertNull(solver.solve(s).answer)
    }
}
