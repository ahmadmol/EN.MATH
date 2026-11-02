package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

class Execution2BoundaryTest {
    private fun repo(): LocalDemoRepository {
        val files = mutableMapOf<String, FixturePreferences>()
        return LocalDemoRepository(false) { files.getOrPut(it) { FixturePreferences() }.prefs }
    }
    @Test fun rawOwnerCannotBypassGenerationTicket() {
        val r = repo(); val a = r.createLocalProfile("A", StudyStage.BAC, true)
        r.createLocalProfile("B", StudyStage.BAC, true); r.selectLocalProfile(a.id)
        assertFalse(r.recordLearning(a.id, "raw-owner", "limits-practice") { it })
    }
    @Test fun newAttemptWithoutEditionCannotBeWritten() {
        val r = repo(); r.createLocalProfile("A", StudyStage.BAC, true)
        val set = r.state.value.practiceSets.first()
        assertFalse(r.recordLearning(LearningRules.sessionOwner(r.state.value), "unscoped") {
            it.copy(attempts = it.attempts + Attempt(set.id, emptyMap(), 0, set.questionIds.size, 1, 1))
        })
    }
    @Test fun replayWithDifferentEffectMustNotReportSuccess() {
        val r = repo(); r.createLocalProfile("A", StudyStage.BAC, true)
        val token = LearningRules.sessionOwner(r.state.value)
        assertTrue(r.recordLearning(token, "same") { it.copy(savedLessons = setOf("limits-practice")) })
        assertFalse(r.recordLearning(token, "same") { it.copy(savedLessons = emptySet()) })
        assertEquals(setOf("limits-practice"), r.state.value.product.savedLessons)
    }
    @Test fun rawLocalWritesCannotBypassScope() {
        val r = repo(); r.createLocalProfile("A", StudyStage.BAC, true)
        r.openLesson("derivatives-foundations"); r.completeLesson("limits-practice")
        assertTrue(r.state.value.completed.isEmpty())
        assertNotEquals("derivatives-foundations", r.state.value.lastLessonId)
    }
}
