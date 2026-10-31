package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

class StudentFinalRepairRegressionTest {
    private fun repository(): LocalDemoRepository {
        val files = mutableMapOf<String, FixturePreferences>()
        return LocalDemoRepository(false) { files.getOrPut(it) { FixturePreferences() }.prefs }
    }

    @Test fun grade9CanSaveAnIndependentGraphWithoutLearningOrRewards() {
        val repo = repository(); repo.createLocalProfile("A", StudyStage.G9, false)
        val app = AppViewModel(repo); val before = repo.state.value
        assertTrue(app.saveGraph("x^2", LearningRules.sessionOwner(before)))
        assertTrue(app.saveGraph("x^2", LearningRules.sessionOwner(before)))
        assertEquals(1, repo.state.value.product.history.size)
        assertEquals(1, repo.state.value.product.savedProblems.size)
        assertEquals(before.completed, repo.state.value.completed)
        assertEquals(before.product.pendingGemEvents, repo.state.value.product.pendingGemEvents)
    }

    @Test fun solverHistoryDeduplicatesTheSameExpressionWithinTheProfile() {
        val repo = repository(); repo.createLocalProfile("B", StudyStage.BAC, false)
        val app = AppViewModel(repo); val ticket = LearningRules.sessionOwner(repo.state.value)
        val a = app.history("2x+4=10", "equations", ticket)
        val b = app.history("2x+4=10", "equations", ticket)
        assertFalse(a.isEmpty()); assertEquals(a, b)
        assertEquals(1, repo.state.value.product.history.size)
    }
}
