package com.ahmadmol.enmath.core.data

import java.util.Locale

/** Bounded answer equivalence shared by practice and checkpoints. This is not a CAS. */
object MathAnswer {
    fun normalize(value: String): String = buildString {
        value.trim().lowercase(Locale.ROOT).forEach { c -> append(when(c) {
            in '٠'..'٩' -> '0' + (c - '٠')
            in '۰'..'۹' -> '0' + (c - '۰')
            '٫' -> '.'; '−' -> '-'; else -> c
        }) }
    }.filterNot(Char::isWhitespace).replace("²", "^2")
    fun number(value: String): Double? {
        val s = normalize(value); val parts=s.split('/')
        val result=if(parts.size==2) {
            val a=parts[0].toDoubleOrNull() ?: return null
            val b=parts[1].toDoubleOrNull() ?: return null
            if(b==0.0) return null else a/b
        } else if(parts.size==1) s.toDoubleOrNull() else null
        return result?.takeIf(Double::isFinite)
    }
    fun matches(expected: String, answer: String): Boolean {
        if(expected.isBlank() || answer.isBlank()) return false
        val wanted=number(expected); val actual=number(answer)
        if(wanted!=null) return actual!=null && kotlin.math.abs(wanted-actual)<1e-6
        val parts=normalize(expected).split('/')
        if(parts.size==2 && parts.all { it.toDoubleOrNull()!=null }) return false
        if(normalize(expected) in setOf("nan", "infinity", "+infinity", "-infinity")) return false
        return normalize(expected)==normalize(answer)
    }
}
