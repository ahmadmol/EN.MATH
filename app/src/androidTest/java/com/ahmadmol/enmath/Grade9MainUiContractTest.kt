package com.ahmadmol.enmath

import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.student.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** No preferences or user storage: compile-only until an authorized device UI run. */
class Grade9MainUiContractTest {
    @get:Rule val ui = createComposeRule()
    private val state = LearningState(user = User("نور", "", AccountType.Guest,
        localId = "00000000-0000-0000-0000-000000000001", studyStage = StudyStage.G9), product = ProductState())

    @Test fun everyGrade9StationOpensItsExactSourceReference() {
        var opened: String? = null
        ui.setContent { ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) { Surface {
            Grade9LearningPath(state, { opened = it }, LocalGemsUiState(), null, null)
        } } }
        SourceCurricula.forStage(StudyStage.G9).researches.forEach { research ->
            ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("station-${research.id}"))
            ui.onNodeWithTag("station-${research.id}").performClick()
            ui.runOnIdle { assertEquals(research.route, opened) }
        }
        ui.onAllNodesWithTag("complete-lesson").assertCountEquals(0)
        ui.onAllNodesWithTag("check-answer").assertCountEquals(0)
    }

    @Test fun grade9HomeHasTheSameAccessibleTabSemanticsAsTheOtherFourTabs() {
        var opened: String? = null
        ui.setContent { ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
            Grade9BottomNav(Routes.Home) { opened = it }
        } }
        ui.onNodeWithTag("nav-home").assertIsSelected().performClick()
        ui.runOnIdle { assertEquals(Routes.Home, opened) }
        Routes.grade9Tabs.filter { it != Routes.Home }.forEach { route ->
            ui.onNodeWithTag("nav-$route").assertIsNotSelected().performClick()
            ui.runOnIdle { assertEquals(route, opened) }
        }
        ui.onAllNodesWithTag("nav-solver").assertCountEquals(0)
        ui.onAllNodesWithTag("nav-explore").assertCountEquals(0)
    }
}
