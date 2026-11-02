package com.ahmadmol.enmath

import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
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
 * Senior QA Host UI and Compose verification test for Student Learning Progress.
 * Covers:
 * - Card presence on Home
 * - State and accuracy updates after completion
 * - Clicking action button navigates to practice
 * - Station circle status update (icon and accessibility description)
 * - Light and Dark modes
 * - Font scaling 150%, 200%
 * - Minimum 48dp touch targets
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ar-rSY-w360dp-h800dp")
@LooperMode(LooperMode.Mode.PAUSED)
class StudentLearningProgressHostTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var nav: NavHostController
    private lateinit var repo: LocalDemoRepository
    private lateinit var files: MutableMap<String, FixturePreferences>

    private val good = listOf(
        "2",
        "٣/٨",
        "true",
        "غير منتهية|دورية",
        "٥|integer",
        "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational"
    )
    private fun id(i: Int) = "G9-A-U01-L01-Q${(i + 1).toString().padStart(2, '0')}"

    private fun mount(dark: Boolean, scale: Float, debuggable: Boolean = true) {
        val context = RuntimeEnvironment.getApplication()
        context.applicationInfo.flags = if (debuggable) context.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
        else context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE.inv()
        ui.activity.applicationInfo.flags = if (debuggable) ui.activity.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
        else ui.activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE.inv()
        files = mutableMapOf()
        repo = LocalDemoRepository(false) { key -> files.getOrPut(key) { FixturePreferences() }.prefs }
        repo.createLocalProfile("اختبار التقدم", StudyStage.G9, false)
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

    private fun scope() = StudentCatalog.scope(repo.state.value)!!

    @Test fun releaseHomeAndEvidenceNeverOfferBlockedDemoPractice() {
        mount(false, 2f, debuggable = false)
        ui.onNodeWithTag("g9-learning-progress-card").assertDoesNotExist()
        ui.onNodeWithTag("g9-reference-action").performScrollTo().performClick()
        ui.runOnIdle { assertEquals("g9/reference/{id}", nav.currentDestination?.route) }
        ui.runOnIdle { nav.navigate(Routes.Progress) }
        ui.onNodeWithTag("nature-evidence-practice").assertDoesNotExist()
        ui.onNodeWithText("التدريب التجريبي غير متاح في هذه النسخة. سجلاتك السابقة محفوظة.").assertExists()
    }

    @Test fun geometryNextStepStaysInsideGeometryInsteadOfStartingAlgebraPractice() {
        mount(false, 1.5f)
        ui.runOnIdle { repo.grade9Study.selectSubject(scope(), Grade9Subject.GEOMETRY) }
        ui.onNodeWithTag("g9-learning-progress-card").assertDoesNotExist()
        ui.onNodeWithTag("g9-reference-action").performScrollTo().performClick()
        ui.runOnIdle {
            assertEquals("g9/reference/{id}", nav.currentDestination?.route)
            assertTrue(nav.currentBackStackEntry?.arguments?.getString("id").orEmpty().startsWith("G9-GEO"))
        }
    }

    @Test fun corruptProgressShowsReadErrorWithoutInventingZeroProgress() {
        mount(false, 1f)
        val key = "enmath_g9_nature_pilot_v1_${scope().ownerId}"
        files.getValue(key).prefs.edit().putString("practice", "corrupt-preserved").commit()
        // A genuine source switch recomposes Home and re-reads the scoped document.
        ui.runOnIdle { repo.grade9Study.selectSubject(scope(), Grade9Subject.GEOMETRY) }
        ui.runOnIdle { nav.navigate(Routes.Progress) }
        ui.onNodeWithTag("nature-progress-read-error").assertExists()
        assertEquals("corrupt-preserved", files.getValue(key).prefs.getString("practice", null))
    }

    @Test fun failedSubjectSaveShowsPreservedDataErrorAndKeepsOriginalDocument() {
        mount(false, 1f)
        val store = files.entries.single { it.key.startsWith("enmath_g9_study_v1_") }.value
        val original = store.file.readBytes()
        store.fail = true
        ui.onNodeWithTag("switch-geometry").performScrollTo().performClick()
        ui.onNodeWithText("تعذّرت قراءة بيانات دراستك المحفوظة.", substring = true).assertExists()
        ui.onNodeWithTag("g9-learning-progress-card").assertDoesNotExist()
        assertEquals(G9StudyRead.StorageError, repo.grade9Study.read(scope()))
        assertArrayEquals(original, store.file.readBytes())
    }

    @Test fun savedSessionOpensItsExactResultWithoutStartingAnotherDraft() {
        mount(false, 1f)
        val s = scope()
        repo.naturePractice.start(s)
        for (i in 0..5) {
            repo.naturePractice.submit(s, id(i), good[i])
            if (i < 5) repo.naturePractice.move(s, i + 1)
        }
        val result = repo.naturePractice.finish(s).resultId!!
        ui.runOnIdle { nav.navigate(Routes.Progress) }
        ui.onNodeWithTag("nature-saved-$result").performScrollTo().performClick()
        ui.runOnIdle { assertEquals(result, nav.currentBackStackEntry?.arguments?.getString("id")) }
        ui.onNodeWithTag("screen-g9-nature-result").assertExists()
        assertNull((repo.naturePractice.read(s) as NatureRead.Ready).data.draft)
    }

    @Test
    fun homeProgressCardAppearsAndNavigatesToPractice() {
        mount(dark = false, scale = 1.0f)

        // 1. Progress card is displayed on Home
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithText("تقدمك في التعلم").assertExists()
        ui.onNodeWithText("الجبر — الأعداد والكسور").assertExists()

        // 2. Action button has >= 48dp touch target and clicking navigates
        val actionButton = ui.onNodeWithTag("g9-progress-action")
        actionButton.assertExists().assertHeightIsAtLeast(48.dp)
        actionButton.performClick()
        ui.runOnIdle {
            assertEquals(NaturePracticeGrader.ROUTE, nav.currentDestination?.route)
        }
    }

    @Test
    fun homeProgressCardAndStationCircleUpdateAfterCompletingPractice() {
        mount(dark = false, scale = 1.0f)
        val station = "station-G9-ALG-U01-L01"

        // Before practice: circle has 'available' accessibility text
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(station))
        val beforeDescription = ui.onNodeWithTag(station).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first()
        assertTrue(beforeDescription.contains("تدريب تفاعلي متاح"))

        // Complete 6 questions in repository
        val s = scope()
        repo.naturePractice.start(s)
        for (i in 0..5) {
            repo.naturePractice.submit(s, id(i), good[i])
            if (i < 5) repo.naturePractice.move(s, i + 1)
        }
        val fin = repo.naturePractice.finish(s)
        assertEquals(PlanWrite.SAVED, fin.status)
        ui.waitForIdle()

        // Scroll back to top to inspect progress card
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("g9-learning-progress-card"))
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithTag("g9-home-progress-bar").assertExists()
        ui.onNodeWithText("أكملت تدريب طبيعة الأعداد (النشاط التفاعلي المتاح بالوحدة)").assertExists()
        ui.onNodeWithText("تدرّب مجددًا أو راجع").assertExists()

        // Station circle updated accessibility description
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(station))
        val afterDescription = ui.onNodeWithTag(station).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first()
        assertTrue(afterDescription.contains("اكتمل التدريب"))
    }

    @Test
    fun tenStepInstantUpdateScenario() {
        mount(dark = false, scale = 1.0f)
        val station = "station-G9-ALG-U01-L01"

        // Step 1: Student opens Home
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithText("تدريب طبيعة الأعداد التفاعلي متاح (6 أنشطة)").assertExists()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(station))
        val initialDesc = ui.onNodeWithTag(station).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first()
        assertTrue(initialDesc.contains("تدريب تفاعلي متاح"))

        // Step 2: Taps "طبيعة الأعداد" station circle
        ui.onNodeWithTag(station).performClick()
        ui.runOnIdle { assertEquals(NaturePracticeGrader.ROUTE, nav.currentDestination?.route) }

        // Step 3: Answers 2 questions in practice session
        val s = scope()
        repo.naturePractice.start(s)
        repo.naturePractice.submit(s, id(0), good[0])
        repo.naturePractice.move(s, 1)
        repo.naturePractice.submit(s, id(1), good[1])
        ui.waitForIdle()

        // Step 4: Returns to Home
        ui.runOnIdle { nav.popBackStack() }
        ui.waitForIdle()

        // Step 5: Real progress appears immediately on Home without app restart
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("g9-learning-progress-card"))
        ui.onNodeWithTag("g9-home-progress-bar").assertExists()
        ui.onNodeWithText("التدريب قيد التقدم: أنجزت 2 من 6 أسئلة").assertExists()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(station))
        val midDesc = ui.onNodeWithTag(station).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first()
        assertTrue(midDesc.contains("التدريب قيد التقدم"))

        // Step 6: Returns to resume practice
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("g9-learning-progress-card"))
        ui.onNodeWithTag("g9-progress-action").performClick()
        ui.runOnIdle { assertEquals(NaturePracticeGrader.ROUTE, nav.currentDestination?.route) }

        // Step 7: Completes the 6 questions
        for (i in 2..5) {
            repo.naturePractice.move(s, i)
            repo.naturePractice.submit(s, id(i), good[i])
        }
        val fin = repo.naturePractice.finish(s)
        assertEquals(PlanWrite.SAVED, fin.status)
        ui.waitForIdle()

        // Step 8: Returns to path / Home
        ui.runOnIdle { nav.popBackStack() }
        ui.waitForIdle()

        // Step 9: Station circle and progress card change immediately without restarting app
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("g9-learning-progress-card"))
        ui.onNodeWithText("أكملت تدريب طبيعة الأعداد (النشاط التفاعلي المتاح بالوحدة)").assertExists()
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag(station))
        val completedDesc = ui.onNodeWithTag(station).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first()
        assertTrue(completedDesc.contains("اكتمل التدريب"))

        // Step 10: Retrains, previous attempt remains preserved
        val attemptsBefore = (repo.naturePractice.read(s) as NatureRead.Ready).data.attempts
        assertEquals(1, attemptsBefore.size)
        val firstAttemptId = attemptsBefore[0].id

        repo.naturePractice.start(s)
        val dataAfterRestart = (repo.naturePractice.read(s) as NatureRead.Ready).data
        assertNotNull(dataAfterRestart.draft)
        assertNotEquals(firstAttemptId, dataAfterRestart.draft?.id)
        assertEquals(1, dataAfterRestart.attempts.size)
        assertEquals(firstAttemptId, dataAfterRestart.attempts[0].id)
    }

    @Test
    fun progressScreenDisplaysNaturePracticeEvidenceSection() {
        mount(dark = false, scale = 1.0f)
        val s = scope()
        repo.naturePractice.start(s)
        for (i in 0..5) {
            repo.naturePractice.submit(s, id(i), good[i])
            if (i < 5) repo.naturePractice.move(s, i + 1)
        }
        repo.naturePractice.finish(s)

        // Navigate to Routes.Progress
        ui.runOnIdle { nav.navigate(Routes.Progress) }
        ui.waitForIdle()

        ui.onNodeWithText("أدلة التدريب التفاعلي — طبيعة الأعداد").assertExists()
        ui.onNodeWithText("جلسات").assertExists()
        ui.onNodeWithText("دقة أول محاولة").assertExists()
        ui.onNodeWithText("100%").assertExists()
    }

    @Test
    fun lightModeFontScale100() {
        mount(dark = false, scale = 1.0f)
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithTag("g9-progress-action").assertExists().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("تقدمك في التعلم").assertExists()
    }

    @Test
    fun lightModeFontScale150() {
        mount(dark = false, scale = 1.5f)
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithTag("g9-progress-action").assertExists().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("تقدمك في التعلم").assertExists()
    }

    @Test
    fun lightModeFontScale200() {
        mount(dark = false, scale = 2.0f)
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithTag("g9-progress-action").assertExists().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("تقدمك في التعلم").assertExists()
    }

    @Test
    fun darkModeFontScale100() {
        mount(dark = true, scale = 1.0f)
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithTag("g9-progress-action").assertExists().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("تقدمك في التعلم").assertExists()
    }

    @Test
    fun darkModeFontScale150() {
        mount(dark = true, scale = 1.5f)
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithTag("g9-progress-action").assertExists().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("تقدمك في التعلم").assertExists()
    }

    @Test
    fun darkModeFontScale200() {
        mount(dark = true, scale = 2.0f)
        ui.onNodeWithTag("g9-learning-progress-card").assertExists()
        ui.onNodeWithTag("g9-progress-action").assertExists().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("تقدمك في التعلم").assertExists()
    }
}
