package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.explore.DemoGraphEngine
import com.ahmadmol.enmath.features.solver.DemoSolverEngine
import org.junit.Assert.*
import org.junit.Test

class ProductRulesTest {
    @Test
    fun allSeedRelationshipsResolve() {
        val p = ProductSeed.seed(1_000_000_000_000L)
        assertEquals(25, ProductSeed.students.size)
        assertEquals(2, p.classes.size)
        assertEquals(48, ProductSeed.questions.size)
        p.assignments.forEach { a ->
            assertTrue(p.classes.any { it.id == a.classId })
            assertTrue(a.questionIds.all { ProductSeed.question(it) != null })
        }
        p.submissions.forEach { s ->
            assertTrue(p.assignments.any { it.id == s.assignmentId })
            assertTrue(ProductSeed.students.any { it.id == s.studentId })
            assertTrue(s.answers.keys.all { ProductSeed.question(it) != null })
        }
        ProductSeed.sets.forEach { s ->
            assertTrue(s.questionIds.size >= 4)
            assertNotNull(DemoContent.topic(s.topicId))
            assertTrue(s.questionIds.all { ProductSeed.question(it)?.topicId == s.topicId })
        }
    }

    @Test
    fun curriculumOrderAndQuestionKindsMatchSpecification() {
        assertEquals(
            listOf("limits", "derivatives", "integrals", "sequences"),
            DemoContent.books[0].topics.map { it.id },
        )
        assertEquals(
            listOf("matrices", "probability", "geometry", "logarithms"),
            DemoContent.books[1].topics.map { it.id },
        )
        DemoContent.books
            .flatMap { it.topics }
            .forEach { t ->
                assertTrue(t.lessons.size in 3..4)
                assertEquals(
                    QuestionKind.entries.toSet(),
                    ProductSeed.questions.filter { it.topicId == t.id }.map { it.kind }.toSet(),
                )
            }
    }

    @Test
    fun masteryUsesDocumentedWeightsAndThresholds() {
        val t = DemoContent.topic("derivatives")!!
        assertEquals(Mastery.NotStarted, LearningRules.mastery(t, emptySet(), 0f, 0f).stage)
        assertEquals(100, LearningRules.mastery(t, t.lessons.map { it.id }.toSet(), 1f, 1f).score)
        assertEquals(
            Mastery.Mastered,
            LearningRules.mastery(t, t.lessons.map { it.id }.toSet(), 1f, 1f).stage,
        )
        assertEquals(60, LearningRules.mastery(t, emptySet(), 1f, 1f).score)
    }

    @Test
    fun teacherAndStudentReadSameAhmedMastery() {
        val state = LearningState()
        val student = LearningRules.topic(state, "derivatives")
        val teacher = LearningRules.topic(state, "derivatives", "ahmed")
        assertEquals(student, teacher)
        val updated = state.copy(completed = state.completed + "derivatives-practice")
        assertTrue(LearningRules.topic(updated, "derivatives", "ahmed").score > teacher.score)
    }

    @Test
    fun gradingAcceptsNumericallyEquivalentAnswersWithoutEvaluatingCode() {
        val q = ProductSeed.question("limits-q2")!!
        assertTrue(LearningRules.correct(q, "5.000000"))
        assertFalse(LearningRules.correct(q, "5; exec()"))
        assertFalse(LearningRules.correct(q, "NaN"))
    }

    @Test
    fun graphSupportsCuratedFunctionsAndBoundedPolynomials() {
        val g = DemoGraphEngine()
        assertEquals(4.0, g.evaluate("y=x²", 2.0)!!, 1e-6)
        assertEquals(0.0, g.evaluate("sin(x)", 0.0)!!, 1e-6)
        assertEquals(5.0, g.evaluate("2x+1", 2.0)!!, 1e-6)
        assertEquals(9.0, g.evaluate("3*x^2-2x+1", 2.0)!!, 1e-6)
        assertNull(g.evaluate("x^99", 2.0))
        assertNull(g.evaluate("system('rm')", 0.0))
        assertNull(g.evaluate("ln(x)", 1.0))
    }

    @Test
    fun limitMethodsHaveSameCorrectFinalAnswer() {
        val s = DemoSolverEngine().solve("lim x→1 (x^2-1)/(x-1)")
        assertEquals("2", s.answer)
        assertEquals(2, s.methods.size)
        assertTrue(s.methods.all { it.steps.isNotEmpty() })
    }

    @Test
    fun arabicSearchIgnoresDiacriticsAndAlefVariants() {
        assertEquals(LearningRules.search("الإشتقاق"), LearningRules.search("الاشتقاق"))
        assertEquals("نهايه", LearningRules.search("نِهَايَة"))
    }
}
