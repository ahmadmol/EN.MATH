package com.ahmadmol.enmath

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Isolated debug memory fixtures, no device preferences. Compile-only in host QA. */
class Grade9PilotUiContractTest {
    @get:Rule val ui = createComposeRule()
    private val state = LearningState(user = User("UI FIXTURE", "", AccountType.Guest,
        "00000000-0000-0000-0000-000000000001", StudyStage.G9), completed = emptySet(), product = ProductState(), language = Language.English)

    @Test fun originalPilotCanRunFromQuestionToSavedResultWithoutRewardActions() {
        val prefs = Grade9PreviewPreferences()
        val repository = Grade9StudyRepository({ prefs }) { state }
        val vm = Grade9PracticeViewModel()
        var resultId by mutableStateOf<String?>(null)
        ui.setContent { ENMathTheme(ThemeMode.Light, Language.English, true, StudyStage.G9) { Surface {
            val id = resultId
            if (id == null) Grade9PracticeScreen(state, repository, vm) { path -> resultId = path.substringAfterLast('/') }
            else Grade9ResultScreen(state, repository, id, {})
        } } }
        Grade9Pilot.questions.forEachIndexed { index, q ->
            ui.onNodeWithTag("g9-answer").performScrollTo().performTextInput(if (index == 0) "6/8" else q.answer)
            ui.onNodeWithTag("g9-check").performScrollTo().performClick()
            if (index < Grade9Pilot.questions.lastIndex) ui.onNodeWithText("Next question").performScrollTo().performClick()
            else ui.onNodeWithTag("g9-finish").performScrollTo().performClick()
        }
        ui.onNodeWithTag("screen-g9-result").assertExists()
        ui.runOnIdle {
            val saved = (repository.read(StudentCatalog.scope(state)!!) as G9StudyRead.Ready).data.attempts.single().record
            assertEquals(3, saved.correct); assertEquals(4, saved.total)
            assertEquals("6/8", saved.answers[Grade9Pilot.questions.first().id])
            assertTrue(state.product.pendingGemEvents.isEmpty())
        }
    }

    @Test fun algebraPilotEntryUsesTheExistingLessonRoute() {
        var path: String? = null
        ui.setContent { ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) { Surface {
            Grade9SubjectScreen(state, false) { path = it }
        } } }
        ui.onNodeWithTag("lesson/${Grade9Pilot.lessonId}").performScrollTo().performClick()
        ui.runOnIdle { assertEquals("lesson/${Grade9Pilot.lessonId}", path) }
    }
}
