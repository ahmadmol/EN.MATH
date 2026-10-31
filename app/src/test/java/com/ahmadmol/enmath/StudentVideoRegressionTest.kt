package com.ahmadmol.enmath

import androidx.lifecycle.SavedStateHandle
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.*
import com.ahmadmol.enmath.features.explore.*
import com.ahmadmol.enmath.features.practice.QuizViewModel
import com.ahmadmol.enmath.features.solver.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import kotlin.math.PI

class StudentVideoRegressionTest {
    @Test fun onlyExactStudentTabsCanShowStudentNavigation() {
        Routes.tabs.forEach { assertTrue(showStudentNavigation(it, listOf(it), true)) }
        val hidden = listOf(null, Routes.Splash, Routes.Main, Routes.Login, Routes.Onboarding,
            Routes.Started, Routes.AccountChoice, Routes.RegisterStudent, Routes.RegisterTeacher,
            Routes.Verify, Routes.Forgot, Routes.NewPassword, Routes.TeacherHome,
            Routes.Book, Routes.Topic, Routes.Lesson, Routes.Solution, Routes.Progress,
            "solver/history", "solver/scan", "solver/solution/{id}", "solver/solution/42",
            "practice/{id}", "practice/{id}/result", "assignments", "assignments/{id}",
            "assignments/{id}/work", "assignments/{id}/result", "library", "search",
            "notifications", "settings", "gems/wallet", "gems/review", "study/catalog")
        hidden.forEach { assertFalse("route=$it", showStudentNavigation(it, listOf(it), true)) }
    }

    @Test fun splashAndSubpageFadeMustFinishBeforeChromeAppears() {
        assertFalse(showStudentNavigation(Routes.Home, listOf(Routes.Splash, Routes.Home), true))
        assertFalse(showStudentNavigation(Routes.Solver, listOf("solver/history", Routes.Solver), true))
        assertFalse(showStudentNavigation(Routes.Splash, listOf(Routes.Home), true))
        assertTrue(showStudentNavigation(Routes.Home, listOf(Routes.Home), true))
        assertTrue(showStudentNavigation(Routes.Study, listOf(Routes.Home, Routes.Study), true))
    }

    @Test fun missingSessionAndUnresolvedRestorationCannotShowOldChrome() {
        assertFalse(showStudentNavigation(Routes.Home, listOf(Routes.Home), false))
        assertFalse(showStudentNavigation(Routes.Home, emptyList(), true))
        assertFalse(showStudentNavigation(Routes.Home, listOf(null, Routes.Home), true))
        assertFalse(showStudentNavigation(null, listOf(Routes.Home), true))
    }

    private val derivative = ProductSeed.questions.first { it.topicId == "derivatives" }

    @Test fun recordedChoiceAndNumericDuplicatesCollapseBeforeTakingTwo() {
        val source = ProductSeed.questions.filter { it.topicId == "derivatives" }
        assertEquals(source[0].expression, source[1].expression)
        val result = similarPractice(source, "derivatives", "d/dx(x^2)")
        assertEquals(2, result.size)
        assertNotEquals(practiceExpressionKey(result[0].expression), practiceExpressionKey(result[1].expression))
        assertEquals(source.first().id, result.first().id)
    }

    @Test fun duplicateIdentityAndNotationVariantsDoNotConsumeSuggestionSlots() {
        val a = derivative.copy(id = "a", expression = "x² − 1")
        val b = a.copy(id = "b", expression = "x^2-1")
        val c = a.copy(id = "c", expression = "x²+1")
        assertEquals(listOf(a, c), similarPractice(listOf(a, a, b, c), "derivatives", "other"))
        assertEquals(listOf(c), similarPractice(listOf(a, b, c), "derivatives", "x^2 - 1"))
    }

    @Test fun signsPowersCaseAndEvaluationPointsRemainDifferentMath() {
        val expressions = listOf("x^2-1", "x^2+1", "x^3-1", "X^2-1",
            "d/dx(x²) at x=2", "d/dx(x²) at x=3", "x=2", "x=-2")
        val questions = expressions.mapIndexed { i, e -> derivative.copy(id = "q$i", expression = e) }
        assertEquals(expressions, similarPractice(questions, "derivatives", "other", 20).map { it.expression })
    }

    @Test fun noAdditionalSuggestionProducesAnEmptyListWithoutFallbackTopic() {
        assertTrue(similarPractice(listOf(derivative), "missing-topic", "other").isEmpty())
        assertTrue(similarPractice(listOf(derivative), derivative.topicId, derivative.expression).isEmpty())
        assertTrue(similarPractice(emptyList(), "derivatives", "other").isEmpty())
    }

    @Test fun viewportRoundTripsAtEverySupportedZoomAndPanDirection() {
        for (zoom in listOf(.25f, .5f, 1f, 2f, 10f)) {
            for (pan in listOf(-900f, 0f, 900f)) {
                val v = GraphViewport(864f, 560f, zoom, pan, -pan)
                for (value in listOf(-12f, -1f, 0f, 1f, 12f)) {
                    assertEquals(value, v.mathX(v.screenX(value)), .0001f)
                    assertEquals(value, v.mathY(v.screenY(value)), .0001f)
                }
                assertTrue(v.screenX(1f) > v.screenX(0f))
                assertTrue(v.screenY(1f) < v.screenY(0f))
            }
        }
    }

    @Test fun pannedYTicksUseInverseYBoundsInsteadOfScreenYSign() {
        val v = GraphViewport(320f, 280f, 1f, 0f, 300f)
        assertEquals(4..12, v.yTicks)
        assertTrue(10 in v.yTicks)
        assertTrue(v.screenY(10f) in 0f..v.height)
        val up = GraphViewport(320f, 280f, 1f, 0f, -300f)
        assertEquals(-12..-4, up.yTicks)
    }

    @Test fun panTranslationAndZoomScalingHaveKnownNumericalResults() {
        val plain = GraphViewport(320f, 280f, 1f)
        val panned = GraphViewport(320f, 280f, 1f, 50f, -30f)
        assertEquals(50f, panned.screenX(2f) - plain.screenX(2f), 0f)
        assertEquals(-30f, panned.screenY(2f) - plain.screenY(2f), 0f)
        val zoomed = GraphViewport(320f, 280f, 2f)
        assertEquals(2f * (plain.screenX(2f) - plain.originX), zoomed.screenX(2f) - zoomed.originX, 0f)
        assertEquals(2f * (plain.originY - plain.screenY(2f)), zoomed.originY - zoomed.screenY(2f), 0f)
    }

    @Test fun tickSeparationAdaptsToPixelDensityTextWidthAndDoubleFontScale() {
        for (unit in listOf(9.5f, 19f, 38f, 76f, 380f)) {
            for (labelSize in listOf(24f, 48f, 96f, 192f)) {
                assertTrue(graphTickStep(unit, labelSize) * unit >= labelSize)
                assertTrue(graphTickStep(unit, labelSize * 2) >= graphTickStep(unit, labelSize))
            }
        }
    }

    @Test fun actualGraphExamplesKeepCorrectSamplesAndCanvasCoordinates() {
        val engine = DemoGraphEngine()
        val v = GraphViewport(400f, 400f, 1f)
        listOf(Triple("x²", 2.0, 4.0), Triple("sin(x)", PI / 2, 1.0),
            Triple("2x+1", -2.0, -3.0)).forEach { (expression, x, expected) ->
            assertEquals(expected, engine.evaluate(expression, x)!!, 1e-8)
            assertEquals(expected.toFloat(), v.mathY(v.screenY(expected.toFloat())), .0001f)
        }
        assertNull(engine.evaluate("sqrt(x)", -1.0))
        assertNull(engine.evaluate("tan(x)", PI / 2))
        assertNull(engine.evaluate("unsupported(x)", 1.0))
    }

    @Test fun supportedSolverExamplesKeepTheirAnswersAndUnsupportedInputsStayUnsolved() {
        val engine = DemoSolverEngine()
        mapOf("2x + 4 = 10" to "x = 3", "d/dx(x²)" to "2x", "∫ 2x dx" to "x² + C",
            "lim x→1 (x^2-1)/(x-1)" to "2", "det[[1,2],[3,4]]" to "−2",
            "p(evenonfairdie)" to "1/2").forEach { (input, answer) ->
            val solution = engine.solve(input)
            assertEquals(answer, solution.answer)
            assertTrue(solution.steps.isNotEmpty())
        }
        listOf("", "x^99", "d/dx(x²) at x=3", "2x+4=11").forEach {
            assertNull(engine.solve(it).answer)
            assertTrue(engine.solve(it).steps.isEmpty())
        }
    }

    @Test fun rapidFinishAndRestoredFinishDispatchOnlyOnePracticeResult() {
        val repo = TestLearningRepository()
        val app = AppViewModel(repo)
        val saved = SavedStateHandle()
        val initialActivityCount = repo.state.value.product.activity.size
        val vm = QuizViewModel(saved)
        val set = ProductSeed.sets.first { it.id == "derivatives-practice-set" }
        set.questionIds.forEachIndexed { index, id ->
            val q = ProductSeed.question(id)!!
            vm.answer(id, q.answer); vm.check(q)
            if (index < set.questionIds.lastIndex) { vm.next(index); vm.next(index) }
        }
        repeat(3) {
            if (vm.claimFinish(set.questionIds)) app.recordPractice(set.id, vm.answers.value, 30)
        }
        val restored = QuizViewModel(SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) }))
        assertFalse(restored.claimFinish(set.questionIds))
        val attempt = repo.state.value.product.attempts.single()
        assertEquals(set.questionIds.size, attempt.correct)
        assertEquals(1f, attempt.accuracy)
        assertEquals(initialActivityCount + 1, repo.state.value.product.activity.size)
    }

    @Test fun resettingSolverCannotUndoIntoPreviousAccountsInput() {
        val vm = SolverViewModel(SavedStateHandle())
        vm.edit("private input"); vm.edit("second private input")
        vm.reset(); vm.undo(); vm.undo()
        assertEquals("", vm.expression.value)
        assertEquals("", vm.submitted.value)
        assertTrue(vm.recent.value.isEmpty())
    }

    @Test fun solverEditDeleteClearAndUndoStillWork() {
        val vm = SolverViewModel(SavedStateHandle())
        vm.edit("2x+4=10"); vm.key("⌫")
        assertEquals("2x+4=1", vm.expression.value)
        vm.undo(); assertEquals("2x+4=10", vm.expression.value)
        vm.key("Clear"); assertEquals("", vm.expression.value)
        vm.undo(); assertTrue(vm.solve()); assertEquals("x = 3", vm.solution.answer)
    }

    @Test fun blankAndInvisibleSearchInputCannotWriteHistory() {
        val repo = TestLearningRepository()
        val app = AppViewModel(repo)
        listOf("", "  ", "\t\n", "ـ", "َُ").forEach(app::search)
        assertTrue(repo.state.value.product.searches.isEmpty())
        app.search("  نهايات  ")
        assertEquals(listOf("نهايات"), repo.state.value.product.searches)
    }

    @Test fun languageAndThemeChangesPreservePracticeSearchSavedAndMasteryData() {
        val repo = TestLearningRepository()
        val app = AppViewModel(repo)
        app.search("limits")
        app.bookmark("problem-1", true)
        app.recordPractice("derivatives-practice-set", mapOf("derivatives-q1" to "6"), 30)
        val before = repo.state.value
        val score = LearningRules.topic(before, "derivatives")
        app.language(Language.English); app.theme(ThemeMode.Dark)
        app.language(Language.Arabic); app.theme(ThemeMode.Light)
        val after = repo.state.value
        assertEquals(before.product, after.product)
        assertEquals(before.completed, after.completed)
        assertEquals(before.activeStudentId, after.activeStudentId)
        assertEquals(before.studentRecords, after.studentRecords)
        assertEquals(score, LearningRules.topic(after, "derivatives"))
    }

    @Test fun anotherAccountCannotInheritHistoricalAliasOrItsMastery() {
        val first = "student:first@example.test"
        val second = "student:second@example.test"
        val record = StudentLearningRecord(setOf("derivatives-rule"), ProductState(searches = listOf("private")))
        val base = LearningState(user = User("First", "first@example.test", AccountType.Student),
            studentRecordId = "legacy", studentRecordOwners = mapOf("legacy" to first),
            studentRecords = mapOf("legacy" to record), completed = emptySet())
        assertEquals("legacy", base.activeStudentId)
        val other = base.copy(user = User("First", "second@example.test", AccountType.Student))
        assertEquals(second, other.activeStudentId)
        assertEquals(0, LearningRules.topic(other, "derivatives").score)
        assertEquals(record, other.studentRecords["legacy"])
        assertEquals(first, other.studentRecordOwners["legacy"])
        assertNotEquals(other.activeStudentId,
            other.copy(user = User("Teacher", "second@example.test", AccountType.Teacher)).activeStudentId)
    }

    @Test fun historyDetailReturnAndTabRestorationKeepExistingNavigationWiring() {
        // Source contract only: actual NavController back-stack execution requires user/device QA.
        val graph = source("core/navigation/ProductGraphs.kt")
        val history = graph.substringAfter("composable(\"solver/history\")").substringBefore("composable(\"explore\")")
        assertTrue(history.contains("onBack = { nav.popBackStack() }"))
        assertTrue(history.contains("::openHistory"))
        assertTrue(graph.contains("item.topicId == \"graph\""))
        assertTrue(graph.contains("route(\"solver/solution/\$id\")"))
        val chrome = source("core/navigation/ENMathApp.kt")
        assertTrue(chrome.contains("visibleEntries.filterNot { it.destination is androidx.navigation.NavGraph }"))
        assertTrue(chrome.contains("saveState = true"))
        assertTrue(chrome.contains("launchSingleTop = true"))
        assertTrue(chrome.contains("restoreState = restoreStudentTabState("))
        assertTrue(chrome.contains("RefTopBar(topTitle, { nav.popBackStack() }"))
    }

    private fun source(path: String): String {
        val relative = "src/main/java/com/ahmadmol/enmath/$path"
        return listOf(File(relative), File("app/$relative")).first { it.exists() }.readText()
    }
}
