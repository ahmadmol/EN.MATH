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
import org.junit.Assert.assertEquals
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
        ui.waitUntil(15_000) { ui.onAllNodesWithTag("screen-home").fetchSemanticsNodes().isNotEmpty() }
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
        ui.onNodeWithText("54 طالباً").assertExists()
        ui.runOnIdle { assertNative(ui.activity.window.decorView) }
        click("teacher-nav-teacher/reference/home")
        ui.onNodeWithTag("screen-home").assertExists()
        shot("teacher-home")
        ui.onNodeWithText("عرض المنهج").performScrollTo().performClick()
        ui.waitForIdle()
        shot("teacher-study-1")
        click("reference-book-2")
        shot("teacher-study-2")
        ui.onNodeWithText("حساب محدد ومقلوب مصفوفة من المرتبة الثانية والثالثة").performClick()
        ui.onNodeWithText("معاينة الدرس").assertExists()
        click("teacher-preview-dismiss")
        ui.onNodeWithText("العمليات على المصفوفات وحساب المقلوب").performClick()
        ui.onNodeWithText("حساب محدد ومقلوب مصفوفة من المرتبة الثانية والثالثة")
            .assertDoesNotExist()
        ui.onNodeWithText("العمليات على المصفوفات وحساب المقلوب").performClick()
        click("teacher-nav-teacher")
        click("teacher-nav-teacher/reference/study")
        ui.onNodeWithText("الجزء الثاني: الجبر والهندسة الفضائية والاحتمالات").assertExists()
        click("teacher-nav-teacher/reference/solver")
        ui.onAllNodesWithText("الحل المفصل").onFirst().assertExists()
        click("teacher-preview-dismiss")
        click("teacher-nav-teacher/reference/lab")
        click("teacher-preview-dismiss")
        click("teacher-nav-teacher")
        ui.onNodeWithTag("screen-teacher-reference").assertExists()
    }

    @Test
    fun localClassAndAssignmentInteractions() {
        waitDashboard()
        click("teacher-class-0")
        ui.onNodeWithText(
                "معاينة تجريبية: 28 طالباً · الإتقان 84%\nالتكامل بالتجزئة وحساب المساحات"
            )
            .assertExists()
        click("teacher-preview-dismiss")
        ui.onNodeWithTag("teacher-support").performScrollTo().performClick()
        click("teacher-preview-dismiss")
        ui.onNodeWithTag("teacher-create-assignment").performScrollTo().performClick()
        ui.onNodeWithTag("teacher-assignment-preview").assertIsNotEnabled()
        ui.onNodeWithTag("teacher-assignment-title").performTextInput("واجب النهايات")
        click("teacher-assignment-class-1")
        click("teacher-assignment-preview")
        ui.onNodeWithTag("teacher-assignments-preview").assertExists()
        ui.onNodeWithText("معاينة تجريبية: واجب النهايات · BAC-SCI-2026B (لم تُنشر)").assertExists()
        shot("teacher-assignment-preview")
        ui.activityRule.scenario.recreate()
        waitDashboard()
        ui.onNodeWithText("معاينة تجريبية: واجب النهايات · BAC-SCI-2026B (لم تُنشر)").assertExists()
        click("teacher-tab-classes")
        ui.onNodeWithTag("teacher-class-1").assertExists()
    }
}
