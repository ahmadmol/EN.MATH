package com.ahmadmol.enmath

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.design.tokens.ResourcePackPresentation
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.ENMathApp
import com.ahmadmol.enmath.features.auth.AuthViewModel
import com.ahmadmol.enmath.features.onboarding.SplashScreen
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Actual production components and NavHost. Seeds only a dedicated emulator. */
class FullApplicationUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var app: AppViewModel
    private lateinit var auth: AuthViewModel
    private lateinit var nav: NavHostController
    private val splash = mutableStateOf(true)
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val output
        get() = File(context.getExternalFilesDir("full-ui"), "")

    private data class Screen(val id: String, val route: String, val tag: String)

    private fun shell(cmd: String) =
        ParcelFileDescriptor.AutoCloseInputStream(
                InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(cmd)
            )
            .use { it.readBytes() }

    private fun setup() {
        check(
            android.os.Build.MODEL.contains("sdk") ||
                android.os.Build.FINGERPRINT.contains("generic")
        )
        shell("wm size 393x865")
        shell("wm density 160")
        shell("settings put system font_scale 1.0")
        context.getSharedPreferences("enmath_demo", 0).edit().clear().commit()
        val repo =
            LocalDemoRepository(context).apply {
                finishOnboarding()
                setLanguage(Language.Arabic)
                setTheme(ThemeMode.Light)
                signIn(User("أحمد محمد", "ahmed@example.com", AccountType.Student))
            }
        ui.runOnUiThread {
            app =
                ViewModelProvider(ui.activity, AppViewModel.factory(repo))[AppViewModel::class.java]
            auth = ViewModelProvider(ui.activity)[AuthViewModel::class.java]
        }
        // Result evidence uses the real scoring rules, never grades copied from artwork.
        val set = ProductSeed.sets.first { it.id == "limits-practice-set" }
        app.recordPractice(
            set.id,
            set.questionIds.associateWith { ProductSeed.question(it)!!.answer },
            120,
        )
        app.history(app.state.value.examples.first().expression, "limits")
        ui.setContent {
            nav = rememberNavController()
            if (splash.value) {
                val state by app.state.collectAsState()
                ENMathTheme(state.theme, state.language) {
                    ResourcePackPresentation(true) { SplashScreen {} }
                }
            } else ENMathApp(nav)
        }
        output.mkdirs()
    }

    private fun waitTag(tag: String) =
        ui.waitUntil(30_000) { ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    private fun route(screen: Screen) {
        ui.runOnIdle { nav.navigate(screen.route) { launchSingleTop = true } }
        waitTag(screen.tag)
        ui.waitForIdle()
    }

    private fun shot(name: String) {
        ui.waitForIdle()
        File(output, "$name.png").outputStream().use {
            ui.onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun layoutIssues(): List<String> {
        val issues = mutableListOf<String>()
        ui.onAllNodes(hasText("", substring = true), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .forEach { node ->
                val results = mutableListOf<TextLayoutResult>()
                node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
                results.forEach {
                    if (it.didOverflowHeight || it.multiParagraph.didExceedMaxLines)
                        issues += it.layoutInput.text.text.replace('\n', ' ').replace(',', ';')
                    repeat(it.lineCount) { line ->
                        if (
                            it.getLineRight(line) - it.getLineLeft(line) >
                                it.layoutInput.constraints.maxWidth + .5f
                        )
                            issues += "line: ${it.layoutInput.text.text.replace(',', ';')}"
                    }
                }
            }
        return issues.distinct()
    }

    private fun screens(): List<Screen> {
        val p = app.state.value.product
        val result =
            p.submissions.firstOrNull { it.studentId == "ahmed" }?.assignmentId
                ?: p.assignments.first().id
        return listOf(
            Screen("E02", "onboarding", "screen-onboarding"),
            Screen("E03", "started", "screen-started"),
            Screen("E04", "login", "screen-login"),
            Screen("E05", "account-choice", "screen-account-choice"),
            Screen("E06", "register-student", "screen-registration"),
            Screen("E07", "register-teacher", "screen-registration"),
            Screen("E08", "verify", "screen-verification"),
            Screen("E09", "forgot", "screen-forgot"),
            Screen("E10", "new-password", "screen-new-password"),
            Screen("S01", "home", "screen-home"),
            Screen("S02", "study", "screen-study"),
            Screen("S03", "book/book-1", "screen-book"),
            Screen("S04", "topic/limits", "screen-topic"),
            Screen("S05", "lesson/limits-practice", "screen-interactive-lesson"),
            Screen("S06", "practice/limits-practice-set", "screen-practice"),
            Screen("S07", "practice/limits-practice-set/result", "screen-practice-result"),
            Screen("S08", "solver", "screen-solver"),
            Screen("S09", "solver/solution/${p.history.first().id}", "screen-solution"),
            Screen("S10", "solver/history", "screen-history"),
            Screen("S11", "explore", "screen-explore"),
            Screen("S12", "progress", "screen-progress"),
            Screen("S13", "assignments", "screen-assignments"),
            Screen("S14", "assignments/${p.assignments.first().id}", "screen-assignment-details"),
            Screen("S15", "assignments/$result/result", "screen-assignment-result"),
            Screen("S16", "library", "screen-library"),
            Screen("S17", "search", "screen-search"),
            Screen("S18", "notifications", "screen-notifications"),
            Screen("S19", "profile", "screen-profile"),
            Screen("S20", "settings", "screen-settings"),
            Screen("T01", "teacher", "screen-teacher-home"),
            Screen("T02", "teacher/classes", "screen-classes"),
            Screen("T03", "teacher/classes/class-a", "screen-class-details"),
            Screen("T04", "teacher/classes/class-a/students/ahmed", "screen-student-details"),
            Screen("T05", "teacher/assignments", "screen-teacher-assignments"),
            Screen("T06", "teacher/assignments/new", "screen-builder"),
            Screen(
                "T07",
                "teacher/assignments/${p.assignments.first().id}",
                "screen-teacher-results",
            ),
            Screen("T08", "teacher/resources", "screen-resources"),
            Screen("T09", "teacher/profile", "screen-profile"),
        )
    }

    @Test
    fun captureAllDestinationsAndResponsivePatterns() {
        setup()
        val rows =
            mutableListOf(
                "screen,theme,viewport,language,fontScale,routeCheck,textIssues,screenshot"
            )
        fun evidence(
            screen: Screen,
            mode: ThemeMode,
            viewport: String,
            language: Language,
            scale: Float,
        ) {
            route(screen)
            val name = "${screen.id}-${mode.name}-$viewport-${language.name}-$scale"
            shot(name)
            rows +=
                "${screen.id},${mode.name},$viewport,${language.name},$scale,PASS,${layoutIssues().joinToString(" | ")},$name.png"
            File(output, "coverage.csv").writeText(rows.joinToString("\n"))
        }
        for (mode in listOf(ThemeMode.Light, ThemeMode.Dark)) {
            ui.runOnIdle {
                app.theme(mode)
                splash.value = true
            }
            waitTag("screen-splash")
            shot("E01-${mode.name}-393x865-Arabic-1.0")
            rows +=
                "E01,${mode.name},393x865,Arabic,1.0,PASS,,E01-${mode.name}-393x865-Arabic-1.0.png"
            ui.runOnIdle {
                splash.value = false
                app.signIn(User("أحمد محمد", "ahmed@example.com", AccountType.Student))
            }
            waitTag("screen-home")
            for (screen in screens().filterNot { it.id.startsWith("T") }) {
                ui.runOnIdle {
                    auth.chooseRole(
                        if (screen.id == "E07") AccountType.Teacher else AccountType.Student
                    )
                }
                evidence(screen, mode, "393x865", Language.Arabic, 1f)
                if (screen.id in listOf("E07", "S09", "S20")) {
                    ui.onAllNodes(hasScrollAction()).fetchSemanticsNodes().firstOrNull()?.let {
                        ui.onAllNodes(hasScrollAction()).onFirst().performSemanticsAction(
                            SemanticsActions.ScrollBy
                        ) {
                            it(0f, 100000f)
                        }
                        shot("${screen.id}-${mode.name}-lower")
                    }
                }
            }
            ui.runOnIdle {
                app.signIn(User("سارة الأحمد", "sara@example.com", AccountType.Teacher))
                nav.navigate("teacher-main")
            }
            waitTag("screen-teacher-home")
            screens()
                .filter { it.id.startsWith("T") }
                .forEach { evidence(it, mode, "393x865", Language.Arabic, 1f) }
        }
        val risky =
            screens().filter {
                it.id in listOf("E07", "S03", "S06", "S08", "S11", "S20", "T03", "T06")
            }
        try {
            for (viewport in listOf("320x720", "360x800", "412x915", "800x1280")) {
                val scale = if (viewport == "320x720") 1.3f else 1f
                shell("wm size $viewport")
                shell("settings put system font_scale $scale")
                ui.waitUntil(30_000) {
                    ui.activity.resources.configuration.screenWidthDp ==
                        viewport.substringBefore('x').toInt()
                }
                for (lang in Language.entries) for (mode in
                    listOf(ThemeMode.Light, ThemeMode.Dark)) {
                    ui.runOnIdle {
                        app.language(lang)
                        app.theme(mode)
                    }
                    var teacher = true
                    for (screen in risky) {
                        val role = screen.id.startsWith("T")
                        if (role != teacher) {
                            teacher = role
                            ui.runOnIdle {
                                app.signIn(
                                    User(
                                        "أحمد محمد",
                                        "ahmed@example.com",
                                        if (role) AccountType.Teacher else AccountType.Student,
                                    )
                                )
                                nav.navigate(if (role) "teacher-main" else "main")
                            }
                            waitTag(if (role) "screen-teacher-home" else "screen-home")
                        }
                        evidence(screen, mode, viewport, lang, scale)
                    }
                }
            }
        } finally {
            shell("wm size 393x865")
            shell("settings put system font_scale 1.0")
        }
        assertEquals(78, rows.count { it.contains(",393x865,Arabic,1.0,") })
    }
}
