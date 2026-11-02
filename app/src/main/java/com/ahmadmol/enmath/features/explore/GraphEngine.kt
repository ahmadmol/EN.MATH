package com.ahmadmol.enmath.features.explore

import com.ahmadmol.enmath.core.data.MathInput
import kotlin.math.*

fun interface GraphFunction {
    fun evaluate(x: Double): Double?
}

interface GraphEngine {
    fun evaluate(expression: String, x: Double): Double?
    fun prepare(expression: String): GraphFunction? = GraphFunction { evaluate(expression, it) }
}

/** Bounded grammar, parsed once per curve. Sampling allocates no regexes or term lists. */
class DemoGraphEngine : GraphEngine {
    override fun evaluate(expression: String, x: Double): Double? = prepare(expression)?.evaluate(x)

    override fun prepare(expression: String): GraphFunction? {
        if (expression.length > 120) return null
        val s = MathInput.graphExpression(expression)
        if (s.isBlank() || s.length > 100) return null
        val elementary: ((Double) -> Double)? = when (s) {
            "sin(x)" -> { x -> sin(x) }
            "cos(x)" -> { x -> cos(x) }
            "tan(x)" -> { x -> if (abs(cos(x)) < .001) Double.NaN else tan(x) }
            "2sin(x)", "2*sin(x)" -> { x -> 2 * sin(x) }
            "3cos(x)", "3*cos(x)" -> { x -> 3 * cos(x) }
            "abs(x)" -> { x -> abs(x) }
            "sqrt(x)" -> { x -> sqrt(x) }
            else -> null
        }
        if (elementary != null) return finiteFunction(elementary)
        val terms = termPattern.findAll(s).map { it.value }.toList()
        if (terms.joinToString("") != s) return null
        val polynomial = terms.map { term ->
            if (term.removePrefix("+").removePrefix("-").startsWith("*")) return null
            val m = monomialPattern.matchEntire(term)
            if (m == null) (term.toDoubleOrNull()?.takeIf(Double::isFinite) ?: return null) to 0
            else {
                val coefficient = when (m.groupValues[1]) {
                    "", "+" -> 1.0
                    "-" -> -1.0
                    else -> m.groupValues[1].toDoubleOrNull()?.takeIf(Double::isFinite) ?: return null
                }
                val power = m.groupValues[2].toIntOrNull() ?: 1
                if (power > 8) return null
                coefficient to power
            }
        }
        return finiteFunction { x -> polynomial.sumOf { (coefficient, power) -> coefficient * x.pow(power) } }
    }

    private fun finiteFunction(function: (Double) -> Double) = GraphFunction { x ->
        if (!x.isFinite()) null else function(x).takeIf(Double::isFinite)
    }

    companion object {
        private val termPattern = Regex("[+-]?[^+-]+")
        private val monomialPattern = Regex("([+-]?(?:\\d+(?:\\.\\d+)?)?)\\*?x(?:\\^(\\d{1,2}))?")
    }
}

/** Grammar support does not depend on whether an individual coordinate is in the domain. */
fun graphSupported(expression: String): Boolean = DemoGraphEngine().prepare(expression) != null
