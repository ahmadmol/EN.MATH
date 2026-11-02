package com.ahmadmol.enmath.core.data

import java.util.Locale

/** Script/typography normalization only; never evaluates or repairs malformed mathematics. */
object MathInput {
    fun normalize(value: String): String = buildString {
        value.lowercase(Locale.ROOT).forEach { c ->
            when (c) {
                in '٠'..'٩' -> append('0' + (c - '٠'))
                in '۰'..'۹' -> append('0' + (c - '۰'))
                '٫' -> append('.')
                '−' -> append('-')
                '×', '⋅' -> append('*')
                '²' -> append("^2")
                else -> if (!c.isWhitespace()) append(c)
            }
        }
    }
    fun graphExpression(value: String): String = normalize(value).removePrefix("f(x)=").removePrefix("y=")
}
