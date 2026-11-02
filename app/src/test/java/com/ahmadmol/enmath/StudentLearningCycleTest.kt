package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.*
import com.ahmadmol.enmath.features.student.recordedMistakes
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class StudentLearningCycleTest {
    private class Stores {
        val files = mutableMapOf<String, FixturePreferences>()
        fun prefs(name: String) = files.getOrPut(name) { FixturePreferences() }.prefs
        fun repo() = LocalDemoRepository(false, ::prefs)
        fun restart() { files.replaceAll { _, old -> FixturePreferences(old.file) } }
    }
    @Test fun attemptEditionAndCommandSurviveRestartAndChangedReplayIsRejected() {
        val stores = Stores(); var r = stores.repo(); val p = r.createLocalProfile("A", StudyStage.BAC, false)
        val app = AppViewModel(r); val set = r.state.value.practiceSets.first()
        val answers = set.questionIds.associateWith { r.state.value.questions.first { q -> q.id == it }.answer }
        assertTrue(app.recordPractice(set.id, answers, 45, LearningRules.sessionOwner(r.state.value), "stable"))
        assertFalse(app.recordPractice(set.id, emptyMap(), 45, LearningRules.sessionOwner(r.state.value), "stable"))
        stores.restart(); r = stores.repo()
        val attempt = StudentCatalog.attempts(r.state.value).single()
        assertEquals(StudentCatalog.BAC_EDITION, attempt.edition); assertEquals(StudyStage.BAC, attempt.stage)
        assertEquals("stable", attempt.commandId)
        assertTrue(AppViewModel(r).recordPractice(set.id, answers, 45, LearningRules.sessionOwner(r.state.value), "stable"))
        assertEquals(1, r.state.value.product.attempts.size)
    }
    @Test fun latestCorrectAttemptResolvesMistakesWithoutInventingWeakSkills() {
        val r = Stores().repo(); val p = r.createLocalProfile("A", StudyStage.BAC, true); val app = AppViewModel(r)
        val set = r.state.value.practiceSets.first()
        assertTrue(recordedMistakes(r.state.value).isEmpty())
        app.recordPractice(set.id, emptyMap(), 10, LearningRules.sessionOwner(r.state.value), "wrong")
        assertEquals(set.questionIds.size, recordedMistakes(r.state.value).size)
        val answers = set.questionIds.associateWith { r.state.value.questions.first { q -> q.id == it }.answer }
        app.recordPractice(set.id, answers, 20, LearningRules.sessionOwner(r.state.value), "correct")
        assertTrue(recordedMistakes(r.state.value).isEmpty())
        assertTrue(r.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun learningResultAndPlannerStayIsolatedAcrossGuestStages() {
        val r = Stores().repo(); val bac = r.createLocalProfile("", StudyStage.BAC, true)
        val app = AppViewModel(r); app.recordPractice(r.state.value.practiceSets.first().id, emptyMap(), 10, LearningRules.sessionOwner(r.state.value), "attempt")
        val bacScope = StudentCatalog.scope(r.state.value)!!
        val task = PlanTask(UUID.randomUUID().toString(), "Arabic reading", "Arabic", PlanPeriod.WEEK,
            StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK), PlanMode.ACHIEVEMENT, 3)
        assertEquals(PlanWrite.SAVED, r.planner.save(bacScope, 0, "plan", task))
        r.createLocalProfile("", StudyStage.G9, true)
        assertTrue(StudentCatalog.attempts(r.state.value).isEmpty()); assertTrue(recordedMistakes(r.state.value).isEmpty())
        assertTrue((r.planner.read(StudentCatalog.scope(r.state.value)!!) as PlanRead.Ready).plan.tasks.isEmpty())
        r.selectLocalProfile(bac.id)
        assertEquals(1, StudentCatalog.attempts(r.state.value).size)
        assertEquals(task, (r.planner.read(StudentCatalog.scope(r.state.value)!!) as PlanRead.Ready).plan.tasks.single())
    }
    @Test fun noSixthTabAndPlannerReviewNeverShowBottomNavigation() {
        assertEquals(listOf("study", "solver", "home", "explore", "profile"), Routes.tabs)
        for (route in listOf("planner", "review", "practice/{id}/result", "lesson/{id}"))
            assertFalse(showStudentNavigation(route, listOf(route), true))
        assertEquals(Routes.Profile, Routes.selectedTab("planner"))
        assertEquals(Routes.Profile, Routes.selectedTab("review"))
    }
    @Test fun priorCompletionDoesNotRetroactivelyCreditNewOrCarriedGoals() {
        val r = Stores().repo(); r.createLocalProfile("A", StudyStage.BAC, true); r.recordScopedLearning(StudentCatalog.scope(r.state.value)!!, StudentCatalog.ref(r.state.value, "limits-practice"), "fixture-complete") { it }
        val scope = StudentCatalog.scope(r.state.value)!!
        val t = PlanTask(UUID.randomUUID().toString(), "Practice", "Mathematics", PlanPeriod.WEEK,
            StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK), PlanMode.ACHIEVEMENT, 5,
            lesson = StudentCatalog.ref(r.state.value, "limits-practice"))
        r.planner.save(scope, 0, "save", t)
        val saved = (r.planner.read(scope) as PlanRead.Ready).plan.tasks.single()
        assertTrue(saved.linkedBaseline); assertEquals(0, r.planner.measured(saved, r.state.value).done)
        r.planner.carry(scope, 1, "carry", t.id, StudyPeriods.next(t.start, t.period), true)
        val carried = (r.planner.read(scope) as PlanRead.Ready).plan.tasks.last()
        assertEquals(5, carried.target); assertEquals(0, r.planner.measured(carried, r.state.value).done)
    }
    @Test fun studentLearningAndPlannerDoNotModifyTeacherSourceBytes() {
        val stores = Stores(); ProductCodec.save(stores.prefs("enmath_demo"), ProductSeed.seed())
        val r = stores.repo(); val before = stores.files.getValue("enmath_demo").file.readBytes()
        val p = r.createLocalProfile("A", StudyStage.BAC, true)
        AppViewModel(r).complete("limits-practice", LearningRules.sessionOwner(r.state.value))
        val scope = StudentCatalog.scope(r.state.value)!!
        r.planner.save(scope, 0, "save", PlanTask("fixture", "Read", "Physics", PlanPeriod.DAY,
            StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.DAY), PlanMode.TIME, 0, 30))
        assertArrayEquals(before, stores.files.getValue("enmath_demo").file.readBytes())
    }
}
