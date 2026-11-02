package com.ahmadmol.enmath.core.data

/** Bounded answer equivalence shared by practice and checkpoints. This is not a CAS. */
object MathAnswer {
    fun normalize(value: String): String = MathInput.normalize(value)
    fun number(value: String): Double? {
        val s = normalize(value); val parts=s.split('/')
        val result=if(parts.size==2) {
            val a=parts[0].toDoubleOrNull()?.takeIf(Double::isFinite) ?: return null
            val b=parts[1].toDoubleOrNull()?.takeIf(Double::isFinite) ?: return null
            if(b==0.0) return null else a/b
        } else if(parts.size==1) s.toDoubleOrNull() else null
        return result?.takeIf(Double::isFinite)
    }
    fun matches(expected: String, answer: String): Boolean {
        if(expected.isBlank() || answer.isBlank()) return false
        val wanted=number(expected); val actual=number(answer)
        if(wanted!=null) return actual!=null && kotlin.math.abs(wanted-actual)<1e-6
        val parts=normalize(expected).split('/')
        if (normalize(expected).toDoubleOrNull() != null) return false
        if(parts.size==2 && parts.all { it.toDoubleOrNull()!=null }) return false
        if(normalize(expected) in setOf("nan", "infinity", "+infinity", "-infinity")) return false
        return normalize(expected)==normalize(answer)
    }
}
