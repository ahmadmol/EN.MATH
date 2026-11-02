package com.ahmadmol.enmath

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.design.tokens.ResourcePackPresentation
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.DualStageHome
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Component-only future QA: no preferences, device shell, clearing, sign-in, or ledger access.
 * Compiled during host verification; every configuration remains NOT_RUN until device QA.
 */
@RunWith(Parameterized::class)
class StudentExecution2ScreensTest(private val stage: StudyStage, private val theme: ThemeMode,
    private val language: Language, private val fontScale: Float) {
    @get:Rule val compose=createComposeRule()
    @Test fun stageHomeAndNavigationActionRemainReachableWithLargeText() {
        val s=LearningState(user=User("Fixture","",AccountType.Guest,
            "00000000-0000-0000-0000-000000000001",stage),completed=emptySet(),product=ProductState(),language=language,theme=theme)
        compose.setContent {
            ENMathTheme(theme,language,student=true) {
                val density=LocalDensity.current.density
                CompositionLocalProvider(LocalDensity provides Density(density,fontScale)) {
                    ResourcePackPresentation(enabled=true) { DualStageHome(s,{},LocalGemsUiState(),null) }
                }
            }
        }
        val heading=if(stage==StudyStage.G9) {
            if(language==Language.Arabic) "الصف التاسع" else "Grade 9"
        } else if(language==Language.Arabic) "البكالوريا العلمي" else "Scientific Baccalaureate"
        compose.onNodeWithText(heading).assertIsDisplayed()
        compose.onNodeWithTag("home-all-progress").performScrollTo().assertIsDisplayed()
    }
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}-{2}-{3}") fun configurations():List<Array<Any>> =
            StudyStage.entries.flatMap { stage -> listOf(ThemeMode.Light,ThemeMode.Dark).flatMap { theme ->
                Language.entries.flatMap { language -> listOf(1f,1.5f,2f).map { font -> arrayOf(stage,theme,language,font) } }
            } }
    }
}
