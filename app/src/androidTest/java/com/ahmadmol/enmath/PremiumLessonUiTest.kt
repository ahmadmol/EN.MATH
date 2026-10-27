package com.ahmadmol.enmath

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.semantics.SemanticsActions
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.model.Language
import com.ahmadmol.enmath.core.model.ThemeMode
import com.ahmadmol.enmath.features.courses.interactive.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

/** Run on a USB physical device only. No emulator is required or launched by this suite. */
@RunWith(Parameterized::class)
class PremiumLessonUiTest(private val mode: ThemeMode, private val language: Language, private val fontScale: Float) {
    @get:Rule val ui = createComposeRule()
    private val state = LearningState()
    private val blocks = LimitsLessonSequence.blocks(state)
    private val q = blocks[2].question()!!
    private val vm = InteractiveLessonViewModel(SavedStateHandle(mapOf("lesson-step" to 2)))

    private fun render() {
        ui.setContent {
            ENMathTheme(mode, language) {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                    InteractiveLimitsScreen(state, vm, {}, {})
                }
            }
        }
    }

    private fun shot(state: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir("premium-lesson-qa"), "$mode-$language-$fontScale-$state.png")
        file.parentFile?.mkdirs()
        file.outputStream().use { output ->
            ui.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    @Test fun choiceHintVerificationAndBackReview() {
        render()
        ui.onNodeWithTag("lesson-check").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithTag("lesson-continue").assertIsNotEnabled()
        ui.onNodeWithTag("lesson-block-progress").performScrollTo()
        shot("before-selection")
        ui.onNodeWithTag("lesson-option-2").performScrollTo().performClick().assertIsSelected()
        ui.onNodeWithTag("lesson-option-${q.answer}").performScrollTo().performClick().assertIsSelected()
        ui.onNodeWithTag("lesson-option-2").assertIsNotSelected()
        shot("selected")
        ui.onNodeWithTag("lesson-hint").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(q.answer, vm.answers.value[q.id]) }
        shot("hint")
        ui.onNodeWithTag("lesson-check").performScrollTo().assertIsEnabled().performClick()
        ui.onNodeWithTag("answer-feedback").performScrollTo().assertExists()
        shot("verified")
        ui.onNodeWithTag("lesson-option-${q.answer}").assertIsNotEnabled()
        ui.onNodeWithTag("lesson-continue").performScrollTo().performClick()
        ui.onNodeWithTag("lesson-previous").performScrollTo().performClick()
        ui.onNodeWithTag("answer-feedback").performScrollTo().assertExists()
        ui.runOnIdle { assertTrue(vm.checked.value); assertEquals(q.answer, vm.answers.value[q.id]) }
    }

    @Test fun explorationSliderHoleAndNavigationRetainValue() {
        vm.move(1, blocks)
        render()
        ui.onNodeWithTag("limit-visual").performScrollTo().assertIsDisplayed()
        shot("exploration-graph")
        ui.onNodeWithTag("limit-slider").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(3f) }
        ui.onNodeWithTag("limit-undefined").performScrollTo().assertIsDisplayed()
        ui.runOnIdle { assertEquals(3f, vm.explorationX.value, 0f) }
        shot("exploration-hole")
        ui.onNodeWithTag("limit-slider").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(3.25f) }
        ui.onNodeWithTag("limit-undefined").assertDoesNotExist()
        ui.onNodeWithTag("lesson-continue").performScrollTo().performClick()
        ui.onNodeWithTag("lesson-previous").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(3.25f, vm.explorationX.value, 0f) }
        ui.onNodeWithTag("limit-slider").performScrollTo().assertIsDisplayed()
        shot("exploration-restored")
    }

    @Test fun longMathRetainsNativeRendererAndChoiceOrder() {
        val longQuestion = q.copy(expression = "lim x→3 (x + 2) + " + "(x² + 1) + ".repeat(20) + "0",
            options = q.options.map { "$it + " + "0 + ".repeat(20) + "0" })
        ui.setContent {
            ENMathTheme(mode, language) {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                    LessonPage("long-question") {
                        LessonQuestion(longQuestion, "", {}, false, false, true)
                    }
                }
            }
        }
        ui.onAllNodesWithTag("math-render").assertCountEquals(1 + longQuestion.options.size)
        longQuestion.options.forEach { option -> ui.onNodeWithTag("answer-$option").performScrollTo().assertExists() }
        shot("long-math")
    }

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}-{1}-font{2}")
        fun configurations() = listOf(ThemeMode.Light, ThemeMode.Dark).flatMap { mode ->
            listOf(Language.Arabic, Language.English).flatMap { language ->
                listOf(1f, 2f).map { scale -> arrayOf<Any>(mode, language, scale) }
            }
        }
    }
}
