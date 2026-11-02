package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.explore.*
import com.ahmadmol.enmath.features.solver.DemoSolverEngine
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale
import androidx.compose.ui.graphics.luminance

class StudentPolishRegressionTest {
    private class Stores {
        val files = mutableMapOf<String, FixturePreferences>()
        fun repo() = LocalDemoRepository(false) { files.getOrPut(it) { FixturePreferences() }.prefs }
        fun restart(): LocalDemoRepository {
            files.replaceAll { _, f -> FixturePreferences(f.file) }
            return repo()
        }
    }

    @Test fun nonfiniteFractionOperandsCannotBecomeCorrectZeroAnswers() {
        for (value in listOf("1/Infinity", "1/-Infinity", "1/1e999", "Infinity/2", "NaN/2")) {
            assertNull(value, MathAnswer.number(value))
            assertFalse(value, MathAnswer.matches("0", value))
        }
    }

    @Test fun overflowExpectedAnswersCannotPassByTextEquality() {
        for (value in listOf("1e999", "-1e999", "1/0", "Infinity", "NaN"))
            assertFalse(value, MathAnswer.matches(value, value))
        assertTrue(MathAnswer.matches("1/2", "٠٫٥"))
        assertTrue(MathAnswer.matches("-3/4", "−۰٫۷۵"))
    }

    @Test fun graphSupportsArabicPersianDigitsDecimalsAndMultiplication() {
        val graph = DemoGraphEngine()
        for (input in listOf("٢×x+١", "۲*x+۱", "f(x) = ٢x + ١", "2\t*x+1"))
            assertEquals(input, 5.0, graph.evaluate(input, 2.0)!!, .00001)
        assertEquals(0.0, graph.evaluate("٠٫٥x²−٢", 2.0)!!, .00001)
        for (input in listOf("١/٠", "√-١", "x*x", "sin(x)+1", "NaN", "Infinity"))
            assertNull(input, graph.evaluate(input, 1.0))
    }

    @Test fun curatedSolverAcceptsEquivalentDigitScriptsWithoutGuessing() {
        val solver = DemoSolverEngine()
        for (input in listOf("٢x + ٤ = ١٠", "۲x+۴=۱۰", "2x\t+4=10"))
            assertEquals("x = 3", solver.solve(input).answer)
        assertEquals("−2", solver.solve("det[[١,٢],[٣,٤]]").answer)
        assertNull(solver.solve("٢x+٤=١١").answer)
    }

    @Test fun solverNormalizationIsIndependentOfTurkishLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("1/2", DemoSolverEngine().solve("P(EVEN ON FAIR DIE)").answer)
        } finally { Locale.setDefault(original) }
    }

    @Test fun graphDescriptionRejectsUnsupportedGrammar() {
        assertTrue(getFunctionDescription("x*x", false).contains("unsupported", ignoreCase = true))
        assertEquals(getFunctionDescription("0.5x^2-2", false), getFunctionDescription("٠٫٥x²−٢", false))
    }

    @Test fun zeroFunctionHasEveryRealNumberAsARoot() {
        assertTrue(getFunctionDescription("0x", false).contains("every real", ignoreCase = true))
        assertTrue(getFunctionDescription("0x+2", false).contains("no roots", ignoreCase = true))
    }

    @Test fun natureFailedWriteInvalidatesObserversPreservesRecordsAndRestarts() {
        val stores = Stores(); val repo = stores.repo()
        val owner = repo.createLocalProfile("A", StudyStage.G9, false)
        val scope = StudentCatalog.scope(repo.state.value)!!
        repo.naturePractice.start(scope)
        val f = stores.files.getValue("enmath_g9_nature_pilot_v1_${owner.id}")
        val bytes = f.file.readBytes(); val before = repo.naturePractice.changes.value
        val original = repo.naturePractice.read(scope)
        f.fail = true
        assertEquals(PlanWrite.STORAGE_ERROR, repo.naturePractice.hint(scope))
        assertTrue(repo.naturePractice.changes.value > before)
        assertEquals(NatureRead.StorageError, repo.naturePractice.read(scope))
        assertArrayEquals(bytes, f.file.readBytes())
        val restarted = stores.restart()
        assertEquals(original, restarted.naturePractice.read(StudentCatalog.scope(restarted.state.value)!!))
    }

    @Test fun sourceFailedWriteNotifiesAndKeepsBookmarksAcrossRestartAndAccounts() {
        val stores = Stores(); val repo = stores.repo()
        val a = repo.createLocalProfile("A", StudyStage.BAC, false)
        val scope = StudentCatalog.scope(repo.state.value)!!
        val refs = SourceCurricula.forStage(StudyStage.BAC).researches.take(2).map { it.ref }
        repo.sourceWorkspace.bookmark(scope, refs[0], true)
        val f = stores.files.getValue("enmath_source_workspace_v1_${a.id}_BAC")
        val bytes = f.file.readBytes(); val before = repo.sourceWorkspace.changes.value
        f.fail = true
        assertEquals(PlanWrite.STORAGE_ERROR, repo.sourceWorkspace.bookmark(scope, refs[1], true))
        assertTrue(repo.sourceWorkspace.changes.value > before)
        assertArrayEquals(bytes, f.file.readBytes())
        val restarted = stores.restart()
        val s = StudentCatalog.scope(restarted.state.value)!!
        assertEquals(setOf(refs[0].lessonId), (restarted.sourceWorkspace.read(s) as SourceWorkspaceRead.Ready).workspace.savedIds)
        restarted.createLocalProfile("B", StudyStage.BAC, false)
        assertTrue((restarted.sourceWorkspace.read(StudentCatalog.scope(restarted.state.value)!!) as SourceWorkspaceRead.Ready).workspace.savedIds.isEmpty())
        assertEquals(SourceWorkspaceRead.OwnerChanged, restarted.sourceWorkspace.read(s))
    }

    @Test fun plannerFailedWriteInvalidatesSummaryAndRetainsHistory() {
        val stores = Stores(); val repo = stores.repo()
        val owner = repo.createLocalProfile("A", StudyStage.G9, false)
        val scope = StudentCatalog.scope(repo.state.value)!!
        val task = PlanTask("goal", "Review", "Math", PlanPeriod.WEEK,
            StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK), PlanMode.ACHIEVEMENT, 2)
        assertEquals(PlanWrite.SAVED, repo.planner.save(scope, 0, "create", task))
        val original = repo.planner.read(scope)
        val f = stores.files.entries.single { it.key.startsWith("enmath_plan") && it.key.contains(owner.id) }.value
        val bytes = f.file.readBytes(); val before = repo.planner.changes.value
        f.fail = true
        assertEquals(PlanWrite.STORAGE_ERROR, repo.planner.remove(scope, 1, "remove", task.id))
        assertTrue(repo.planner.changes.value > before)
        assertArrayEquals(bytes, f.file.readBytes())
        val restarted = stores.restart()
        assertEquals(original, restarted.planner.read(StudentCatalog.scope(restarted.state.value)!!))
    }

    @Test fun wrongRegistryPreferenceTypeFailsClosedWithoutOverwritingIt() {
        val f = FixturePreferences()
        f.prefs.edit().putInt("registry", 42).commit()
        val bytes = f.file.readBytes()
        val store = LocalLearnerStore { f.prefs }
        assertThrows(CorruptLocalData::class.java) { store.profiles() }
        assertArrayEquals(bytes, f.file.readBytes())
    }

    @Test fun preparedGraphPreservesDomainAndFiniteValueContracts() {
        val graph = DemoGraphEngine()
        val polynomial = graph.prepare("٢×x²−٤")!!
        for (x in -100..100) assertEquals(2.0 * x * x - 4, polynomial.evaluate(x.toDouble())!!, .00001)
        assertNull(polynomial.evaluate(Double.POSITIVE_INFINITY))
        assertNull(graph.prepare("sqrt(x)")!!.evaluate(-1.0))
        assertNull(graph.prepare("tan(x)")!!.evaluate(Math.PI / 2))
        assertNull(graph.prepare("1/0"))
    }

    @Test fun preparedCurveSamplingBenchmarkRecordsComparableChecksums() {
        val graph = DemoGraphEngine(); val input = "2*x^2-4*x+3"; val prepared = graph.prepare(input)!!
        var direct = 0.0; var compiled = 0.0
        repeat(500) { prepared.evaluate(it / 100.0); graph.evaluate(input, it / 100.0) }
        val t0 = System.nanoTime()
        repeat(4000) { direct += graph.evaluate(input, it / 1000.0)!! }
        val t1 = System.nanoTime()
        repeat(4000) { compiled += prepared.evaluate(it / 1000.0)!! }
        val t2 = System.nanoTime()
        assertEquals(direct, compiled, .000001)
        println("GRAPH_SAMPLING_4000 direct_ns=${t1-t0} prepared_ns=${t2-t1}; host microbenchmark, not device FPS")
    }

    @Test fun studentPrimaryTextHasAccessibleContrastInBothThemesAndStages() {
        for (stage in StudyStage.entries) for (dark in listOf(false, true)) {
            val colors = com.ahmadmol.enmath.core.design.studentColorScheme(dark, stage)
            fun contrast(a: androidx.compose.ui.graphics.Color, b: androidx.compose.ui.graphics.Color): Float {
                val x = a.luminance()
                val y = b.luminance()
                return (maxOf(x, y) + .05f) / (minOf(x, y) + .05f)
            }
            assertTrue("$stage dark=$dark primary text", contrast(colors.primary, colors.surface) >= 4.5f)
            assertTrue("$stage dark=$dark button text", contrast(colors.primary, colors.onPrimary) >= 4.5f)
        }
    }

    @Test fun thrownCommitFailsClosedAndNotifiesAllScopedDocuments() {
        val stores = Stores(); val repo = stores.repo()
        repo.createLocalProfile("A", StudyStage.G9, false)
        val scope = StudentCatalog.scope(repo.state.value)!!
        var fail = false
        val prefs: (String) -> android.content.SharedPreferences = { name ->
            val original = stores.files.getOrPut(name) { FixturePreferences() }.prefs
            java.lang.reflect.Proxy.newProxyInstance(original.javaClass.classLoader,
                arrayOf(android.content.SharedPreferences::class.java)) { _, method, args ->
                if (method.name == "edit") {
                    val editor = original.edit()
                    java.lang.reflect.Proxy.newProxyInstance(editor.javaClass.classLoader,
                        arrayOf(android.content.SharedPreferences.Editor::class.java)) { proxy, call, arguments ->
                        if (call.name == "commit" && fail) throw SecurityException("Fixture storage unavailable")
                        val value = call.invoke(editor, *(arguments ?: emptyArray()))
                        if (call.name.startsWith("put")) proxy else value
                    }
                } else method.invoke(original, *(args ?: emptyArray()))
            } as android.content.SharedPreferences
        }
        val nature = NaturePracticeRepository(prefs) { repo.state.value }
        val source = SourceWorkspaceRepository(prefs) { repo.state.value }
        val planner = PlannerRepository(prefs) { repo.state.value }
        val study = Grade9StudyRepository(prefs) { repo.state.value }
        nature.start(scope)
        val ref = SourceCurricula.forStage(StudyStage.G9).researches.first().ref
        source.visit(scope, ref)
        study.selectSubject(scope, Grade9Subject.ALGEBRA)
        val task = PlanTask("goal", "Review", "Math", PlanPeriod.WEEK,
            StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK), PlanMode.ACHIEVEMENT, 1)
        planner.save(scope, 0, "create", task)
        val originals = stores.files.mapValues { it.value.file.readBytes() }
        val changes = listOf(nature.changes.value, source.changes.value, planner.changes.value, study.changes.value)
        fail = true
        assertEquals(PlanWrite.STORAGE_ERROR, nature.hint(scope))
        assertEquals(PlanWrite.STORAGE_ERROR, source.bookmark(scope, ref, true))
        assertEquals(PlanWrite.STORAGE_ERROR, planner.remove(scope, 1, "remove", task.id))
        assertEquals(PlanWrite.STORAGE_ERROR, study.selectSubject(scope, Grade9Subject.GEOMETRY))
        assertEquals(NatureRead.StorageError, nature.read(scope))
        assertEquals(SourceWorkspaceRead.StorageError, source.read(scope))
        assertEquals(PlanRead.StorageError, planner.read(scope))
        assertEquals(G9StudyRead.StorageError, study.read(scope))
        listOf(nature.changes.value, source.changes.value, planner.changes.value, study.changes.value)
            .forEachIndexed { i, value -> assertTrue(value > changes[i]) }
        originals.forEach { (name, bytes) -> assertArrayEquals(bytes, stores.files.getValue(name).file.readBytes()) }
    }
}
