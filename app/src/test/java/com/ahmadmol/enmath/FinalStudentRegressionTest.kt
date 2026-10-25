package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.explore.getFunctionDescription
import org.junit.Assert.*
import org.junit.Test

class FinalStudentRegressionTest {
    private fun student(email: String, name: String = "Ahmed") =
        LearningState(user = User(name, email, AccountType.Student))

    @Test fun domainsPunctuationAndAhmedNamesCannotAliasAnotherStudent() {
        val emails = listOf("ahmed@example.com", "ahmed@other.test", "a.b@scope-1.example.test", "a_b@scope-1.example.test", "a-b@scope-1.example.test", "a.b@scope-2.example.test")
        assertEquals(emails.size, emails.map { student(it).activeStudentId }.toSet().size)
        assertEquals("ahmed", student(emails.first()).activeStudentId)
        assertEquals(student("sara@scope-1.example.test").activeStudentId, student(" SARA@scope-1.example.test ", "Renamed").activeStudentId)
        assertNotEquals("ahmed", LearningState().activeStudentId)
    }

    @Test fun anotherStudentCannotReadCurrentStudentsMasteryThroughAhmedAlias() {
        val state = student("other@scope-1.example.test").copy(completed = emptySet())
        assertEquals(0, LearningRules.topic(state, "limits").lessons)
        assertTrue(LearningRules.topic(state, "limits", "ahmed").lessons > 0)
        val teacher = state.copy(user = User("Teacher", "teacher@scope-1.example.test", AccountType.Teacher),
            studentRecordId = null, studentRecords = mapOf("ahmed" to StudentLearningRecord(emptySet(), ProductState())))
        assertEquals(0, LearningRules.topic(teacher, "limits", "ahmed").lessons)
    }

    @Test fun searchSavesActualQueriesDeduplicatesBoundsAndIgnoresBlanks() {
        val repo = TestLearningRepository()
        val app = AppViewModel(repo)
        app.search("  نهايات  ")
        app.search("derivatives")
        app.search("نهايات")
        app.search("   ")
        assertEquals(listOf("نهايات", "derivatives"), repo.state.value.product.searches)
        repeat(12) { app.search("query-$it") }
        assertEquals(8, repo.state.value.product.searches.size)
        assertEquals("query-11", repo.state.value.product.searches.first())
    }

    @Test fun labDescriptionsFollowTheSelectedLanguage() {
        assertTrue(getFunctionDescription("x^2", false).contains("minimum"))
        assertTrue(getFunctionDescription("x^2", true).contains("الصغرى"))
        assertTrue(getFunctionDescription("3x+2", false).contains("root"))
    }
}
