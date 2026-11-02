package com.ahmadmol.enmath

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.library.Grade9FileScreen
import com.ahmadmol.enmath.features.student.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Pure memory UI fixtures. Host QA compiles these; it does not run an emulator or access user data. */
class Grade9Phase3AccessibilityTest {
    @get:Rule val ui = createComposeRule()
    private fun fixtureState(id: String = "00000000-0000-0000-0000-000000000001") = LearningState(
        user = User("FIXTURE", "", AccountType.Guest, id, StudyStage.G9), completed = emptySet(), product = ProductState())

    @Test fun fractionInputKeepsLtrMathInsideArabicAtLargeType() {
        ui.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) { Surface {
                    G9FractionAnswerField("٣/٤", {}, submit = {})
                } }
            }
        }
        val nodes = ui.onAllNodes(hasSetTextAction(), useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue(nodes.isNotEmpty())
        val layouts = mutableListOf<TextLayoutResult>()
        nodes.forEach { node ->
            node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        }
        assertTrue("The field must expose a text layout", layouts.isNotEmpty())
        layouts.forEach { assertEquals(TextDirection.Ltr, it.layoutInput.style.textDirection) }
    }

    @Test fun checkedCorrectionExposesAPoliteLiveRegion() {
        val state = fixtureState(); val prefs = Grade9PreviewPreferences()
        val repository = Grade9StudyRepository({ prefs }) { state }
        val q = Grade9Pilot.questions.first()
        repository.saveDraft(StudentCatalog.scope(state)!!, G9PracticeDraft("ui-correction", mapOf(q.id to "6/8"), setOf(q.id)))
        val vm = Grade9PracticeViewModel()
        ui.setContent { ENMathTheme(ThemeMode.Light, Language.English, true, StudyStage.G9) { Surface {
            Grade9PracticeScreen(state, repository, vm, {})
        } } }
        ui.onNodeWithText("Needs review").performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test fun fileDetailsNeverKeepThePreviousProfileNameAfterSwitch() {
        var state by mutableStateOf(fixtureState())
        val stores = mutableMapOf<String, Grade9PreviewPreferences>()
        val repository = Grade9StudyRepository({ name -> stores.getOrPut(name) { Grade9PreviewPreferences() } }) { state }
        repository.addFile(StudentCatalog.scope(state)!!,
            StudentFile("private", "content://fixture/document", "PRIVATE PROFILE A.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        ui.setContent { ENMathTheme(ThemeMode.Light, Language.English, true, StudyStage.G9) { Surface {
            Grade9FileScreen(state, repository, "private")
        } } }
        ui.onNodeWithText("PRIVATE PROFILE A.docx").assertExists()
        ui.runOnIdle { state = fixtureState("00000000-0000-0000-0000-000000000002").copy(generation = 1) }
        ui.onAllNodesWithText("PRIVATE PROFILE A.docx").assertCountEquals(0)
        ui.onNodeWithText("This document is not saved in the current local file.").assertExists()
    }
}
