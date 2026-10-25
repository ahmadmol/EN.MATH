package com.ahmadmol.enmath.features.solver

import com.ahmadmol.enmath.core.model.Question

/** Not algebraic simplification: preserve case, signs, bounds and evaluation points. */
internal fun practiceExpressionKey(expression: String): String = expression
    .filterNot { it.isWhitespace() }
    .replace("−", "-").replace("×", "*").replace("÷", "/")
    .replace("²", "^2").replace("³", "^3")

/** This surface opens the expression in Solver, so input-mode variants are one suggestion. */
internal fun similarPractice(
    questions: List<Question>, topicId: String, currentExpression: String, limit: Int = 2,
): List<Question> {
    val current = practiceExpressionKey(currentExpression)
    val ids = mutableSetOf<String>()
    val expressions = mutableSetOf<String>()
    return questions.asSequence().filter { it.topicId == topicId }
        .filter { ids.add(it.id) }
        .filter {
            val key = practiceExpressionKey(it.expression)
            key.isNotBlank() && key != current && expressions.add(key)
        }.take(limit.coerceAtLeast(0)).toList()
}
