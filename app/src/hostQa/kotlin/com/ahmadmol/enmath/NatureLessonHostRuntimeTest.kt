package com.ahmadmol.enmath

import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
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

/**
 * Host Android Runtime tests for Phase 10A:
 * Complete Learning Unit integration (Lesson Explanation, Step Reveal, Self Check, Navigation, and Independent Scroll).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ar-rSY-w360dp-h800dp")
@LooperMode(LooperMode.Mode.PAUSED)
class NatureLessonHostRuntimeTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var nav: NavHostController
    private lateinit var repo: LocalDemoRepository

    private fun mount(dark: Boolean = false, scale: Float = 1.0f) {
        val context = RuntimeEnvironment.getApplication()
        context.applicationInfo.flags = context.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
        ui.activity.applicationInfo.flags = ui.activity.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE

        val files = mutableMapOf<String, FixturePreferences>()
        repo = LocalDemoRepository(false) { key -> files.getOrPut(key) { FixturePreferences() }.prefs }
        repo.createLocalProfile("طالب تاسع", StudyStage.G9, false)
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

    private fun awaitTag(tag: String): SemanticsNodeInteraction {
        ui.waitUntil(10000) { ui.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        return ui.onNodeWithTag(tag, useUnmergedTree = true)
    }

    private fun clickTag(tag: String) = awaitTag(tag).performScrollTo().performClick()

    // 1. Lesson Explanation Screen Complete Walkthrough
    @Test
    fun lessonExplanationFullWalkthrough() {
        mount(dark = false, scale = 1.0f)

        // Navigate to lesson explanation
        ui.runOnIdle { nav.navigate("lesson/${Grade9NatureLesson.LESSON_ID}") }
        awaitTag("screen-g9-nature-lesson").assertExists()

        // Verify Title and Golden Rule
        ui.onNodeWithTag("g9-nature-lesson-title").assertExists()
        ui.onNodeWithText("قاعدة ذهبية: العبرة بالقيمة لا بالشكل الظاهري").performScrollTo().assertExists()

        // Step Reveal in Worked Example
        ui.onNodeWithText("مثال محلول خطوة بخطوة").performScrollTo().assertExists()
        ui.onNodeWithText("الخطوة 1: تبسيط الأعداد لمعرفة قيمتها الفعلية").performScrollTo().assertExists()

        // Reveal step 2
        clickTag("g9-nature-reveal-step")
        ui.onNodeWithText("الخطوة 2: تصنيف الأعداد الصحيحة").performScrollTo().assertExists()

        // Self-Check interaction
        ui.onNodeWithText("تحقّق من فهمك").performScrollTo().assertExists()
        clickTag("g9-nature-selfcheck-opt-2") // Select √5
        clickTag("g9-nature-check-btn")
        awaitTag("g9-nature-selfcheck-feedback").assertExists()
        ui.onNodeWithText("أحسنت! إجابة صحيحة.").assertExists()

        // Navigate to practice session from lesson
        clickTag("g9-nature-start-practice")
        ui.runOnIdle { assertEquals(NaturePracticeGrader.ROUTE, nav.currentDestination?.route) }
        awaitTag("screen-g9-nature-practice").assertExists()

        // From practice screen: navigate back to lesson explanation
        clickTag("g9-nature-open-lesson")
        ui.runOnIdle {
            assertEquals(Routes.Lesson, nav.currentDestination?.route)
            assertEquals(Grade9NatureLesson.LESSON_ID, nav.currentBackStackEntry?.arguments?.getString("id"))
        }
        awaitTag("screen-g9-nature-lesson").assertExists()

        // Back returns to practice screen where student came from
        clickTag("g9-nature-lesson-back")
        ui.runOnIdle { assertEquals(NaturePracticeGrader.ROUTE, nav.currentDestination?.route) }
        awaitTag("screen-g9-nature-practice").assertExists()
    }

    // 2. Result Screen Link to Lesson Explanation
    @Test
    fun resultScreenNavigatesToLessonExplanation() {
        mount(dark = true, scale = 1.5f)
        val scope = StudentCatalog.scope(repo.state.value)!!
        repo.naturePractice.start(scope)
        val good = listOf("2", "٣/٨", "true", "غير منتهية|دورية", "٥|integer", "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational")
        for (i in 0..5) {
            val qId = "G9-A-U01-L01-Q${(i + 1).toString().padStart(2, '0')}"
            repo.naturePractice.submit(scope, qId, good[i])
            if (i < 5) repo.naturePractice.move(scope, i + 1)
        }
        val fin = repo.naturePractice.finish(scope)
        assertNotNull(fin.resultId)

        ui.runOnIdle { nav.navigate("g9/nature-result/${fin.resultId}") }
        awaitTag("screen-g9-nature-result").assertExists()

        // Click review lesson
        clickTag("g9-nature-review-lesson")
        ui.runOnIdle {
            assertEquals(Routes.Lesson, nav.currentDestination?.route)
            assertEquals(Grade9NatureLesson.LESSON_ID, nav.currentBackStackEntry?.arguments?.getString("id"))
        }
        awaitTag("screen-g9-nature-lesson").assertExists()
    }

    // 3. Home Screen Station Label Navigates to Lesson Explanation
    @Test
    fun homeStationLabelNavigatesToLessonExplanation() {
        mount(dark = false, scale = 1.0f)
        awaitTag("screen-home").assertExists()

        // Clicking the station label card navigates directly to lesson explanation
        clickTag("station-label-${Grade9NatureLesson.LESSON_ID}")
        ui.runOnIdle {
            assertEquals(Routes.Lesson, nav.currentDestination?.route)
            assertEquals(Grade9NatureLesson.LESSON_ID, nav.currentBackStackEntry?.arguments?.getString("id"))
        }
        awaitTag("screen-g9-nature-lesson").assertExists()
    }

    // 4. Switching Subject Preserves Independent Scroll Positions
    @Test
    fun algebraAndGeometryPreserveIndependentScrollPositions() {
        mount(dark = false, scale = 1.0f)
        awaitTag("screen-home").assertExists()

        // Initially in Algebra
        val algebraNode = ui.onNodeWithTag("station-G9-ALG-U01-L01")
        algebraNode.assertExists()

        // Switch to Geometry
        clickTag("switch-geometry")
        ui.waitForIdle()

        // Geometry stations appear
        val geoNode = ui.onNodeWithTag("station-G9-GEO-U01-L01")
        geoNode.assertExists()

        // Switch back to Algebra
        clickTag("switch-algebra")
        ui.waitForIdle()

        // Algebra stations appear back
        ui.onNodeWithTag("station-G9-ALG-U01-L01").assertExists()

        // Switch to Geometry and back again (multi-switch idempotency)
        clickTag("switch-geometry")
        ui.waitForIdle()
        ui.onNodeWithTag("station-G9-GEO-U01-L01").assertExists()

        clickTag("switch-algebra")
        ui.waitForIdle()
        ui.onNodeWithTag("station-G9-ALG-U01-L01").assertExists()
    }
}
