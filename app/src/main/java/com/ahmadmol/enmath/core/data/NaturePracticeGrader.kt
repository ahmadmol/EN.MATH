package com.ahmadmol.enmath.core.data

import java.math.BigInteger

/** Deterministic, question-specific pilot graders. No free-form natural language grading. */
object NaturePracticeGrader {
    const val SOURCE_LESSON = "G9-A-U01-L01"
    const val ROUTE = "g9/nature-practice"
    private const val PREFIX = "G9-A-U01-L01-Q"

    /** UI answer contract: choice index, signed reduced fraction, boolean, parts, root|group, or groups. */
    fun correct(questionId: String, submitted: String): Boolean = when (questionId) {
        "${PREFIX}01" -> submitted == "2" // asset choices[2] == √3
        "${PREFIX}02" -> reducedFraction(submitted, BigInteger.valueOf(3L), BigInteger.valueOf(8L))
        "${PREFIX}03" -> submitted == "true"
        "${PREFIX}04" -> submitted.split('|').toSet() == setOf("غير منتهية", "دورية") &&
            submitted.split('|').size == 2
        "${PREFIX}05" -> submitted.split('|').let { it.size == 2 && normalizeDigits(it[0].trim()) == "5" && it[1] == "integer" }
        "${PREFIX}06" -> parseCategories(submitted) == mapOf(
            "-3" to "integer", "2/5" to "decimal", "π" to "irrational",
            "√49" to "integer", "√2" to "irrational"
        )
        else -> false // unreviewed questions fail closed
    }

    /** Refuse unreduced but numerically equivalent fractions, e.g. 6/16. */
    fun reducedFraction(answer: String, expectedNumerator: BigInteger, expectedDenominator: BigInteger): Boolean {
        val normalized = normalizeDigits(answer).replace('⁄', '/').replace('∕', '/').replace('−', '-')
            .replace(Regex("\\s+"), "")
        if (normalized.length > 100 || !Regex("[+-]?[0-9]{1,45}/[+-]?[0-9]{1,45}").matches(normalized)) return false
        val parts = normalized.split('/')
        val n = parts[0].toBigIntegerOrNull() ?: return false
        val d = parts[1].toBigIntegerOrNull() ?: return false
        if (d <= BigInteger.ZERO || n.gcd(d) != BigInteger.ONE) return false
        return n.multiply(expectedDenominator) == d.multiply(expectedNumerator)
    }

    fun normalizeDigits(value: String) = buildString(value.length) {
        for (ch in value) append(when (ch) {
            in '٠'..'٩' -> ('0'.code + ch.code - '٠'.code).toChar()
            in '۰'..'۹' -> ('0'.code + ch.code - '۰'.code).toChar()
            else -> ch
        })
    }

    /** Stable encoding: exactly one group selection for each visible source token. */
    fun serializeCategories(choices: Map<String, String>): String = listOf("-3", "2/5", "π", "√49", "√2")
        .mapNotNull { key -> choices[key]?.let { "$key=$it" } }.joinToString("|")

    /** Restore only the fixed UI tokens and groups. */
    fun restoreCategories(value: String): Map<String, String> = value.split('|').mapNotNull { part ->
        val token = part.substringBefore('=')
        val group = part.substringAfter('=', "")
        if (token in listOf("-3", "2/5", "π", "√49", "√2") &&
            group in listOf("integer", "decimal", "irrational")) token to group else null
    }.toMap()

    private fun parseCategories(value: String): Map<String, String>? {
        val parts = value.split('|')
        if (parts.size != 5) return null
        val pairs = parts.map {
            val i = it.lastIndexOf('=')
            if (i <= 0) return null
            it.substring(0, i) to it.substring(i + 1)
        }
        if (pairs.map { it.first }.distinct().size != 5) return null
        return pairs.toMap()
    }
}
