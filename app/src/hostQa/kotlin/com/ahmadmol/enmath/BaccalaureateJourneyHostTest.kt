package com.ahmadmol.enmath

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Real production graph, local fixtures only. Source browsing never awards learning credit. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-w393dp-h850dp")
@LooperMode(LooperMode.Mode.PAUSED)
class BaccalaureateJourneyHostTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var nav: NavHostController
    private lateinit var repo: LocalDemoRepository
    private fun mount() {
        val files = mutableMapOf<String, FixturePreferences>()
        repo = LocalDemoRepository(false) { files.getOrPut(it) { FixturePreferences() }.prefs }
        repo.createLocalProfile("BAC QA", StudyStage.BAC, false)
        val app = AppViewModel(repo)
        val solver = SolverViewModel(SavedStateHandle())
        ui.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                ENMathTheme(ThemeMode.Dark, Language.English, true, StudyStage.BAC) {
                    Surface {
                        nav = rememberNavController()
                        NavHost(nav, startDestination = Routes.Study) {
                            studentGraph(nav, app, solver, { nav.navigate(it) }, {}, { it() }, null)
                        }
                    }
                }
            }
        }
        ui.waitForIdle()
    }

    @Test fun bothBooksAndEveryIndexedUnitAndResearchOpenWithCorrectSourceIdentity() {
        mount()
        val catalog = SourceCurricula.forStage(StudyStage.BAC)
        assertEquals(2, catalog.books.size); assertEquals(14, catalog.units.size); assertEquals(63, catalog.researches.size)
        for (book in catalog.books) {
            ui.runOnIdle { nav.navigate(Routes.Study) }
            val legacyId = BacSourceCatalog.book(book.id)!!.existingBookId
            ui.onNodeWithTag("catalog-book-$legacyId").performScrollTo().performClick()
            ui.onNodeWithTag("screen-book").assertExists()
            for (unit in book.units) {
                ui.runOnIdle { nav.navigate(book.route) }
                ui.onNodeWithTag("book-topic-${unit.id}").performScrollTo().performClick()
                ui.onNodeWithTag("screen-topic").assertExists()
                for (research in unit.researches) {
                    ui.runOnIdle { nav.navigate(unit.route) }
                    ui.onNodeWithTag("topic-lesson-${research.id}").performScrollTo().performClick()
                    ui.onNodeWithTag("bac-source-pages").assertExists()
                    ui.runOnIdle {
                        assertEquals(research.id, nav.currentBackStackEntry?.arguments?.getString("id"))
                    }
                    val scope = StudentCatalog.scope(repo.state.value)!!
                    ui.waitUntil(10000) {
                        (repo.sourceWorkspace.read(scope) as SourceWorkspaceRead.Ready).workspace.visitedId == research.id
                    }
                    assertEquals(research.ref, BacSourceCatalog.ref(research.id))
                }
            }
        }
        assertTrue(StudentCatalog.completed(repo.state.value).isEmpty())
        assertTrue(repo.state.value.product.attempts.isEmpty())
        assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
    }

    @Test fun unavailableSourcePracticeStaysPendingAndCrossStageReferencesStayUnavailable() {
        mount()
        val research = SourceCurricula.forStage(StudyStage.BAC).researches.first()
        ui.runOnIdle { nav.navigate(research.route) }
        ui.onNodeWithTag("g9-practice-status").performScrollTo().performClick()
        ui.onNodeWithTag("screen-bac-source-pending").assertExists()
        assertTrue(repo.state.value.product.attempts.isEmpty())
        ui.runOnIdle { nav.navigate("lesson/G9-ALG-U01-L01") }
        ui.onNodeWithTag("screen-g9-nature-lesson").assertDoesNotExist()
        ui.onNodeWithTag("bac-source-pages").assertDoesNotExist()
    }
}
