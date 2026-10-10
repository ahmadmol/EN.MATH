package com.ahmadmol.enmath

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmadmol.enmath.core.data.LocalDemoRepository
import com.ahmadmol.enmath.core.model.*
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource

/** Visual reference QA: ONLY run on the dedicated emulator (resets demo preferences). */
class ReferenceScreensTest {
    @get:Rule(order = 0)
    val seed =
        object : ExternalResource() {
            override fun before() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("enmath_demo", 0).edit().clear().commit()
                LocalDemoRepository(context).apply {
                    signIn(User("أحمد محمد", "ahmed@example.com", AccountType.Student))
                    setTheme(ThemeMode.Dark)
                    setLanguage(Language.Arabic)
                    finishOnboarding()
                }
            }
        }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()

    private fun click(tag: String) {
        ui.onNodeWithTag(tag).performClick()
        ui.waitForIdle()
    }

    private fun shot(name: String) {
        ui.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val width = context.resources.configuration.screenWidthDp
        val file = File(context.getExternalFilesDir("reference-screenshots"), "$width-$name.png")
        file.parentFile?.mkdirs()
        file.outputStream().use {
            ui.onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun waitHome() {
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("screen-home").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertNative(view: View) {
        assertFalse(
            "Reference screen must contain no WebView",
            view.javaClass.name.contains("WebView"),
        )
        if (view is ViewGroup) for (i in 0 until view.childCount) assertNative(view.getChildAt(i))
    }

    @Test
    fun referenceScreensAndInteractions() {
        waitHome()
        shot("home")
        click("nav-study")
        shot("study-part-1")
        click("reference-book-2")
        shot("study-part-2")
        click("nav-solver")
        shot("solver-top")
        ui.runOnIdle { assertNative(ui.activity.window.decorView) }
        ui.onAllNodesWithText("x² − 4").onFirst().assertExists()
        ui.onNodeWithTag("screen-solver").performTouchInput { swipeUp() }
        shot("solver-steps")
        ui.onNodeWithTag("screen-solver").performTouchInput { swipeUp() }
        shot("solver-result")
        click("nav-explore")
        shot("graph")
        click("graph-function-2")
        click("graph-control-1")
        click("graph-control-2")
        click("nav-profile")
        shot("profile")
        ui.onNodeWithText("English").performClick()
        ui.waitForIdle()
        shot("profile-en")
        ui.onNodeWithText("العربية").performClick()
        ui.waitForIdle()
        click("nav-study")
        ui.onNodeWithText("الجزء الثاني: الجبر والهندسة الفضائية والاحتمالات").assertExists()
        click("nav-home")
        ui.onNodeWithTag("screen-home").assertExists()
    }

    @Test
    fun alternateSolutionAndMathKeyboard() {
        waitHome()
        click("nav-solver")
        click("method-hopital")
        ui.onNodeWithTag("screen-solver").performTouchInput { swipeUp() }
        ui.onAllNodesWithText("2 × 2 = 4").onFirst().assertExists()
        ui.onNodeWithTag("screen-solver").performTouchInput { swipeUp() }
        ui.onAllNodesWithText("ln(x)").onFirst().performScrollTo().performClick()
        ui.onNodeWithTag("reference-math-input").performScrollTo().assertTextContains("ln(x)")
    }
}
