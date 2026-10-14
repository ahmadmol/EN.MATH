package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.design.nativeEquationParts
import org.junit.Assert.*
import org.junit.Test

class NativeEquationFormattingTest {
    @Test
    fun limitKeepsItsOriginalApproachAndFractionOperands() {
        val parts = nativeEquationParts("lim x→2 (x²−4)/(x−2)")
        assertEquals("x→2", parts.limit)
        assertEquals("(x²−4)", parts.numerator)
        assertEquals("(x−2)", parts.denominator)
    }

    @Test
    fun ambiguousOperatorPrecedenceRemainsLiteral() {
        listOf("a+b/c", "a/b+c", "d/dx x³", "(a)/(b)=4", "a/b/c", "(a)+(b)/c").forEach {
            val parts = nativeEquationParts(it)
            assertEquals(it, parts.numerator)
            assertNull(parts.denominator)
        }
    }

    @Test
    fun nestedDivisionIsNotReinterpreted() {
        val parts = nativeEquationParts("lim x→∞ (1+1/x)^x")
        assertEquals("x→∞", parts.limit)
        assertEquals("(1+1/x)^x", parts.numerator)
        assertNull(parts.denominator)
    }
}
