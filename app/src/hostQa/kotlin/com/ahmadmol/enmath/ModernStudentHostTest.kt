package com.ahmadmol.enmath

import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.*
import com.ahmadmol.enmath.features.solver.SolverViewModel
import com.ahmadmol.enmath.features.student.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Production Compose graph, fixtures only. Geometry/semantics checks do not replace raster/device QA. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="en-w360dp-h800dp")
@LooperMode(LooperMode.Mode.PAUSED)
class ModernStudentHostTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private lateinit var repo:LocalDemoRepository
    private lateinit var nav:NavHostController
    private data class Options(val scale:Float=1f,val language:Language=Language.Arabic,val dark:Boolean=false)
    private val options=mutableStateOf(Options())
    private fun mount(start:String=Routes.Home,stage:StudyStage=StudyStage.G9,bar:Boolean=false) {
        val context=RuntimeEnvironment.getApplication()
        context.applicationInfo.flags=context.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
        ui.activity.applicationInfo.flags=ui.activity.applicationInfo.flags or ApplicationInfo.FLAG_DEBUGGABLE
        val files=mutableMapOf<String,FixturePreferences>()
        repo=LocalDemoRepository(false){files.getOrPut(it){FixturePreferences()}.prefs}
        repo.createLocalProfile("Student · طالب",stage,false)
        val app=AppViewModel(repo,ui.activity.applicationContext); val solver=SolverViewModel(SavedStateHandle())
        ui.setContent {
            val s by repo.state.collectAsState()
            val reading=rememberModernStudent(s,repo.studentExperience)
            val avatar=(reading as? ModernRead.Ready)?.data?.selectedAvatarId
            val o=options.value
            CompositionLocalProvider(LocalDensity provides Density(1f,o.scale)) {
                ENMathTheme(if(o.dark)ThemeMode.Dark else ThemeMode.Light,o.language,true,s.user?.studyStage,avatar) {
                    nav=rememberNavController()
                    val entry by nav.currentBackStackEntryAsState()
                    val selected = Routes.selectedTab(entry?.destination?.route) ?: Routes.Home
                    val wide = LocalConfiguration.current.screenWidthDp >= 768
                    Scaffold(bottomBar={if(bar && !wide) ModernStudentNavigation(selected){nav.navigate(it)}}) { padding ->
                        Row(Modifier.fillMaxSize().padding(padding)) {
                            if(bar && wide) ModernStudentRail(selected){nav.navigate(it)}
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                            NavHost(nav,startDestination=start) {
                                studentGraph(nav,app,solver,{nav.navigate(it)},{},{it()},null)
                                localGemsGraph(app,{nav.popBackStack()},{})
                            }
                            }
                        }
                    }
                }
            }
        };ui.waitForIdle()
    }
    private fun scope()=StudentCatalog.scope(repo.state.value)!!
    private fun click(tag:String) = ui.onNodeWithTag(tag).performScrollTo().performClick()
    private fun lazyClick(screen:String,tag:String) {ui.onNodeWithTag(screen).performScrollToNode(hasTestTag(tag));ui.onNodeWithTag(tag).performClick()}
    private fun matrix() {
        mount(bar=true)
        for(stage in StudyStage.entries) {
            if(stage!=repo.state.value.user?.studyStage) ui.runOnIdle{repo.createLocalProfile("Matrix",stage,false)}
            for(language in Language.entries) for(dark in listOf(false,true)) for(scale in listOf(1f,1.5f,2f)) {
                ui.runOnIdle{repo.setLanguage(language);options.value=Options(scale,language,dark)}
                ui.onNodeWithTag("screen-home").performScrollToIndex(0)
                if(ui.activity.resources.configuration.screenWidthDp < 768) {
                    val circle=ui.onNodeWithTag("nav-home").assertIsSelected().assertWidthIsEqualTo(64.dp).assertHeightIsEqualTo(64.dp).fetchSemanticsNode().boundsInRoot
                    val surface=ui.onNodeWithTag("student-nav-surface").fetchSemanticsNode().boundsInRoot
                    assertEquals("Circle is half above bar",surface.top,circle.center.y,.6f)
                } else { ui.onNodeWithTag("student-navigation-rail").assertExists();ui.onNodeWithTag("nav-home").assertIsSelected() }
                for(route in Routes.modernStudentTabs) ui.onNodeWithTag("nav-$route").assertHeightIsAtLeast(48.dp)
                val labels=if(language==Language.Arabic) listOf("المنهج","الأدوات","الرئيسية","تقدمي","حسابي") else listOf("Curriculum","Tools","Home","Progress","Profile")
                labels.forEach { label ->
                    val layouts=mutableListOf<TextLayoutResult>()
                    ui.onNodeWithText(label).performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
                    assertTrue("$stage/$language/$dark/$scale/$label " + layouts.joinToString { "size=${it.size}, constraints=${it.layoutInput.constraints}, width=${it.didOverflowWidth}, height=${it.didOverflowHeight}" },layouts.isNotEmpty() && layouts.none {it.hasVisualOverflow})
                }
                ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("g9-learning-progress-card"))
                assertTrue(StudentCatalog.completed(repo.state.value).isEmpty())
                assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
                println("MODERN_MATRIX width=${ui.activity.resources.configuration.screenWidthDp} stage=$stage language=$language dark=$dark scale=$scale PASS")
            }
        }
    }
    @Test fun matrix360()=matrix()
    @Test @Config(qualifiers="en-w393dp-h850dp") fun matrix393()=matrix()
    @Test @Config(qualifiers="en-w800dp-h1200dp") fun matrix800()=matrix()

    @Test fun everyRootHasAReadableSelectedStateAt200PercentInBothLanguages() {
        options.value=Options(2f,Language.Arabic,true)
        mount(bar=true)
        for(language in Language.entries) {
            ui.runOnIdle { repo.setLanguage(language);options.value=Options(2f,language,true) }
            for(route in Routes.modernStudentTabs) {
                ui.onNodeWithTag("nav-$route").performClick().assertIsSelected()
                val label=if(language==Language.Arabic) mapOf("study" to "المنهج","tools" to "الأدوات","home" to "الرئيسية","progress" to "تقدمي","profile" to "حسابي").getValue(route)
                    else mapOf("study" to "Curriculum","tools" to "Tools","home" to "Home","progress" to "Progress","profile" to "Profile").getValue(route)
                val layouts=mutableListOf<TextLayoutResult>()
                ui.onNodeWithText(label).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                assertTrue(layouts.isNotEmpty() && layouts.none { it.hasVisualOverflow })
            }
        }
    }

    @Test fun avatarSelectionPersistsAcrossHomeProfileAndASecondAccount() {
        mount(Routes.Avatar)
        click("avatar-choice-lama");click("avatar-save")
        assertEquals("lama",(repo.studentExperience.read(scope()) as ModernRead.Ready).data.selectedAvatarId)
        ui.runOnIdle{nav.navigate(Routes.Profile)}
        ui.onNodeWithTag("student-avatar-lama").assertExists()
        val a=scope().ownerId
        ui.runOnIdle{repo.createLocalProfile("B",StudyStage.G9,false);nav.navigate(Routes.Profile)}
        ui.onNodeWithTag("student-avatar-neutral").assertExists()
        ui.runOnIdle{repo.selectLocalProfile(a);nav.navigate(Routes.Profile)}
        ui.onNodeWithTag("student-avatar-lama").assertExists()
    }
    @Test fun allTenToolsOpenRealDestinations() {
        mount(Routes.Tools)
        val destinations=listOf("planner" to "planner","library" to "library","solver" to Routes.Solver,
            "graph" to "explore","training" to "training","content" to "content/status","activity" to Routes.Activity,
            "progress" to Routes.Progress,"gems" to "gems/wallet","review" to "review")
        destinations.forEach { (id,route) ->
            ui.runOnIdle{nav.navigate(Routes.Tools)}
            lazyClick("screen-tools","tools-$id")
            ui.runOnIdle{assertEquals(route,nav.currentDestination?.route)}
        }
    }
    @Test fun sixQuestionQaFlowSavesWrongTextHintsAndTeacherReviewWithoutRewards() {
        val lesson="G9-ALG-U01-L02"
        mount("qa/practice/$lesson")
        ui.waitUntil(10000){ui.onAllNodesWithTag("qa-choice-0").fetchSemanticsNodes().isNotEmpty()}
        click("qa-choice-0");click("qa-check");click("qa-hint")
        assertEquals(DraftGrade.INCORRECT,(repo.studentExperience.read(scope()) as ModernRead.Ready).data.drafts.single().submissions.single().grade)
        click("qa-choice-2");click("qa-check");click("qa-next")
        ui.onNodeWithTag("qa-answer").performScrollTo().performTextInput("١٤")
        ui.runOnIdle{nav.navigate(Routes.Tools);nav.navigate("qa/practice/$lesson")}
        ui.onNodeWithTag("qa-answer").assertTextContains("١٤")
        click("qa-check");click("qa-next");click("qa-true");click("qa-check");click("qa-next")
        ui.onNodeWithTag("qa-answer").performScrollTo().performTextInput("7 والباقي 8")
        click("qa-check");click("qa-next")
        ui.onNodeWithTag("qa-answer").performScrollTo().performTextInput("التبرير بالقواسم")
        click("qa-check");click("qa-next")
        for(i in 0..2) ui.onNodeWithTag("qa-step-$i").performScrollTo().performTextInput(listOf("60 و84","القاسم المشترك 12","12 حزمة")[i])
        click("qa-check");click("qa-next")
        ui.onNodeWithTag("screen-original-qa-result").assertExists()
        val data=(repo.studentExperience.read(scope()) as ModernRead.Ready).data
        assertEquals(1,data.results.size);assertEquals(7,data.results.single().submissions.size)
        assertEquals(3,data.results.single().submissions.count{it.grade==DraftGrade.NEEDS_TEACHER_REVIEW})
        assertEquals(1,data.results.single().hints.size);assertTrue(data.events.isEmpty());assertTrue(repo.state.value.product.pendingGemEvents.isEmpty())
        ui.runOnIdle { nav.navigate(Routes.Home) }
        ui.onNodeWithTag("screen-home").performScrollToNode(hasTestTag("home-research-$lesson"))
        ui.onNodeWithTag("home-research-$lesson").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"جلسة اختبار داخلي محفوظة · مراجعة المعلم معلّقة"))
        ui.onNodeWithTag("home-research-$lesson").performClick()
        ui.onNodeWithTag("journey-last-qa-result").performScrollTo().assertIsDisplayed().performSemanticsAction(SemanticsActions.OnClick) { it() }
        ui.runOnIdle { assertEquals(data.results.single().id, nav.currentBackStackEntry?.arguments?.getString("id")) }
        ui.runOnIdle { nav.navigate("qa/review") }
        ui.onNodeWithTag("screen-qa-mistakes").performScrollToNode(hasTestTag("qa-mistake-G9-A-U01-L02-Q01"))
        ui.onNodeWithTag("qa-mistake-G9-A-U01-L02-Q01").assertExists()
    }
    @Test fun unitScreenStartsWithLessonsAndKeepsPlanActionSecondary() {
        mount("topic/G9-ALG-U01")
        val first=ui.onNodeWithTag("topic-lesson-G9-ALG-U01-L01").fetchSemanticsNode().boundsInRoot
        assertTrue(first.top<500f)
        ui.onNodeWithText("إضافة مراجع الوحدة كلها للخطة").assertDoesNotExist()
        click("topic-lesson-G9-ALG-U01-L02")
        ui.onNodeWithTag("source-qa-practice").assertExists()
    }
    @Test fun profilePreferencesAndPrivacyRoutesWorkAtLargeType() {
        options.value=Options(2f,Language.English,true)
        mount(Routes.Profile)
        lazyClick("screen-profile","profile-reduced-motion")
        assertTrue((repo.studentExperience.read(scope()) as ModernRead.Ready).data.reducedMotion)
        lazyClick("screen-profile","profile-privacy")
        ui.onNodeWithTag("screen-student-privacy").assertExists()
        assertFalse(LocalOnlyCloudSync.connected)
    }
}
