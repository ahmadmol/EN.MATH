package com.ahmadmol.enmath

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.TestName

/** Always run on a dedicated emulator: preferences are seeded for a repeatable vertical slice. */
class RoleExperienceTest {
    private val mode =
        if (InstrumentationRegistry.getArguments().getString("theme") == "Light") ThemeMode.Light
        else ThemeMode.Dark
    @get:Rule(order = 0) val testName = TestName()
    @get:Rule(order = 1)
    val seed =
        object : ExternalResource() {
            override fun before() {
                check(
                    android.os.Build.MODEL.contains("sdk") ||
                        android.os.Build.FINGERPRINT.contains("generic")
                ) {
                    "Use a dedicated emulator; real phone data must not be reset."
                }
                val ctx = InstrumentationRegistry.getInstrumentation().targetContext
                ctx.getSharedPreferences("enmath_demo", 0).edit().clear().commit()
                LocalDemoRepository(ctx).apply {
                    finishOnboarding()
                    setTheme(mode)
                    setLanguage(Language.Arabic)
                    signIn(
                        if (testName.methodName.startsWith("teacher"))
                            User("سارة الأحمد", "sara@example.com", AccountType.Teacher)
                        else User("أحمد محمد", "ahmed@example.com", AccountType.Student)
                    )
                }
            }
        }
    @get:Rule(order = 2) val ui = createAndroidComposeRule<MainActivity>()

    private fun node(tag: String): SemanticsNodeInteraction {
        val n = ui.onNodeWithTag(tag)
        var parent = n.fetchSemanticsNode().parent
        while (parent != null) {
            if (parent.config.contains(SemanticsActions.ScrollBy)) {
                n.performScrollTo()
                break
            }
            parent = parent.parent
        }
        return n
    }

    private fun click(tag: String) {
        node(tag).performClick()
        ui.waitForIdle()
    }

    private fun input(tag: String, value: String) {
        node(tag).performTextReplacement(value)
        ui.runOnIdle {
            val imm =
                ui.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(ui.activity.window.decorView.windowToken, 0)
        }
        android.os.SystemClock.sleep(350)
        ui.waitForIdle()
    }

    private fun waitTag(tag: String) {
        ui.waitUntil(15_000) { ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun shot(name: String) {
        ui.waitForIdle()
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val file =
            File(
                ctx.getExternalFilesDir("role-experience-screenshots"),
                "${ctx.resources.configuration.screenWidthDp}-${mode.name}-$name.png",
            )
        file.parentFile?.mkdirs()
        file.outputStream().use {
            ui.onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun native(view: View) {
        assertFalse(view.javaClass.name.contains("WebView"))
        if (view is ViewGroup) for (i in 0 until view.childCount) native(view.getChildAt(i))
    }

    @Test
    fun studentLearningAndTeacherEvidenceUseOneState() {
        waitTag("screen-home")
        shot("student-home")
        click("nav-study")
        click("study-limits")
        click("start-interactive-limits")
        waitTag("block-concept")
        node("lesson-continue").assertIsEnabled()
        click("lesson-continue")
        waitTag("block-visual")
        ui.onNodeWithTag("limit-slider").performSemanticsAction(SemanticsActions.SetProgress) {
            it(3f)
        }
        shot("interactive-limits-visual")
        ui.runOnIdle { native(ui.activity.window.decorView) }
        click("lesson-continue")
        waitTag("block-prediction")
        node("lesson-continue").assertIsNotEnabled()
        click("lesson-option-2")
        click("lesson-check")
        node("answer-feedback").assertExists()
        node("lesson-continue").assertIsNotEnabled()
        click("lesson-hint")
        click("lesson-option-5")
        click("lesson-check")
        shot("interactive-limits-feedback")
        click("lesson-continue")
        waitTag("block-understand")
        click("lesson-continue")
        waitTag("block-rule")
        click("lesson-continue")
        waitTag("block-example")
        shot("interactive-limits-worked")
        click("lesson-continue")
        waitTag("block-challenge")
        input("lesson-answer", "5")
        click("lesson-check")
        click("lesson-continue")
        waitTag("block-verify")
        click("lesson-option-false")
        click("lesson-check")
        click("lesson-continue")
        waitTag("block-remember")
        click("lesson-hint")
        click("lesson-continue")
        waitTag("block-checkpoint")
        input("lesson-answer", "4")
        ui.activityRule.scenario.recreate()
        waitTag("block-checkpoint")
        node("lesson-answer").assertTextContains("4")
        click("lesson-check")
        shot("interactive-limits-checkpoint")
        click("lesson-continue")
        waitTag("block-completion")
        click("complete-lesson")
        assertTrue(
            LimitsLessonSequence.lessonId in LocalDemoRepository(ui.activity).state.value.completed
        )
        click("lesson-practice")
        waitTag("screen-practice")
        shot("student-practice")
        ui.runOnIdle { native(ui.activity.window.decorView) }
        val questions =
            ProductSeed.sets
                .first { it.id == LimitsLessonSequence.practiceId }
                .questionIds
                .map { ProductSeed.question(it)!! }
        questions.forEachIndexed { index, q ->
            if (q.kind == QuestionKind.Choice || q.kind == QuestionKind.TrueFalse)
                click("answer-${q.answer}")
            else input("quiz-answer", q.answer)
            click("check-answer")
            if (index == 0) shot("student-practice-feedback")
            click("quiz-next")
        }
        waitTag("screen-practice-result")
        shot("practice-result")
        val after = LocalDemoRepository(ui.activity).state.value
        assertEquals(6, after.product.attempts.last().correct)
        val score = LearningRules.topic(after, "limits").score
        ui.onNodeWithTag("student-mastery-limits").assertTextContains("$score%")
        click("nav-profile")
        click("demo-switch-role")
        waitTag("screen-teacher-home")
        shot("teacher-home")
        click("teacher-class-class-a")
        waitTag("screen-class-details")
        shot("class-details")
        click("class-tab-1")
        input("teacher-student-search", "أحمد")
        click("teacher-student-ahmed")
        waitTag("screen-student-details")
        shot("student-details")
        node("teacher-mastery-limits").assertTextContains("$score%")
        ui.activityRule.scenario.recreate()
        waitTag("screen-student-details")
        node("teacher-mastery-limits").assertTextContains("$score%")
        click("back")
        waitTag("screen-class-details")
        input("teacher-student-search", "no-match")
        ui.onAllNodesWithTag("teacher-student-ahmed").assertCountEquals(0)
        click("class-tab-2")
        node("teacher-curriculum-limits").assertExists()
        click("teacher-nav-teacher")
        waitTag("screen-teacher-home")
        click("teacher-action-3")
        waitTag("screen-home")
        assertEquals(
            score,
            LearningRules.topic(LocalDemoRepository(ui.activity).state.value, "limits").score,
        )
    }

    @Test
    fun teacherProductivityFiltersAndEvidence() {
        waitTag("screen-teacher-home")
        shot("teacher-home-sara")
        click("teacher-class-class-a")
        waitTag("screen-class-details")
        shot("class-details-sara")
        click("teacher-action-0")
        ui.onNodeWithText("تم نسخ رمز الدعوة").assertExists()
        click("class-tab-1")
        click("student-filter-1")
        node("student-filter-1").assertIsSelected()
        click("student-filter-0")
        input("teacher-student-search", "أحمد")
        click("teacher-student-ahmed")
        waitTag("screen-student-details")
        shot("student-details-sara")
        val score =
            LearningRules.topic(LocalDemoRepository(ui.activity).state.value, "limits", "ahmed")
                .score
        node("teacher-mastery-limits").assertTextContains("$score%")
        click("back")
        click("class-tab-2")
        node("teacher-curriculum-limits").assertExists()
        shot("teacher-class-curriculum")
        click("class-tab-1")
        input("teacher-student-search", "student 12")
        click("teacher-student-student-12")
        waitTag("screen-student-details")
        ui.onNodeWithText("مراجعة آخر تسليم").assertIsNotEnabled()
    }

    @Test
    fun teacherToolsHistoryAndGraphStayInTeacherSession() {
        waitTag("screen-teacher-home")
        click("teacher-nav-teacher/resources")
        click("teacher-resource-solver")
        waitTag("screen-solver")
        input("solver-input", "2x+4=10")
        click("solve")
        waitTag("screen-solution")
        ui.onAllNodesWithText("x = 3").onFirst().assertExists()
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        waitTag("screen-solver")
        ui.onNodeWithText("سجل المسائل").performScrollTo().performClick()
        waitTag("screen-history")
        ui.onAllNodesWithText("2x+4=10").onFirst().assertExists()
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        waitTag("screen-solver")
        ui.onNodeWithText("استكشاف الرسوم").performScrollTo().performClick()
        waitTag("screen-explore")
        input("graph-input", "cos(x)")
        ui.onNodeWithText("إضافة دالة").performScrollTo().performClick()
        ui.onAllNodesWithText("y = cos(x)").onFirst().assertExists()
        click("graph-values-toggle")
        ui.onAllNodesWithText("y=cos(x)").onFirst().assertExists()
        shot("teacher-graph-values")
        assertEquals(AccountType.Teacher, LocalDemoRepository(ui.activity).state.value.user?.type)
    }
}
