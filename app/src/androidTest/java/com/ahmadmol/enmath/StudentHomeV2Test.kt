package com.ahmadmol.enmath

import android.graphics.Bitmap
import android.graphics.Color
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.model.*
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource

/** Dedicated emulator only. Never seed or clear the user's physical phone. */
class StudentHomeV2Test {
    private val args = InstrumentationRegistry.getArguments()
    private var theme = if (args.getString("theme") == "Dark") ThemeMode.Dark else ThemeMode.Light
    private var language =
        if (args.getString("language") == "English") Language.English else Language.Arabic
    @get:Rule(order = 0)
    val seed =
        object : ExternalResource() {
            override fun before() {
                check(
                    android.os.Build.MODEL.contains("sdk") ||
                        android.os.Build.FINGERPRINT.contains("generic")
                )
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("enmath_demo", 0).edit().clear().commit()
                LocalDemoRepository(context).apply {
                    finishOnboarding()
                    setTheme(theme)
                    setLanguage(language)
                    signIn(User("أحمد محمد", "ahmed@example.com", AccountType.Student))
                }
            }
        }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()

    private fun waitHome() =
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("screen-home").fetchSemanticsNodes().isNotEmpty()
        }

    private fun pressBack() = ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }

    private fun reveal(tag: String): SemanticsNodeInteraction =
        ui.onNodeWithTag(tag).apply {
            var parent = fetchSemanticsNode().parent
            while (parent != null) {
                if (parent.config.contains(SemanticsActions.ScrollBy)) {
                    performScrollTo()
                    break
                }
                parent = parent.parent
            }
        }

    private fun touch(tag: String, minimumHeight: Float = 48f) {
        val bounds = reveal(tag).fetchSemanticsNode().boundsInRoot
        val density = ui.activity.resources.displayMetrics.density
        assertTrue("$tag height", bounds.height / density >= minimumHeight - .5f)
        assertTrue("$tag width", bounds.width / density >= 48f)
    }

    private fun textFits() {
        ui.onAllNodes(hasText("", substring = true), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .forEach { node ->
                val results = mutableListOf<TextLayoutResult>()
                node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
                results.forEach {
                // RTL paragraphs can retain the constraint width despite a tightly sized Text.
                // Check actual line widths against available space, not paragraph width vs size.
                assertFalse("Clipped height: ${it.layoutInput.text}", it.didOverflowHeight)
                assertFalse("Ellipsized text: ${it.layoutInput.text}", it.multiParagraph.didExceedMaxLines)
                repeat(it.lineCount) { line ->
                    assertTrue("Clipped line: ${it.layoutInput.text}", it.getLineRight(line) - it.getLineLeft(line) <= it.layoutInput.constraints.maxWidth + .5f)
                }
                }
            }
    }

    private fun shot(suffix: String) {
        ui.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val width = context.resources.configuration.screenWidthDp
        val scale = context.resources.configuration.fontScale
        File(
                context.getExternalFilesDir("s01-v2-screenshots"),
                "$width-$theme-$language-$scale-$suffix.png",
            )
            .apply {
                parentFile?.mkdirs()
                outputStream().use {
                    ui.onRoot()
                        .captureToImage()
                        .asAndroidBitmap()
                        .compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        ).use { it.readBytes() }
    }

    @Test
    fun responsiveMatrix() {
        val cases = mutableListOf<Triple<String, Language, Float>>()
        for (size in listOf("393x865", "360x800", "412x915", "800x1280"))
            for (lang in listOf(Language.Arabic, Language.English)) cases += Triple(size, lang, 1f)
        cases += Triple("360x800", Language.Arabic, 1.3f)
        cases += Triple("320x720", Language.Arabic, 1.3f)
        val rows = mutableListOf("viewport,theme,language,fontScale,passed")
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val results = File(ctx.getExternalFilesDir("s01-v2-screenshots"), "ui-matrix.csv")
        results.parentFile?.mkdirs()
        for ((size, lang, scale) in cases) {
            shell("wm size $size")
            shell("settings put system font_scale $scale")
            ui.waitUntil(30_000) {
                ui.activity.resources.configuration.screenWidthDp == size.substringBefore('x').toInt() &&
                    kotlin.math.abs(ui.activity.resources.configuration.fontScale - scale) < .01f
            }
            waitHome()
            for (mode in listOf(ThemeMode.Light, ThemeMode.Dark)) {
                theme = mode; language = lang
                ui.runOnIdle {
                    ViewModelProvider(ui.activity)[AppViewModel::class.java].apply { theme(mode); language(lang) }
                }
                ui.waitForIdle()
                try {
                    ui.onNode(hasScrollAction()).performScrollToNode(hasText(if (lang == Language.Arabic) "أهلاً، أحمد محمد" else "Hello, أحمد محمد"))
                    ui.onNode(hasScrollAction()).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -100000f) }
                    ui.waitForIdle()
                    shot("home")
                    textFits()
                    touch("student-continue", 52f)
                    touch("home-practice"); touch("home-quick-solver")
                    val a = reveal("home-practice").fetchSemanticsNode().boundsInRoot
                    val b = reveal("home-quick-solver").fetchSemanticsNode().boundsInRoot
                    assertEquals("Aligned action tiles", a.height, b.height, .5f)
                    textFits()
                    if (ui.onAllNodesWithTag("home-upcoming-assignment").fetchSemanticsNodes().isNotEmpty()) touch("home-upcoming-assignment")
                    touch("home-all-progress"); textFits(); shot("progress")
                    ui.onNodeWithText(if (lang == Language.Arabic) "تعلمك الأخير" else "Recent learning").performScrollTo()
                    textFits(); shot("recent")
                    rows += "$size,$mode,$lang,$scale,true"
                } catch (error: Throwable) {
                    rows += "$size,$mode,$lang,$scale,false"
                    results.writeText(rows.joinToString("\n"))
                    throw error
                }
                results.writeText(rows.joinToString("\n"))
            }
        }
    }

    @Test
    fun homeLayoutAndActions() {
        waitHome()
        ui.onNodeWithTag("nav-home").assertIsSelected()
        val before = LocalDemoRepository(ui.activity).state.value
        val bitmap = ui.onRoot().captureToImage().asAndroidBitmap()
        assertEquals(
            Color.parseColor(if (theme == ThemeMode.Dark) "#0B1322" else "#F7F9FC"),
            bitmap.getPixel(0, 20),
        )
        touch("student-continue", 52f)
        shot("home")
        textFits()
        touch("home-practice")
        touch("home-quick-solver")
        textFits()
        if (ui.onAllNodesWithTag("home-upcoming-assignment").fetchSemanticsNodes().isNotEmpty())
            touch("home-upcoming-assignment")
        touch("home-all-progress")
        textFits()
        shot("progress")
        ui.onNodeWithText(if (language == Language.Arabic) "تعلمك الأخير" else "Recent learning")
            .performScrollTo()
        textFits()
        shot("recent")
        reveal("student-continue").performClick()
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("block-concept").fetchSemanticsNodes().isNotEmpty()
        }
        pressBack()
        waitHome()
        reveal("home-practice").performClick()
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("screen-practice").fetchSemanticsNodes().isNotEmpty()
        }
        pressBack()
        waitHome()
        reveal("home-quick-solver").performClick()
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("screen-solver").fetchSemanticsNodes().isNotEmpty()
        }
        pressBack()
        waitHome()
        reveal("home-all-progress").performClick()
        ui.waitUntil(15_000) {
            ui.onAllNodesWithTag("screen-progress").fetchSemanticsNodes().isNotEmpty()
        }
        pressBack()
        waitHome()
        ui.activityRule.scenario.recreate()
        waitHome()
        ui.onNodeWithTag("student-continue").assertExists()
        val after = LocalDemoRepository(ui.activity).state.value
        assertEquals(before.completed, after.completed)
        assertEquals(before.product.attempts, after.product.attempts)
        assertEquals(before.user, after.user)
    }
}
