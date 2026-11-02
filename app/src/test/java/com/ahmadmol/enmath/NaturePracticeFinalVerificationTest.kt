package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.StudyStage
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class NaturePracticeFinalVerificationTest {
    private val disk = mutableMapOf<String, File>()
    private fun reopen() = LocalDemoRepository(false) { key ->
        FixturePreferences(disk.getOrPut(key) { FixturePreferences().file }).prefs
    }
    private val good = listOf("2", "٣/٨", "true", "غير منتهية|دورية", "٥|integer",
        "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational")
    private val bad = listOf("0", "6/16", "false", "دورية", "-5|integer",
        "-3=irrational|2/5=decimal|π=irrational|√49=integer|√2=irrational")
    private fun id(i: Int) = "G9-A-U01-L01-Q${(i + 1).toString().padStart(2, '0')}"
    private fun scope(r: LocalDemoRepository) = StudentCatalog.scope(r.state.value)!!
    private fun data(r: LocalDemoRepository) = (r.naturePractice.read(scope(r)) as NatureRead.Ready).data

    @Test fun everyQuestionPersistsWrongHintCorrectAndPositionAcrossFreshRepositoriesAndStudentSessions() {
        var r = reopen()
        val a = r.createLocalProfile("اختبار أ", StudyStage.G9, false)
        val b = r.createLocalProfile("اختبار ب", StudyStage.G9, false)
        r.selectLocalProfile(a.id)
        r.naturePractice.start(scope(r))
        val draftId = data(r).draft!!.id
        val baseline = r.state.value.product
        for (i in 0..5) {
            assertEquals(PlanWrite.INVALID, r.naturePractice.finish(scope(r)).status)
            assertEquals(false, r.naturePractice.submit(scope(r), id(i), bad[i]).correct)
            assertEquals(PlanWrite.SAVED, r.naturePractice.hint(scope(r)))
            assertEquals(PlanWrite.ALREADY_SAVED, r.naturePractice.hint(scope(r)))
            if (i < 5) assertEquals(PlanWrite.INVALID, r.naturePractice.move(scope(r), i + 1))
            val stale = scope(r)
            val expected = data(r)
            r = reopen()
            r.selectLocalProfile(b.id)
            assertNull(data(r).draft)
            assertTrue(data(r).attempts.isEmpty())
            assertEquals(NatureRead.OwnerChanged, r.naturePractice.read(stale))
            assertEquals(PlanWrite.OWNER_CHANGED, r.naturePractice.submit(stale, id(i), good[i]).status)
            r.selectLocalProfile(a.id)
            assertEquals(expected, data(r))
            assertEquals(draftId, data(r).draft!!.id)
            assertEquals(i, data(r).draft!!.position)
            assertEquals(bad[i], data(r).draft!!.answers[id(i)])
            assertEquals(true, r.naturePractice.submit(scope(r), id(i), good[i]).correct)
            assertEquals(PlanWrite.ALREADY_SAVED, r.naturePractice.submit(scope(r), id(i), bad[i]).status)
            if (i < 5) assertEquals(PlanWrite.SAVED, r.naturePractice.move(scope(r), i + 1))
        }
        assertEquals(baseline, r.state.value.product)
        val finish = r.naturePractice.finish(scope(r))
        assertEquals(PlanWrite.SAVED, finish.status)
        val expected = data(r)
        assertEquals(draftId, finish.resultId)
        assertEquals(6, expected.attempts.single().hints.size)
        assertEquals(6, expected.attempts.single().missed.values.sum())
        r = reopen()
        r.selectLocalProfile(a.id)
        assertEquals(expected, data(r))
        r.selectLocalProfile(b.id)
        r.naturePractice.start(scope(r))
        r.naturePractice.submit(scope(r), id(0), "1")
        val bData = data(r)
        r = reopen()
        r.selectLocalProfile(a.id)
        assertEquals(expected, data(r))
        r.naturePractice.start(scope(r))
        assertNotEquals(draftId, data(r).draft!!.id)
        assertEquals(expected.attempts, data(r).attempts)
        r.selectLocalProfile(b.id)
        assertEquals(bData, data(r))
    }

    @Test fun savedClassificationRestoresTheExactVisibleSelections() {
        val selected = mapOf("-3" to "integer", "2/5" to "decimal", "π" to "irrational",
            "√49" to "integer", "√2" to "irrational")
        assertEquals(selected, NaturePracticeGrader.restoreCategories(NaturePracticeGrader.serializeCategories(selected)))
        assertEquals(mapOf("π" to "irrational"), NaturePracticeGrader.restoreCategories("invalid|π=irrational|other=integer|√2=unknown"))
        assertTrue(NaturePracticeGrader.restoreCategories("").isEmpty())
    }

    @Test fun arabicAndPersianSystemLocalesKeepStableAsciiQuestionIds() {
        val original = java.util.Locale.getDefault()
        try {
            for (language in listOf("ar", "fa")) {
                java.util.Locale.setDefault(java.util.Locale.forLanguageTag(language))
                var r = reopen()
                val student = r.createLocalProfile("اختبار لغة", StudyStage.G9, false)
                r.naturePractice.start(scope(r))
                for (i in 0..5) {
                    assertEquals(true, r.naturePractice.submit(scope(r), id(i), good[i]).correct)
                    if (i < 5) assertEquals(PlanWrite.SAVED, r.naturePractice.move(scope(r), i + 1))
                }
                assertEquals(PlanWrite.SAVED, r.naturePractice.finish(scope(r)).status)
                val expected = data(r)
                r = reopen()
                r.selectLocalProfile(student.id)
                assertEquals(expected, data(r))
            }
        } finally { java.util.Locale.setDefault(original) }
    }

    @Test fun arabicMathPresentationPreservesBankTextAndKeepsRootsAndSignsLeftToRight() {
        for (text in listOf("اختر العدد: 7 / 0.25 / √3 / -2", "صنّف -3، √49، √2.",
            "√49 = 7؛ و2/5 = 0.4", "لأن 5/6 = 0.8333…؛ ولا تنتهي كتابته العشرية.")) {
            val rendered = com.ahmadmol.enmath.features.student.naturePracticeProse(text)
            assertEquals(text, rendered.replace("\u2066", "").replace("\u2069", ""))
            val bidi = java.text.Bidi(rendered, java.text.Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT)
            rendered.forEachIndexed { index, char ->
                if (char == '√' || char == '-' || char == '…') assertEquals("Math symbol must resolve LTR: $text", 0, bidi.getLevelAt(index) % 2)
            }
            if (text.startsWith("اختر")) assertEquals(1, bidi.getLevelAt(rendered.indexOf(':')) % 2)
        }
    }

    @Test fun draftAssetsStayVariantOnlyAndReleaseNavigationHasBothGuards() {
        assertFalse(File("src/main/assets/content/g9/nature_numbers_pilot.json").exists())
        assertEquals(File("src/debug/assets/content/g9/nature_numbers_pilot.json").readText(),
            File("src/w1Qa/assets/content/g9/nature_numbers_pilot.json").readText())
        val graph = File("src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt").readText()
        val practice = graph.substringAfter("composable(NaturePracticeGrader.ROUTE)").substringBefore("composable(\"g9/nature-result")
        val result = graph.substringAfter("composable(\"g9/nature-result").substringBefore("composable(\"g9/practice")
        assertTrue(practice.contains("if (!debuggable"))
        assertTrue(result.contains("if (!debuggable"))
        assertTrue(graph.contains("popUpTo(NaturePracticeGrader.ROUTE) { inclusive = true }"))
        assertTrue(graph.contains("popUpTo(Routes.Home) { inclusive = false }"))
    }
}
