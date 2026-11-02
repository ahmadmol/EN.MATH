package com.ahmadmol.enmath

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Pure UI fixtures only. Does not seed, clear, open or migrate app preferences. Compile is not a run. */
class SourceStudentUiContractTest {
    @get:Rule val ui = createComposeRule()
    @Test fun sourceResearchHasNoLearningCompletionOrRewardActionAcrossThemesAndFontScales() {
        var stage by mutableStateOf(StudyStage.G9)
        var language by mutableStateOf(Language.Arabic)
        var theme by mutableStateOf(ThemeMode.Light)
        var scale by mutableFloatStateOf(1f)
        ui.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                val state = LearningState(user=User("Fixture", "", AccountType.Guest,
                    localId="00000000-0000-0000-0000-000000000001", studyStage=stage),
                    language=language, product=ProductState(), completed=emptySet())
                ENMathTheme(theme,language,true,stage) { Surface {
                    key(stage,language,theme,scale) {
                        SourceResearchScreen(SourceCurricula.forStage(stage).researches.first().id,state,null,null,{})
                    }
                } }
            }
        }
        StudyStage.entries.forEach { nextStage -> Language.entries.forEach { nextLanguage ->
            listOf(ThemeMode.Light,ThemeMode.Dark).forEach { nextTheme -> listOf(1f,1.5f,2f).forEach { nextScale ->
                ui.runOnIdle { stage=nextStage;language=nextLanguage;theme=nextTheme;scale=nextScale }
                ui.waitForIdle()
                ui.onAllNodesWithTag("complete-lesson").assertCountEquals(0)
                ui.onAllNodesWithTag("check-answer").assertCountEquals(0)
                ui.onAllNodes(hasText("",substring=true),useUnmergedTree=true).fetchSemanticsNodes().forEach { node ->
                    val results=mutableListOf<TextLayoutResult>()
                    node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
                    results.forEach { assertFalse(it.layoutInput.text.text,it.didOverflowHeight) }
                }
            } }
        } }
    }
    @Test fun sourceUnitClickUsesExactResearchId() {
        var clicked: String? = null
        val catalog=SourceCurricula.forStage(StudyStage.BAC); val unit=catalog.books[1].units.first()
        ui.setContent { ENMathTheme(ThemeMode.Light,Language.Arabic,true,StudyStage.BAC) { Surface {
            SourceUnitScreen(StudyStage.BAC,unit.id,{ clicked=it })
        } } }
        val research=unit.researches.first()
        ui.onNodeWithTag("topic-lesson-${research.id}").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(research.route,clicked) }
    }
}
