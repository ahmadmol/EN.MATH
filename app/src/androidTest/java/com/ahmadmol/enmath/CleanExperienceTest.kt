package com.ahmadmol.enmath

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmadmol.enmath.core.data.LocalDemoRepository
import com.ahmadmol.enmath.core.model.*
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource

/** Dedicated emulator only: seeds a logged-out demo session. */
class CleanExperienceTest {
    @get:Rule(order = 0)
    val seed =
        object : ExternalResource() {
            override fun before() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("enmath_demo", 0).edit().clear().commit()
                LocalDemoRepository(context).apply {
                    finishOnboarding()
                    setTheme(ThemeMode.Dark)
                    setLanguage(Language.Arabic)
                }
            }
        }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()

    private fun click(tag: String) {
        ui.onNodeWithTag(tag).performClick()
        ui.waitForIdle()
    }

    private fun scrollClick(tag: String) {
        ui.onNodeWithTag(tag).performScrollTo().performClick()
        ui.waitForIdle()
    }

    private fun waitTag(tag: String) {
        ui.waitUntil(15_000) { ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun input(tag: String, value: String) {
        ui.onNodeWithTag(tag).performScrollTo().performTextReplacement(value)
        ui.runOnIdle {
            val manager =
                ui.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            manager.hideSoftInputFromWindow(ui.activity.window.decorView.windowToken, 0)
        }
        android.os.SystemClock.sleep(350)
        ui.waitForIdle()
    }

    private fun shot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file =
            File(
                context.getExternalFilesDir("clean-ui-screenshots"),
                "${context.resources.configuration.screenWidthDp}-$name.png",
            )
        file.parentFile?.mkdirs()
        file.outputStream().use {
            ui.onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test
    fun loginValidationRolesAndSignOut() {
        waitTag("screen-started")
        scrollClick("role-student")
        shot("student-login")
        scrollClick("login-submit")
        ui.onNodeWithTag("auth-error").assertExists()
        input("auth-email", "ahmed@example.com")
        input("auth-password", "demo-pass-123")
        scrollClick("login-submit")
        waitTag("screen-home")
        ui.onNodeWithTag("nav-home").assertIsSelected()
        val bounds = ui.onNodeWithTag("nav-home").fetchSemanticsNode().boundsInRoot
        val density = ui.activity.resources.displayMetrics.density
        assertTrue(bounds.height / density >= 48f)
        shot("student-home")
        click("nav-profile")
        ui.onNodeWithText("ahmed").assertExists()
        scrollClick("logout")
        click("dialog-confirm")
        waitTag("screen-started")
        scrollClick("role-teacher")
        shot("teacher-login")
        input("auth-email", "sara@example.com")
        input("auth-password", "demo-pass-123")
        scrollClick("login-submit")
        waitTag("screen-teacher-reference")
        ui.onNodeWithTag("teacher-nav-teacher").assertIsSelected()
        shot("teacher-dashboard")
        click("teacher-nav-teacher/profile")
        scrollClick("logout")
        click("dialog-confirm")
        waitTag("screen-started")
        scrollClick("role-student")
        assertTrue(
            ui.onNodeWithTag("auth-password")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.EditableText]
                .text
                .isEmpty()
        )
    }
}
