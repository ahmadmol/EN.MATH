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

    // 1. New student sees Subject Picker cleanly, NOT the path or progress card
    @Test
    fun newStudentSeesSubjectPickerCleanly() {
        mount()

        // Greeting card and Subject Picker exist
        ui.onNodeWithTag("g9-subject-picker").assertExists()
        ui.onNodeWithText("أهلًا بك في رياضيات التاسع!").assertExists()
        ui.onNodeWithTag("select-algebra").assertExists()
        ui.onNodeWithTag("select-geometry").assertExists()

        // Path items and progress card DO NOT exist prior to choice
        ui.onNodeWithTag("g9-learning-progress-card").assertDoesNotExist()
        ui.onNodeWithTag("switch-algebra").assertDoesNotExist()
        ui.onNodeWithTag("switch-geometry").assertDoesNotExist()
        ui.onNodeWithTag("station-G9-ALG-U01-L01").assertDoesNotExist()
        ui.onNodeWithTag("g9-continue").assertDoesNotExist()
    }

    // 2. Selecting Algebra transitions immediately to Algebra path and saves state
    @Test
    fun selectingAlgebraTransitionsToAlgebraPath() {
        mount()

        // Click Algebra card
        ui.onNodeWithTag("select-algebra").performScrollTo().performClick()
        ui.waitForIdle()

        // Picker disappears
        ui.onNodeWithTag("g9-subject-picker").assertDoesNotExist()

        // Top Switcher and Progress Card appear
        ui.onNodeWithTag("switch-algebra").assertExists()
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()

        // Algebra units and stations are now visible
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("station-G9-ALG-U01-L01"))
        ui.onNodeWithTag("book-G9-ALG").assertExists()
        ui.onNodeWithTag("station-G9-ALG-U01-L01").assertExists()

        // Verify persistent store
        assertEquals(Grade9Subject.ALGEBRA, studyData().activeSubject)
        assertTrue(studyData().subjectChosen)
    }

    // 3. Selecting Geometry transitions immediately to Geometry path and saves state
    @Test
    fun selectingGeometryTransitionsToGeometryPath() {
        mount()

        // Click Geometry card
        ui.onNodeWithTag("select-geometry").performScrollTo().performClick()
        ui.waitForIdle()

        // Picker disappears
        ui.onNodeWithTag("g9-subject-picker").assertDoesNotExist()

        // Geometry units and stations appear
        ui.onNodeWithTag("switch-geometry").assertExists()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("station-G9-GEO-U01-L01"))
        ui.onNodeWithTag("book-G9-GEO").assertExists()
        ui.onNodeWithTag("station-G9-GEO-U01-L01").assertExists()

        // Verify persistent store
        assertEquals(Grade9Subject.GEOMETRY, studyData().activeSubject)
        assertTrue(studyData().subjectChosen)
    }

    // 4. Switching between Algebra and Geometry dynamically toggles paths
    @Test
    fun switchingBetweenAlgebraAndGeometryTogglesPaths() {
        mount()
        // Start by choosing Algebra
        ui.onNodeWithTag("select-algebra").performScrollTo().performClick()
        ui.waitForIdle()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("book-G9-ALG"))
        ui.onNodeWithTag("book-G9-ALG").assertExists()

        // Switch to Geometry via tab
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("switch-geometry"))
        ui.onNodeWithTag("switch-geometry").performClick()
        ui.waitForIdle()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("book-G9-GEO"))
        ui.onNodeWithTag("book-G9-GEO").assertExists()
        assertEquals(Grade9Subject.GEOMETRY, studyData().activeSubject)

        // Switch back to Algebra
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("switch-algebra"))
        ui.onNodeWithTag("switch-algebra").performClick()
        ui.waitForIdle()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("book-G9-ALG"))
        ui.onNodeWithTag("book-G9-ALG").assertExists()
        assertEquals(Grade9Subject.ALGEBRA, studyData().activeSubject)
    }

    // 5. Scroll and Subject Isolation: changing student creates clean state
    @Test
    fun studentSwitchingDoesNotCarryOverSubjectState() {
        mount(initialProfileName = "الطالب أ")
        val studentAId = repo.state.value.user?.localId!!

        // Student A chooses Geometry
        ui.onNodeWithTag("select-geometry").performScrollTo().performClick()
        ui.waitForIdle()
        assertEquals(Grade9Subject.GEOMETRY, studyData().activeSubject)

        // Switch to new Student B
        repo.createLocalProfile("الطالب ب", StudyStage.G9, false)
        ui.waitForIdle()

        // Student B must see the Subject Picker, not Student A's Geometry path
        ui.onNodeWithTag("g9-subject-picker").assertExists()
        assertNull(studyData().activeSubject)
        assertFalse(studyData().subjectChosen)

        // Switch back to Student A
        repo.selectLocalProfile(studentAId)
        ui.waitForIdle()
        assertEquals(Grade9Subject.GEOMETRY, studyData().activeSubject)
    }

    // 6. Single clear primary follow-up button on Home without competing actions
    @Test
    fun singlePrimaryActionButtonOnHome() {
        mount()
        ui.onNodeWithTag("select-algebra").performScrollTo().performClick()
        ui.waitForIdle()

        // One primary action button exists in progress card
        ui.onNodeWithTag("g9-progress-action").assertExists().assertHeightIsAtLeast(48.dp)

        // Competing continue button DOES NOT exist
        ui.onNodeWithTag("g9-continue").assertDoesNotExist()

        // Essential tools and plan remain accessible
        ui.onNodeWithTag("screen-home").performScrollToNode(hasText("أدوات دراستك"))
        ui.onNodeWithText("المكتبة والمراجع المحفوظة").assertExists()
        ui.onNodeWithText("الحل الرياضي").assertExists()
        ui.onNodeWithText("مختبر الرسم").assertExists()
        ui.onNodeWithText("التمارين وحالة التوفر").assertExists()
    }
}
