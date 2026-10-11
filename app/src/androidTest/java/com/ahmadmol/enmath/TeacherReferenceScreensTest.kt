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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource

/** Run only on the QA emulator: resets its local demo session, never the user's phone. */
class TeacherReferenceScreensTest {
    @get:Rule(order = 0)
    val seed =
        object : ExternalResource() {
            override fun before() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("enmath_demo", 0).edit().clear().commit()
                LocalDemoRepository(context).apply {
                    signIn(User("سارة الأحمد", "sara@example.com", AccountType.Teacher))
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

    @Test
    fun demoRoleSwitchPreservesDataAndSession() {
        waitDashboard()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val before = LocalDemoRepository(context).state.value
        ui.onNodeWithTag("demo-switch-role").performScrollTo().performClick()
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("screen-home").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(AccountType.Student, LocalDemoRepository(context).state.value.user?.type)
        click("nav-profile")
        ui.onNodeWithTag("demo-switch-role").performScrollTo().performClick()
        waitDashboard()
        val after = LocalDemoRepository(context).state.value
        assertEquals(AccountType.Teacher, after.user?.type)
        assertEquals(before.user?.name, after.user?.name)
        assertEquals(before.user?.email, after.user?.email)
        assertEquals(before.completed, after.completed)
        assertEquals(before.product, after.product)
        assertEquals(before.language, after.language)
        assertEquals(before.theme, after.theme)
        ui.activityRule.scenario.recreate()
        waitDashboard()
        assertEquals(AccountType.Teacher, LocalDemoRepository(context).state.value.user?.type)
    }

    private fun waitDashboard() {
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("screen-teacher-reference").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun shot(name: String) {
        ui.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val width = context.resources.configuration.screenWidthDp
        val file =
            File(context.getExternalFilesDir("teacher-reference-screenshots"), "$width-$name.png")
        file.parentFile?.mkdirs()
        file.outputStream().use {
            ui.onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun assertNative(view: View) {
        assertFalse(
            "Teacher reference must be native Compose",
            view.javaClass.name.contains("WebView"),
        )
        if (view is ViewGroup) for (i in 0 until view.childCount) assertNative(view.getChildAt(i))
    }

    @Test
    fun referenceNavigationAndCurriculum() {
        waitDashboard()
        shot("teacher-dashboard")
        ui.runOnIdle { assertNative(ui.activity.window.decorView) }
        ui.onNodeWithTag("teacher-open-study").performScrollTo().performClick()
        ui.waitForIdle()
        shot("teacher-study-1")
        ui.onNodeWithTag("reference-book-2").performScrollTo().performClick()
        shot("teacher-study-2")
        ui.onNodeWithText("حساب محدد ومقلوب مصفوفة من المرتبة الثانية والثالثة")
            .performScrollTo()
            .performClick()
        ui.waitForIdle()
        ui.onNodeWithTag("screen-teacher-lesson").assertExists()
        ui.runOnIdle { assertNative(ui.activity.window.decorView) }
        click("teacher-nav-teacher/resources")
        ui.onNodeWithTag("screen-resources").assertExists()
        ui.onNodeWithTag("teacher-resource-solver").performScrollTo().performClick()
        ui.onNodeWithTag("screen-solver").assertExists()
        click("teacher-nav-teacher/resources")
        ui.onNodeWithTag("teacher-resource-lab").performScrollTo().performClick()
        ui.onNodeWithTag("graph-function-0").assertExists()
        click("teacher-nav-teacher")
        ui.onNodeWithTag("screen-teacher-reference").assertExists()
        click("teacher-nav-teacher/profile")
        ui.onNodeWithText("سارة الأحمد").assertExists()
        shot("teacher-account")
    }

    @Test
    fun localClassAndAssignmentInteractions() {
        waitDashboard()
        click("teacher-nav-teacher/classes")
        click("create-class")
        ui.onNodeWithTag("class-name").performTextInput("شعبة اختبار التنقل")
        click("class-save")
        ui.onNodeWithText("شعبة اختبار التنقل").assertExists()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertFalse(
            LocalDemoRepository(context).state.value.product.classes.none {
                it.name.ar == "شعبة اختبار التنقل"
            }
        )
        click("teacher-nav-teacher/assignments")
        ui.onNodeWithTag("screen-teacher-assignments").assertExists()
        click("create-assignment")
        ui.onNodeWithTag("screen-builder").assertExists()
    }
}
