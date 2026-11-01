package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.NaturePracticeGrader
import org.junit.Assert.*
import org.junit.Test

class NaturePracticeGraderTest {
    private val id = "G9-A-U01-L01-Q"
    @Test fun firstQuestionOnlyExactSourceOption() {
        assertTrue(NaturePracticeGrader.correct("${id}01", "2"))
        listOf("0", "1", "3", "√3", "").forEach { assertFalse(NaturePracticeGrader.correct("${id}01", it)) }
    }
    @Test fun reducedFractionRequiresLowestTermsAndAcceptsArabicPersianDigits() {
        listOf("3/8", "٣/٨", "۳/۸", " ٣ ⁄ ٨ ").forEach { assertTrue(it, NaturePracticeGrader.correct("${id}02", it)) }
        listOf("6/16", "12/32", "0.375", "3/0", "4/8", "-3/8", "3/-8", "3/8/2", "abc")
            .forEach { assertFalse(it, NaturePracticeGrader.correct("${id}02", it)) }
    }
    @Test fun trueFalseIsBooleanNotArbitraryArabicText() {
        assertTrue(NaturePracticeGrader.correct("${id}03", "true"))
        assertFalse(NaturePracticeGrader.correct("${id}03", "false"))
    }
    @Test fun periodicDecimalRequiresBothPieces() {
        assertTrue(NaturePracticeGrader.correct("${id}04", "غير منتهية|دورية"))
        assertTrue(NaturePracticeGrader.correct("${id}04", "دورية|غير منتهية"))
        assertFalse(NaturePracticeGrader.correct("${id}04", "غير منتهية"))
        assertFalse(NaturePracticeGrader.correct("${id}04", "دورية|دورية"))
    }
    @Test fun squareRootActualValueAndSmallestClass() {
        assertTrue(NaturePracticeGrader.correct("${id}05", "5|integer"))
        assertTrue(NaturePracticeGrader.correct("${id}05", "٥|integer"))
        assertFalse(NaturePracticeGrader.correct("${id}05", "-5|integer"))
        assertFalse(NaturePracticeGrader.correct("${id}05", "5|irrational"))
    }
    @Test fun classificationRequiresAllFiveUniqueTokens() {
        val good = mapOf("-3" to "integer", "2/5" to "decimal", "π" to "irrational", "√49" to "integer", "√2" to "irrational")
        assertTrue(NaturePracticeGrader.correct("${id}06", NaturePracticeGrader.serializeCategories(good)))
        assertFalse(NaturePracticeGrader.correct("${id}06", NaturePracticeGrader.serializeCategories(good - "√2")))
        assertFalse(NaturePracticeGrader.correct("${id}06", NaturePracticeGrader.serializeCategories(good + ("√49" to "irrational"))))
        assertFalse(NaturePracticeGrader.correct("G9-A-U01-L02-Q01", "2"))
    }
}
