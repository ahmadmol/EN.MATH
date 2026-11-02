package com.ahmadmol.enmath

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.explore.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-w393dp-h850dp")
@LooperMode(LooperMode.Mode.PAUSED)
class GraphPreparationHostTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    @Test fun changingViewportAndVisibilityDoesNotParseCurvesAgain() {
        var parses = 0
        val delegate = DemoGraphEngine()
        val engine = object : GraphEngine {
            override fun evaluate(expression: String, x: Double) = error("Sampling must use the prepared function")
            override fun prepare(expression: String): GraphFunction? { parses++; return delegate.prepare(expression) }
        }
        val zoom = mutableFloatStateOf(1f)
        val hidden = mutableStateOf(emptyList<String>())
        val curves = listOf("x^2", "sin(x)")
        ui.setContent {
            ENMathTheme(ThemeMode.Light, Language.English, true, StudyStage.BAC) { Surface {
                InteractiveGraphCanvas(curves, hidden.value, listOf(Color.Blue, Color.Green), engine,
                    zoom.floatValue, Offset.Zero, true, true, { _, _ -> }, false)
            } }
        }
        ui.waitForIdle()
        assertEquals(2, parses)
        ui.runOnIdle { zoom.floatValue = 2f; hidden.value = listOf("sin(x)") }
        ui.waitForIdle()
        assertEquals("Viewport interactions must reuse both compiled expressions", 2, parses)
    }
}
