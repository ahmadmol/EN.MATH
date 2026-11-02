package com.ahmadmol.enmath

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Production Compose and navigation, isolated disk fixtures. These are not raster screenshots. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ar-rSY-w360dp-h800dp")
@LooperMode(LooperMode.Mode.PAUSED)
class StudentPolishHostTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var repo: LocalDemoRepository
    private lateinit var nav: NavHostController
    private val files = mutableMapOf<String, FixturePreferences>()
    private fun mount(stage: StudyStage, language: Language, dark: Boolean, scale: Float,
        start: String = Routes.Home, prepare: (LocalDemoRepository) -> Unit = {}) {
        repo = LocalDemoRepository(false) { files.getOrPut(it) { FixturePreferences() }.prefs }
        repo.createLocalProfile("طالب · Student", stage, false)
        if (stage == StudyStage.G9) repo.grade9Study.selectSubject(StudentCatalog.scope(repo.state.value)!!, Grade9Subject.ALGEBRA)
        prepare(repo)
        val app = AppViewModel(repo)
        val solver = SolverViewModel(SavedStateHandle())
        ui.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, scale)) {
                ENMathTheme(if (dark) ThemeMode.Dark else ThemeMode.Light, language, true, stage) {
                    Surface {
                        nav = rememberNavController()
                        NavHost(nav, startDestination = start) {
                            studentGraph(nav, app, solver, { nav.navigate(it) }, {}, { it() }, null)
                        }
                    }
                }
            }
        }
        ui.waitForIdle()
    }
    private fun scope() = StudentCatalog.scope(repo.state.value)!!
    private fun assertBacNextAction() {
        ui.onNodeWithText("2/4").assertDoesNotExist()
        ui.onNodeWithText("Milestone Reward!").assertDoesNotExist()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("student-continue"))
        ui.onNodeWithTag("student-continue").assertHeightIsAtLeast(48.dp).performClick()
        ui.onNodeWithTag("bac-source-pages").assertExists()
        assertTrue(StudentCatalog.completed(repo.state.value).isEmpty())
        assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
    }

    @Test fun bac360ArabicDark200HasHonestNextAction() {
        mount(StudyStage.BAC, Language.Arabic, true, 2f); assertBacNextAction()
    }
    @Test @Config(qualifiers = "en-w393dp-h850dp")
    fun bac393EnglishLight150HasHonestNextAction() {
        mount(StudyStage.BAC, Language.English, false, 1.5f); assertBacNextAction()
    }
    @Test @Config(qualifiers = "en-w800dp-h1200dp")
    fun bacTabletDark200KeepsReadableWidth() {
        mount(StudyStage.BAC, Language.English, true, 2f)
        ui.onNodeWithTag("screen-home").assertWidthIsEqualTo(640.dp)
        assertBacNextAction()
    }
    @Test fun bacBookmarkRemainsAReferenceAndDoesNotCreateCompletionOrGems() {
        val research = SourceCurricula.forStage(StudyStage.BAC).researches.first()
        mount(StudyStage.BAC, Language.English, false, 1f, prepare = {
            it.sourceWorkspace.bookmark(StudentCatalog.scope(it.state.value)!!, research.ref, true)
        })
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("home-research-" + research.id))
        ui.onNodeWithTag("home-research-" + research.id).assert(SemanticsMatcher.expectValue(
            androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Saved source reference"))
        assertTrue(StudentCatalog.completed(repo.state.value).isEmpty())
        assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun bacSourceStationPositionIsStableAfterReverseScrolling() {
        mount(StudyStage.BAC, Language.English, false, 1f)
        val researches = SourceCurricula.forStage(StudyStage.BAC).researches
        val first = "home-research-" + researches.first().id
        val last = "home-research-" + researches.last().id
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(first))
        val left = ui.onNodeWithTag(first).fetchSemanticsNode().boundsInRoot.left
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(last))
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(first))
        assertEquals(left, ui.onNodeWithTag(first).fetchSemanticsNode().boundsInRoot.left, .01f)
        ui.onNodeWithTag(first).performClick()
        ui.runOnIdle { assertEquals(researches.first().id, nav.currentBackStackEntry?.arguments?.getString("id")) }
    }
    @Test fun g9ProgressCopyWrapsCompletelyAt200Percent() {
        mount(StudyStage.G9, Language.Arabic, true, 2f)
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("g9-learning-progress-card"))
        val text = StudentLearningProgressEngine.computeGrade9Progress(NaturePracticeData()).availablePracticeProgressText.ar
        val layouts = mutableListOf<TextLayoutResult>()
        ui.onNodeWithText(text).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue(layouts.isNotEmpty())
        val layout = layouts.single()
        assertFalse("Full learning instructions must remain readable: size=${layout.size}, lines=${layout.lineCount}, widthOverflow=${layout.didOverflowWidth}, heightOverflow=${layout.didOverflowHeight}, maxLines=${layout.layoutInput.maxLines}, constraints=${layout.layoutInput.constraints}, card=${ui.onNodeWithTag("g9-learning-progress-card").fetchSemanticsNode().boundsInRoot}", layout.hasVisualOverflow)
    }
    @Test fun failedHintImmediatelyShowsPreservedDataError() {
        mount(StudyStage.G9, Language.English, true, 1f, NaturePracticeGrader.ROUTE)
        ui.waitUntil(10000) { ui.onAllNodesWithTag("g9-nature-check").fetchSemanticsNodes().isNotEmpty() }
        val store = files.getValue("enmath_g9_nature_pilot_v1_${scope().ownerId}")
        val bytes = store.file.readBytes(); store.fail = true
        ui.onNodeWithText("Hint").performScrollTo().performClick()
        ui.onNodeWithText("Student file or local storage cannot be read.", substring = true).assertExists()
        ui.onNodeWithTag("g9-nature-check").assertDoesNotExist()
        assertArrayEquals(bytes, store.file.readBytes())
    }
    @Test fun failedPreviousImmediatelyShowsPreservedDataError() {
        mount(StudyStage.G9, Language.English, false, 1f, NaturePracticeGrader.ROUTE, prepare = {
            val s = StudentCatalog.scope(it.state.value)!!
            it.naturePractice.start(s); it.naturePractice.submit(s, "G9-A-U01-L01-Q01", "2"); it.naturePractice.move(s, 1)
        })
        ui.waitUntil(10000) { ui.onAllNodesWithTag("g9-nature-fraction").fetchSemanticsNodes().isNotEmpty() }
        val store = files.getValue("enmath_g9_nature_pilot_v1_${scope().ownerId}")
        val bytes = store.file.readBytes(); store.fail = true
        ui.onNodeWithText("Previous").performScrollTo().performClick()
        ui.onNodeWithText("Student file or local storage cannot be read.", substring = true).assertExists()
        assertArrayEquals(bytes, store.file.readBytes())
    }
}
