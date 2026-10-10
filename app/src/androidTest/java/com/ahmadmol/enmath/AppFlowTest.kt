package com.ahmadmol.enmath

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
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
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class AppFlowTest {
    @get:Rule(order = 0)
    val clean =
        object : ExternalResource() {
            override fun before() {
                InstrumentationRegistry.getInstrumentation()
                    .targetContext
                    .getSharedPreferences("enmath_demo", 0)
                    .edit()
                    .clear()
                    .commit()
            }
        }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    @get:Rule(order = 2)
    val diagnostics =
        object : TestWatcher() {
            override fun failed(e: Throwable, d: Description) {
                runCatching { shot("failed-" + d.methodName) }
                android.util.Log.e("ENMathTest", ui.onAllNodes(isRoot()).onFirst().printToString())
            }
        }

    private fun SemanticsNodeInteraction.reveal(): SemanticsNodeInteraction {
        var parent = fetchSemanticsNode().parent
        while (parent != null) {
            if (parent.config.contains(SemanticsActions.ScrollBy)) {
                performScrollTo()
                break
            }
            parent = parent.parent
        }
        return this
    }

    private fun click(tag: String) {
        ui.onNodeWithTag(tag).reveal().performClick()
        ui.waitForIdle()
    }

    private fun text(value: String) {
        ui.onAllNodesWithText(value).onFirst().reveal().performClick()
        ui.waitForIdle()
    }

    private fun input(tag: String, value: String) {
        ui.onNodeWithTag(tag).reveal().performTextReplacement(value)
        ui.runOnIdle {
            val manager =
                ui.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            val windows =
                if (android.os.Build.VERSION.SDK_INT >= 29)
                    android.view.inspector.WindowInspector.getGlobalWindowViews()
                else listOf(ui.activity.window.decorView)
            windows.forEach { manager.hideSoftInputFromWindow(it.windowToken, 0) }
        }
        android.os.SystemClock.sleep(
            350
        ) // Wait for the system IME animation, outside Compose's clock.
        ui.waitForIdle()
    }

    private fun intro() {
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("intro-skip").fetchSemanticsNodes().isNotEmpty()
        }
        click("intro-skip")
    }

    private fun login(teacher: Boolean = false) {
        intro()
        click(if (teacher) "role-teacher" else "role-student")
        input("auth-email", if (teacher) "sara@example.com" else "ahmed@example.com")
        input("auth-password", "demo-pass-123")
        click("login-submit")
    }

    private fun shot(name: String) {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(ctx.getExternalFilesDir("product-screenshots"), "$name.png")
        file.parentFile?.mkdirs()
        ui.waitForIdle()
        android.os.SystemClock.sleep(300) // Include the asynchronous WebView math surface.
        val bitmap =
            requireNotNull(
                InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            )
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test
    fun studentRegistrationAndVerification() {
        intro()
        click("role-student")
        click("register")
        click("register-Student")
        input("auth-name", "أحمد محمد")
        input("auth-email", "ahmed@example.com")
        input("auth-password", "demo-pass-123")
        input("auth-confirm", "demo-pass-123")
        click("auth-terms")
        click("registration-submit")
        input("auth-code", "000000")
        click("verify-submit")
        ui.onNodeWithTag("auth-error").assertExists()
        input("auth-code", "123456")
        click("verify-submit")
        ui.onNodeWithTag("screen-home").assertExists()
    }

    @Test
    fun teacherRegistrationAndRecovery() {
        intro()
        click("role-teacher")
        click("register")
        click("register-Teacher")
        input("auth-name", "الأستاذة سارة")
        input("auth-email", "sara@example.com")
        input("teacher-subject", "الرياضيات")
        input("teacher-experience", "5")
        input("auth-password", "demo-pass-123")
        input("auth-confirm", "demo-pass-123")
        click("auth-terms")
        click("registration-submit")
        input("auth-code", "123456")
        click("verify-submit")
        ui.onNodeWithTag("screen-teacher").assertExists()
        click("nav-teacher/profile")
        click("logout")
        click("dialog-confirm")
        click("role-student")
        click("forgot")
        input("auth-email", "ahmed@example.com")
        click("forgot-submit")
        input("auth-code", "123456")
        click("verify-submit")
        input("auth-password", "new-demo-pass")
        input("auth-confirm", "new-demo-pass")
        click("new-password-submit")
        ui.onNodeWithTag("screen-login").assertExists()
    }

    @Test
    fun studyPracticeResultAndPersistence() {
        login()
        click("nav-study")
        click("book-book-1")
        click("topic-limits")
        click("lesson-limits-practice")
        shot("lesson-ar-light")
        click("complete-lesson")
        ui.onNodeWithTag("complete-lesson").assertIsNotEnabled()
        text("تدرّب على المهارة")
        click("answer-5")
        click("check-answer")
        click("quiz-next")
        input("quiz-answer", "5")
        click("check-answer")
        click("quiz-next")
        click("answer-true")
        click("check-answer")
        click("quiz-next")
        input("quiz-answer", "5")
        click("check-answer")
        click("quiz-next")
        click("answer-false")
        click("check-answer")
        click("quiz-next")
        input("quiz-answer", "4")
        click("check-answer")
        click("quiz-next")
        ui.onNodeWithTag("screen-practice-result").assertExists()
        shot("practice-result")
        val repo = LocalDemoRepository(ui.activity)
        assertTrue(repo.state.value.product.attempts.isNotEmpty())
        assertTrue("limits-practice" in repo.state.value.completed)
    }

    @Test
    fun solverUnknownHistoryAndGraph() {
        login()
        click("nav-solver")
        input("solver-input", "2x+4=10")
        click("solve")
        ui.onNodeWithTag("screen-solution").assertExists()
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("math-render").fetchSemanticsNodes().let { nodes ->
                nodes.isNotEmpty() &&
                    nodes.all {
                        it.config.getOrNull(
                            androidx.compose.ui.semantics.SemanticsProperties.StateDescription
                        ) == "math-ready"
                    }
            }
        }
        ui.onAllNodesWithText("x = 3").onFirst().assertExists()
        shot("solution-ar-light")
        click("bookmark")
        click("edit-problem")
        input("solver-input", "sin(9)+y")
        click("solve")
        ui.onNodeWithText("هذه المسألة غير مدعومة في الحل التجريبي بعد.").assertExists()
        click("edit-problem")
        text("سجل المسائل")
        ui.onAllNodesWithText("2x+4=10").onFirst().assertExists()
        click("back")
        text("استكشاف الرسوم")
        ui.onNodeWithTag("screen-explore").assertExists()
        shot("graph-ar-light")
    }

    @Test
    fun guestProtectionAndAdaptiveTabs() {
        intro()
        click("guest")
        shot("adaptive-home")
        listOf("study", "solver", "progress", "profile", "home").forEach {
            click("nav-$it")
            ui.onNodeWithTag("screen-$it").assertExists()
            if (it == "study" || it == "solver") shot("adaptive-$it")
        }
        text("مكتبتي")
        text("إنشاء حساب")
        ui.onNodeWithText("أنشئ حسابًا تجريبيًا").assertExists()
        text("متابعة كضيف")
    }

    @Test
    fun teacherCreatesClassAndAssignment() {
        login(true)
        shot("teacher-ar-light")
        click("nav-teacher/classes")
        click("create-class")
        input("class-name", "صف تجريبي جديد")
        click("class-save")
        ui.onNodeWithText("صف تجريبي جديد").assertExists()
        click("back")
        click("nav-teacher/assignments")
        click("create-assignment")
        input("builder-title", "اختبار مشتقات جديد")
        input("builder-description", "تدرّب ثم تحقق من النتيجة")
        click("builder-next")
        click("select-derivatives-q1")
        click("builder-next")
        click("builder-next")
        shot("assignment-builder")
        click("builder-next")
        ui.onNodeWithTag("screen-teacher-results").assertExists()
        val repo = LocalDemoRepository(ui.activity)
        assertTrue(repo.state.value.product.assignments.any { it.title.ar == "اختبار مشتقات جديد" })
        assertTrue(repo.state.value.product.classes.any { it.name.ar == "صف تجريبي جديد" })
    }

    @Test
    fun studentAssignmentSubmissionVisibleInRepository() {
        login()
        text("مراجعة الاشتقاق")
        click("assignment-work")
        click("answer-6")
        click("quiz-next")
        input("quiz-answer", "6")
        click("quiz-next")
        click("answer-true")
        click("quiz-next")
        input("quiz-answer", "2x")
        click("quiz-next")
        click("submit-confirm")
        ui.onNodeWithTag("screen-assignment-result").assertExists()
        shot("assignment-result")
        val repo = LocalDemoRepository(ui.activity)
        assertTrue(
            repo.state.value.product.submissions.any {
                it.assignmentId == "assignment-1" && it.studentId == "ahmed" && it.score == 100
            }
        )
    }

    @Test
    fun languageThemeSearchNotificationsAndLongExpression() {
        login()
        shot("home-ar-light")
        click("nav-profile")
        click("open-settings")
        click("theme-Dark")
        shot("settings-ar-dark")
        click("language-English")
        ui.onAllNodesWithText("Settings").onFirst().assertExists()
        click("back")
        click("nav-home")
        shot("home-en-dark")
        click("global-search")
        input("search-input", "derivatives")
        ui.onNodeWithText("Derivatives").assertExists()
        click("back")
        click("global-notifications")
        text("Mark all read")
        click("back")
        click("nav-solver")
        input("solver-input", "x+".repeat(120) + "1")
        click("solve")
        ui.onNodeWithTag("screen-solution").assertExists()
        shot("long-expression-dark")
    }

    @Test
    fun onboardingAndTabState() {
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("intro-next").fetchSemanticsNodes().isNotEmpty()
        }
        repeat(3) { click("intro-next") }
        click("guest")
        click("nav-solver")
        input("solver-input", "2x+4=10")
        click("nav-home")
        click("nav-solver")
        ui.onNodeWithTag("solver-input").assertTextContains("2x+4=10")
    }

    @Test
    fun teacherResourceSharingReachesStudentLibrary() {
        login(true)
        click("nav-teacher/resources")
        text("إنشاء مورد")
        input("resource-title", "ملاحظة نهايات جديدة")
        input("resource-body", "تحقق من النهايات من الجهتين.")
        text("حفظ")
        val resource = LocalDemoRepository(ui.activity).state.value.product.resources.last()
        click("share-${resource.id}")
        text("الثالث الثانوي العلمي — أ")
        assertTrue(
            "class-a" in
                LocalDemoRepository(ui.activity).state.value.product.resources.last().classIds
        )
        click("nav-teacher/profile")
        click("logout")
        click("dialog-confirm")
        click("role-student")
        input("auth-email", "ahmed@example.com")
        input("auth-password", "demo-pass-123")
        click("login-submit")
        text("مكتبتي")
        text("موارد المعلّم")
        ui.onNodeWithText("ملاحظة نهايات جديدة").assertExists()
        shot("student-library")
    }

    @Test
    fun scanLimitMethodsAndTeacherResponse() {
        login()
        click("nav-solver")
        text("كاميرا تجريبية")
        text("محاكاة التصوير")
        text("استخدام المثال المتعرّف عليه")
        ui.onNodeWithTag("solver-input").assertTextContains("2x + 4 = 10")
        input("solver-input", "lim x→1 (x^2-1)/(x-1)")
        click("solve")
        text("لوبيتال")
        ui.onAllNodesWithText("2").onFirst().assertExists()
        shot("limit-method")
        click("edit-problem")
        click("nav-profile")
        click("logout")
        click("dialog-confirm")
        click("role-teacher")
        input("auth-email", "sara@example.com")
        input("auth-password", "demo-pass-123")
        click("login-submit")
        click("nav-teacher/assignments")
        text("أساسيات الاحتمالات")
        text("أحمد محمد")
        input("teacher-feedback", "أحسنت، تابع التدريب.")
        text("حفظ الملاحظات")
        assertTrue(
            LocalDemoRepository(ui.activity).state.value.product.submissions.any {
                it.assignmentId == "assignment-3" && it.studentId == "ahmed" && it.reviewed
            }
        )
        shot("teacher-feedback")
    }
}
