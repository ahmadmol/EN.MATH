package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.*
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence
import org.junit.Assert.*
import org.junit.Test
import androidx.compose.ui.graphics.luminance
import com.ahmadmol.enmath.core.design.studentColorScheme

class StudentCompletionRegressionTest {
    private fun student(email: String) = LearningState(user = User("Ahmed", email, AccountType.Student))

    @Test fun unownedLegacyAliasCannotOverrideFullEmailIdentity() {
        val state = student("different@example.com").copy(studentRecordId = "ahmed")
        assertEquals("student:different@example.com", state.activeStudentId)
    }

    @Test fun recordedLegacyOwnerKeepsItsExistingClassroomIdWithoutRekeyingData() {
        val state = student("different@example.com").copy(studentRecordId = "ahmed",
            studentRecordOwners = mapOf("ahmed" to "student:different@example.com"))
        assertEquals("ahmed", state.activeStudentId)
        assertEquals("student:ahmed@example.com", state.copy(user = User("Seed", "ahmed@example.com", AccountType.Student), studentRecordId = null).activeStudentId)
    }

    @Test fun unrelatedAccountsCannotReuseARecordedAlias() {
        val owners = mapOf("legacy" to "student:first@scope-1.example.test")
        assertEquals("legacy", student(" FIRST@scope-1.example.test ").copy(studentRecordId = "legacy", studentRecordOwners = owners).activeStudentId)
        assertEquals("student:second@scope-1.example.test", student("second@scope-1.example.test").copy(studentRecordId = "legacy", studentRecordOwners = owners).activeStudentId)
    }

    @Test fun aliasesDoNotAffectGuestTeacherOrSignedOutIdentities() {
        val state = student("a@scope-1.example.test").copy(studentRecordId = "ahmed", studentRecordOwners = mapOf("ahmed" to "student:a@scope-1.example.test"))
        assertEquals("anonymous", state.copy(user = null).activeStudentId)
        assertEquals("guest", state.copy(user = User("A", "a@scope-1.example.test", AccountType.Guest)).activeStudentId)
        assertEquals("teacher:a@scope-1.example.test", state.copy(user = User("A", "a@scope-1.example.test", AccountType.Teacher)).activeStudentId)
    }

    @Test fun interactiveLessonHasAnOuterHeaderAndRegularLessonOwnsItsHeader() {
        assertFalse(studentOwnsHeader(Routes.Lesson, LimitsLessonSequence.lessonId))
        assertTrue(studentOwnsHeader(Routes.Lesson, "derivatives-rule"))
        assertTrue(studentOwnsHeader(Routes.Topic, "limits"))
        assertTrue(studentOwnsHeader(Routes.Progress, null))
        assertTrue(studentOwnsHeader("solver/history", null))
        listOf("practice/{id}", "practice/{id}/result", "assignments/{id}/work", "gems/review", "search", "notifications")
            .forEach { assertFalse(it, studentOwnsHeader(it, null)) }
    }

    @Test fun probabilityFilterAndQueryAreAppliedTogetherAndUnknownFilterIsSafe() {
        val probability = HistoryItem("p", "P(evenonfairdie)", "probability", 1)
        val limits = HistoryItem("l", "lim(x)", "limits", 2)
        val history = listOf(probability, limits)
        assertTrue("probability" in LearningRules.historyTopicIds)
        assertEquals(listOf(probability), LearningRules.solutionHistory(history, "probability", "even"))
        assertTrue(LearningRules.solutionHistory(history, "probability", "lim").isEmpty())
        assertEquals(history, LearningRules.solutionHistory(history, "obsolete", ""))
        assertEquals(listOf(limits), LearningRules.solutionHistory(history, "all", "LIM"))
        assertEquals(2, history.size)
    }

    @Test fun centralStudentTextPairsMeetNormalTextContrastInBothThemes() {
        listOf(false, true).forEach { dark ->
            val scheme = studentColorScheme(dark)
            listOf(scheme.onSurface to scheme.surface, scheme.onSurfaceVariant to scheme.surface,
                scheme.onPrimary to scheme.primary, scheme.onPrimaryContainer to scheme.primaryContainer,
                scheme.onSecondaryContainer to scheme.secondaryContainer).forEach { (text, background) ->
                val contrast = (maxOf(text.luminance(), background.luminance()) + .05f) /
                    (minOf(text.luminance(), background.luminance()) + .05f)
                assertTrue("dark=$dark contrast=$contrast", contrast >= 4.5f)
            }
        }
    }
}
