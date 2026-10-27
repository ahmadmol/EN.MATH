package com.ahmadmol.enmath.features.explore

import kotlin.math.*

interface GraphEngine {
    fun evaluate(expression: String, x: Double): Double?
}

/** Deliberately bounded demo grammar: sums of polynomial monomials and basic trig. No eval. */
class DemoGraphEngine : GraphEngine {
    override fun evaluate(expression: String, x: Double): Double? {
        val s =
            expression
                .lowercase()
                .replace(" ", "")
                .removePrefix("f(x)=")
                .removePrefix("y=")
                .replace("²", "^2")
                .replace("−", "-")
        when (s) {
            "sin(x)" -> return sin(x)
            "cos(x)" -> return cos(x)
            "tan(x)" -> return if (abs(cos(x)) < .001) null else tan(x)
            "2sin(x)", "2*sin(x)" -> return 2 * sin(x)
            "3cos(x)", "3*cos(x)" -> return 3 * cos(x)
            "abs(x)" -> return abs(x)
            "sqrt(x)" -> return if (x < 0) null else sqrt(x)
        }
        if (s.isBlank() || s.length > 100) return null
        val terms = Regex("[+-]?[^+-]+").findAll(s).map { it.value }.toList()
        if (terms.joinToString("") != s) return null
        var sum = 0.0
        for (term in terms) {
            val m = Regex("([+-]?(?:\\d+(?:\\.\\d+)?)?)\\*?x(?:\\^(\\d{1,2}))?").matchEntire(term)
            val value =
                if (m != null) {
                    val coefficient =
                        when (m.groupValues[1]) {
                            "",
                            "+" -> 1.0
                            "-" -> -1.0
                            else -> m.groupValues[1].toDoubleOrNull() ?: return null
                        }
                    val power = m.groupValues[2].toIntOrNull() ?: 1
                    if (power > 8) return null
                    coefficient * x.pow(power)
                } else term.toDoubleOrNull() ?: return null
            sum += value
        }
        return sum.takeIf { it.isFinite() }
    }
}

/** A grammar support probe, not a claim that every sampled x is in the domain. */
fun graphSupported(expression: String): Boolean = expression.isNotBlank() &&
    listOf(-2.0, -1.0, 0.0, 1.0, 2.0).any { DemoGraphEngine().evaluate(expression, it) != null }
