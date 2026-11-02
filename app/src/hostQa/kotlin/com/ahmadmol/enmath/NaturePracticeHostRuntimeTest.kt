package com.ahmadmol.enmath

import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.*
import com.ahmadmol.enmath.features.solver.SolverViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Optional host Android runtime, using production studentGraph, real Compose actions and isolated stores.
 * This never connects to a device, ADB, emulator, or actual student preferences.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ar-rSY-w360dp-h800dp")
@LooperMode(LooperMode.Mode.PAUSED)
class NaturePracticeHostRuntimeTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var nav: NavHostController
    private lateinit var repo: LocalDemoRepository
    private fun mount(dark: Boolean, scale: Float, debuggable: Boolean = true) {
        val context = RuntimeEnvironment.getApplication()
        context.applicationInfo.flags = if (debuggable) context.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
            else context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE.inv()
        ui.activity.applicationInfo.flags = if (debuggable) ui.activity.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
            else ui.activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE.inv()
        if (debuggable) assertEquals(6, NaturePracticeBank.load(ui.activity).size)
        val files = mutableMapOf<String, FixturePreferences>()
        repo = LocalDemoRepository(false) { key -> files.getOrPut(key) { FixturePreferences() }.prefs }
        repo.createLocalProfile("اختبار مضيف", StudyStage.G9, false)
        val s = StudentCatalog.scope(repo.state.value)!!
        repo.grade9Study.selectSubject(s, Grade9Subject.ALGEBRA)
        val app = AppViewModel(repo)
        val solver = SolverViewModel(SavedStateHandle())
        ui.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, scale)) {
                ENMathTheme(if (dark) ThemeMode.Dark else ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
                    Surface {
                        nav = rememberNavController()
                        NavHost(nav, startDestination = Routes.Home) {
                            studentGraph(nav, app, solver, { nav.navigate(it) }, {}, { it() }, null)
                        }
                    }
                }
            }
        }
        ui.waitForIdle()
    }
    private fun clickText(text: String) = ui.onNodeWithText(text).performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
    private fun awaitTag(tag: String): SemanticsNodeInteraction {
        ui.waitUntil(10000) { ui.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        return ui.onNodeWithTag(tag, useUnmergedTree = true)
    }
    private fun clickTag(tag: String) = awaitTag(tag).performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
    private fun awaitQuestion(position: Int) {
        ui.waitUntil(10000) { ui.onAllNodesWithText("السؤال ${position + 1} من 6").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun snapshot() = (repo.naturePractice.read(StudentCatalog.scope(repo.state.value)!!) as NatureRead.Ready).data
    private fun runSession(dark: Boolean, scale: Float) {
        mount(dark, scale)
        val station = "station-G9-ALG-U01-L01"
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(station))
        val before = ui.onNodeWithTag(station).fetchSemanticsNode().boundsInRoot
        ui.onNodeWithTag(station).performClick()
        ui.onNodeWithTag("journey-qa-practice").performScrollTo().assertIsDisplayed().performSemanticsAction(SemanticsActions.OnClick) { it() }
        ui.runOnIdle { assertEquals(NaturePracticeGrader.ROUTE, nav.currentDestination?.route) }
        ui.waitUntil(10000) { snapshot().draft != null }
        ui.onNodeWithTag("screen-g9-nature-practice").assertExists()
        awaitTag("g9-nature-check").assertIsNotEnabled()
        for (q in 0..5) {
            clickText("تلميح")
            awaitTag("g9-nature-hint").assertExists()
            fun choose(correct: Boolean) {
                when (q) {
                    0 -> clickTag(if (correct) "g9-nature-option-2" else "g9-nature-option-0")
                    1 -> ui.onNodeWithTag("g9-nature-fraction").performScrollTo().performTextReplacement(if (correct) "٣/٨" else "6/16")
                    2 -> clickText(if (correct) "صحيح" else "خطأ")
                    3 -> { if (!correct) clickText("دورية") else clickText("غير منتهية") }
                    4 -> {
                        ui.onNodeWithTag("g9-nature-root-value").performScrollTo().performTextReplacement(if (correct) "٥" else "-5")
                        if (!correct) clickText("صحيح")
                    }
                    5 -> listOf("صحيحة", "عشرية", "غير عادية", "صحيحة", "غير عادية").forEachIndexed { i, title ->
                        ui.onAllNodesWithText(if (!correct && i == 0) "غير عادية" else title)[i]
                            .performScrollTo().assertHeightIsAtLeast(48.dp).performClick()
                    }
                }
            }
            choose(false)
            clickTag("g9-nature-check")
            ui.waitUntil(10000) { (snapshot().draft!!.missed.values.sum()) == q + 1 }
            awaitTag("g9-nature-feedback").assertExists()
            ui.onAllNodesWithTag("g9-nature-next").assertCountEquals(0)
            awaitTag("g9-nature-explanation").assertExists()
            choose(true)
            clickTag("g9-nature-check")
            ui.waitUntil(10000) { snapshot().draft!!.passed.size == q + 1 }
            awaitTag("g9-nature-next").assertExists()
            if (q > 0) {
                clickText("السابق")
                awaitQuestion(q - 1)
                clickTag("g9-nature-next")
                awaitQuestion(q)
            }
            if (q == 3) {
                ui.onNodeWithText("دورية").assertIsSelected()
                ui.onNodeWithText("غير منتهية").assertIsSelected()
            }
            if (q == 4) {
                ui.onNodeWithTag("g9-nature-root-value").assertTextContains("٥")
                ui.onNodeWithText("صحيح").assertIsSelected()
            }
            if (q == 5) listOf("صحيحة", "عشرية", "غير عادية", "صحيحة", "غير عادية").forEachIndexed { i, title ->
                ui.onAllNodesWithText(title)[i].assertIsSelected()
            }
            clickTag("g9-nature-next")
            if (q < 5) awaitQuestion(q + 1)
        }
        ui.waitUntil(10000) { snapshot().attempts.size == 1 }
        awaitTag("screen-g9-nature-result").assertExists()
        clickText("العودة إلى المسار الدراسي")
        ui.onNodeWithTag("screen-home").assertExists()
        val after = ui.onNodeWithTag(station).fetchSemanticsNode().boundsInRoot
        assertEquals(before.top, after.top, 1f)
        assertEquals(before.bottom, after.bottom, 1f)
        assertEquals(6, snapshot().attempts.single().missed.values.sum())
        assertEquals(6, snapshot().attempts.single().hints.size)
        val completed = snapshot().attempts.single()
        ui.runOnIdle { nav.navigate("g9/nature-result/${completed.id}") }
        awaitTag("screen-g9-nature-result")
        clickText("التدرّب من جديد")
        awaitTag("g9-nature-check").assertIsNotEnabled()
        assertNotEquals(completed.id, snapshot().draft!!.id)
        assertEquals(listOf(completed), snapshot().attempts)
        clickText("العودة إلى المسار")
        awaitTag("screen-home")
        assertEquals(before.top, ui.onNodeWithTag(station).fetchSemanticsNode().boundsInRoot.top, 1f)
    }
    @Test fun light150Runtime() = runSession(false, 1.5f)
    @Test fun light200Runtime() = runSession(false, 2f)
    @Test fun dark150Runtime() = runSession(true, 1.5f)
    @Test fun dark200Runtime() = runSession(true, 2f)
    @Test fun nonDebuggableEntryAndBothDirectDestinationsStayBlocked() {
        mount(false, 2f, debuggable = false)
        val station = "station-G9-ALG-U01-L01"
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(station))
        ui.onNodeWithTag(station).performClick()
        ui.runOnIdle { assertNotEquals(NaturePracticeGrader.ROUTE, nav.currentDestination?.route); nav.navigate(NaturePracticeGrader.ROUTE) }
        ui.onAllNodesWithTag("screen-g9-nature-practice").assertCountEquals(0)
        ui.runOnIdle { nav.navigate("g9/nature-result/00000000-0000-0000-0000-000000000001") }
        ui.onAllNodesWithTag("screen-g9-nature-result").assertCountEquals(0)
        assertNull(snapshot().draft)
    }
}
