package com.ahmadmol.enmath

import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
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

/**
 * Host Runtime Tests for Phase 9 Journey Acceptance:
 * Verifies First-Time Subject Choice, Subject Switching, Path rendering, Scroll isolation,
 * and Single Clear Follow-Up Action on Home using real Compose UI interactions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ar-rSY-w360dp-h800dp")
@LooperMode(LooperMode.Mode.PAUSED)
class Grade9JourneyHostRuntimeTest {

    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var nav: NavHostController
    private lateinit var repo: LocalDemoRepository

    private fun mount(dark: Boolean = false, scale: Float = 1.0f, initialProfileName: String = "طالب جديد") {
        val context = RuntimeEnvironment.getApplication()
        context.applicationInfo.flags = context.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
        ui.activity.applicationInfo.flags = ui.activity.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE

        val files = mutableMapOf<String, FixturePreferences>()
        repo = LocalDemoRepository(false) { key -> files.getOrPut(key) { FixturePreferences() }.prefs }
        repo.createLocalProfile(initialProfileName, StudyStage.G9, false)
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

    private fun currentScope(): StudentScope = StudentCatalog.scope(repo.state.value)!!
    private fun studyData(): G9StudyData = (repo.grade9Study.read(currentScope()) as G9StudyRead.Ready).data


    private fun scroll(tag: String) = ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(tag))
    private fun jump(book: String, unit: String) {
        scroll("journey-select-$book")
        ui.onNodeWithTag("journey-select-$book").performClick()
        ui.onNodeWithTag("journey-unit-picker").performScrollToNode(hasTestTag("journey-jump-$unit"))
        ui.onNodeWithTag("journey-jump-$unit").assertIsDisplayed().performSemanticsAction(SemanticsActions.OnClick) { it() }
        ui.waitForIdle()
        ui.onNodeWithTag("home-unit-$unit").assertIsDisplayed()
    }
    @Test fun newStudentSeesOneJourneyWithoutARequiredSubjectGate() {
        mount()
        ui.onNodeWithTag("home-unlimited-energy").assertExists()
        scroll("g9-learning-progress-card")
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        scroll("station-G9-ALG-U01-L01")
        ui.onNodeWithTag("station-G9-ALG-U01-L01").assertExists()
        assertNull(studyData().activeSubject)
    }
    @Test fun choosingAlgebraUnitJumpsInsideTheSameHome() {
        mount(); jump("G9-ALG", "G9-ALG-U06")
        ui.runOnIdle { assertEquals(Routes.Home, nav.currentDestination?.route) }
        assertTrue(repo.state.value.completed.isEmpty())
    }
    @Test fun geometryPickerContainsFourUnitsAndJumpsToTheFourth() {
        mount(); jump("G9-GEO", "G9-GEO-U04")
        scroll("station-G9-GEO-U04-L03")
        ui.onNodeWithTag("station-G9-GEO-U04-L03").assertExists()
        assertEquals(4,SourceCurricula.forStage(StudyStage.G9).book("G9-GEO")!!.units.size)
    }
    @Test fun all32StationsAndTenUnitsRemainInTheSameLazyJourney() {
        mount()
        val catalog=SourceCurricula.forStage(StudyStage.G9)
        catalog.units.forEach { unit ->
            scroll("home-unit-${unit.id}"); ui.onNodeWithTag("home-unit-${unit.id}").assertExists()
            unit.researches.forEach { research -> scroll("station-${research.id}"); ui.onNodeWithTag("station-${research.id}").assertExists() }
        }
        assertEquals(32,catalog.researches.size); assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun studentSwitchingKeepsAvatarAndViewportOwnedByEachStudent() {
        mount(initialProfileName="A"); val a=currentScope().ownerId
        repo.studentExperience.selectAvatar(currentScope(),"muadh")
        jump("G9-GEO","G9-GEO-U04")
        scroll("station-G9-GEO-U04-L03")
        ui.runOnIdle { repo.createLocalProfile("B",StudyStage.G9,false) }
        ui.onNodeWithTag("home-unlimited-energy").assertExists()
        assertNull((repo.studentExperience.read(currentScope()) as ModernRead.Ready).data.selectedAvatarId)
        ui.runOnIdle { repo.selectLocalProfile(a) }
        assertEquals("muadh",(repo.studentExperience.read(currentScope()) as ModernRead.Ready).data.selectedAvatarId)
    }
    @Test fun homeHasOneNextActionAndToolsHaveTheirDedicatedScreen() {
        mount()
        scroll("student-continue")
        ui.onAllNodesWithTag("student-continue").assertCountEquals(1)
        ui.onNodeWithTag("student-continue").assertHeightIsAtLeast(48.dp)
        ui.onNodeWithTag("tools-solver").assertDoesNotExist()
        ui.runOnIdle { nav.navigate(Routes.Tools) }
        ui.onNodeWithTag("screen-tools").assertExists()
        ui.onNodeWithTag("screen-tools").performScrollToNode(hasTestTag("tools-solver"))
        ui.onNodeWithTag("tools-solver").performClick()
        ui.runOnIdle { assertEquals(Routes.Solver,nav.currentDestination?.route) }
    }
}
