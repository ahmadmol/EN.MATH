package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.features.auth.DemoAuthRules
import com.ahmadmol.enmath.features.solver.DemoSolverEngine
import org.junit.Assert.*
import org.junit.Test

class DemoLogicTest {
    @Test
    fun solverNeverInventsAnswersForUnknownExpressions() {
        val result = DemoSolverEngine().solve("sin(9) + y")
        assertNull(result.answer)
        assertTrue(result.steps.isEmpty())
        assertTrue(result.isDemo)
    }

    @Test
    fun curatedExamplesHaveCorrectAnswersAndReasoning() {
        val engine = DemoSolverEngine()
        assertEquals("x = 3", engine.solve("2x+4=10").answer)
        assertEquals("2x", engine.solve("d/dx(x²)").answer)
        assertEquals("x² + C", engine.solve("∫ 2x dx").answer)
        DemoContent.examples.forEach {
            assertNotNull(engine.solve(it.expression).answer)
            assertTrue(engine.solve(it.expression).steps.size >= 2)
        }
    }

    @Test
    fun catalogAndProgressReferencesAreConsistent() {
        val lessons = DemoContent.books.flatMap { it.lessons }
        assertEquals(24, lessons.size)
        assertEquals(lessons.size, lessons.map { it.id }.distinct().size)
        assertTrue(DemoContent.initiallyCompleted.all { id -> lessons.any { it.id == id } })
        assertNotNull(DemoContent.lesson(LearningState().lastLessonId))
        assertEquals(3f / 24, LearningState().progress.fraction)
    }

    @Test
    fun teacherRegistrationUsesEducationFieldsAndValidation() {
        assertTrue(DemoAuthRules.validTeacher("Mathematics", "4"))
        assertFalse(DemoAuthRules.validTeacher("", "4"))
        assertFalse(DemoAuthRules.validTeacher("Mathematics", "not-a-number"))
        assertFalse(DemoAuthRules.validEmail("a@"))
        assertTrue(DemoAuthRules.validEmail("demo@example.com"))
        assertFalse(DemoAuthRules.validPassword("short"))
    }
}
