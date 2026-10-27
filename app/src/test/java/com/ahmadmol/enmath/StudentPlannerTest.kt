package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class StudentPlannerTest {
    private class Fixture {
        val files = mutableMapOf<String, FixturePreferences>()
        fun preferences(name: String) = files.getOrPut(name) { FixturePreferences() }.prefs
        var repo = LocalDemoRepository(false, ::preferences)
        init { repo.createLocalProfile("A", StudyStage.BAC, false) }
        val scope get() = StudentCatalog.scope(repo.state.value)!!
        fun task(mode: PlanMode = PlanMode.ACHIEVEMENT) = PlanTask(UUID.randomUUID().toString(), "Read", "Science", PlanPeriod.WEEK,
            StudyPeriods.start(1793088000000L, PlanPeriod.WEEK), mode, 5, if (mode == PlanMode.ACHIEVEMENT) 0 else 60)
        fun plan() = (repo.planner.read(scope) as PlanRead.Ready).plan
        fun restart() { files.replaceAll { _, old -> FixturePreferences(old.file) }; repo = LocalDemoRepository(false, ::preferences) }
    }
    @Test fun planAndPartialManualProgressSurviveRestartWithoutRewards() {
        val f = Fixture(); val t = f.task(); val user = f.repo.state.value.user
        assertEquals(PlanWrite.SAVED, f.repo.planner.save(f.scope, 0, "add", t))
        assertEquals(PlanWrite.SAVED, f.repo.planner.progress(f.scope, 1, "progress", t.id, 2, 0))
        f.restart(); assertEquals(user, f.repo.state.value.user)
        assertEquals(2, f.plan().tasks.single().done); assertEquals(2, f.plan().history.size)
        assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
        assertTrue(f.repo.state.value.completed.isEmpty())
    }
    @Test fun repeatedCommandDoesNotDoubleCountAndChangedPayloadConflicts() {
        val f = Fixture(); val t = f.task(); f.repo.planner.save(f.scope, 0, "add", t)
        assertEquals(PlanWrite.SAVED, f.repo.planner.progress(f.scope, 1, "same", t.id, 2, 0))
        assertEquals(PlanWrite.ALREADY_SAVED, f.repo.planner.progress(f.scope, 1, "same", t.id, 2, 0))
        assertEquals(PlanWrite.CONFLICT, f.repo.planner.progress(f.scope, 2, "same", t.id, 3, 0))
        assertEquals(2, f.plan().tasks.single().done)
    }
    @Test fun staleScopeIsRejectedEvenAfterReturningToSameOwner() {
        val f = Fixture(); val scope = f.scope; val t = f.task()
        f.repo.createLocalProfile("B", StudyStage.G9, true)
        assertEquals(PlanRead.OwnerChanged, f.repo.planner.read(scope))
        assertEquals(PlanWrite.OWNER_CHANGED, f.repo.planner.save(scope, 0, "old", t))
        assertTrue(f.plan().tasks.isEmpty())
        f.repo.selectLocalProfile(scope.ownerId)
        assertEquals(PlanWrite.OWNER_CHANGED, f.repo.planner.save(scope, 0, "old", t))
    }
    @Test fun wrongEditionStageAndUnknownContentCannotBeLinked() {
        val f = Fixture(); val t = f.task()
        for (ref in listOf(ContentRef(StudyStage.G9, StudentCatalog.G9_EDITION, "limits-practice"),
            ContentRef(StudyStage.BAC, "unknown", "limits-practice"),
            ContentRef(StudyStage.BAC, StudentCatalog.BAC_EDITION, "missing")))
            assertEquals(PlanWrite.INVALID, f.repo.planner.save(f.scope, 0, "bad", t.copy(lesson = ref)))
        assertEquals(PlanRead.OwnerChanged, f.repo.planner.read(f.scope.copy(edition = "unknown")))
    }
    @Test fun carryRequiresConsentClosesOriginalAndCopiesOnlyRemainder() {
        val f = Fixture(); val t = f.task(PlanMode.HYBRID)
        f.repo.planner.save(f.scope, 0, "add", t); f.repo.planner.progress(f.scope, 1, "partial", t.id, 2, 20)
        val next = StudyPeriods.next(t.start, t.period)
        assertEquals(PlanWrite.INVALID, f.repo.planner.carry(f.scope, 2, "move", t.id, next, false))
        assertEquals(PlanWrite.SAVED, f.repo.planner.carry(f.scope, 2, "move", t.id, next, true))
        val tasks = f.plan().tasks; assertTrue(tasks.first().closed)
        assertEquals(3, tasks.last().target); assertEquals(40, tasks.last().minutes)
        assertEquals(t.id, tasks.last().parent); assertEquals(0, tasks.last().done)
        assertEquals(PlanWrite.ALREADY_SAVED, f.repo.planner.carry(f.scope, 2, "move", t.id, next, true))
    }
    @Test fun failurePreservesDocumentAndRetryAfterRestartIsSafe() {
        val f = Fixture(); val t = f.task(); f.repo.planner.save(f.scope, 0, "add", t)
        val store = f.files.entries.single { it.key.startsWith("enmath_plan_") }.value
        val original = store.file.readBytes(); store.fail = true
        assertEquals(PlanWrite.STORAGE_ERROR, f.repo.planner.progress(f.scope, 1, "retry", t.id, 2, 0))
        assertArrayEquals(original, store.file.readBytes()); assertEquals(PlanRead.StorageError, f.repo.planner.read(f.scope))
        f.restart(); assertEquals(PlanWrite.SAVED, f.repo.planner.progress(f.scope, 1, "retry", t.id, 2, 0))
        assertEquals(2, f.plan().tasks.single().done)
    }
    @Test fun unknownVersionIsPreservedWithoutSeeding() {
        val f = Fixture(); val t = f.task(); f.repo.planner.save(f.scope, 0, "add", t)
        val store = f.files.entries.single { it.key.startsWith("enmath_plan_") }.value
        store.prefs.edit().putString("plan", "{\"version\":99}").commit()
        assertEquals(PlanRead.StorageError, f.repo.planner.read(f.scope))
        assertNotEquals(PlanWrite.SAVED, f.repo.planner.save(f.scope, 1, "bad", t))
        assertEquals("{\"version\":99}", store.prefs.getString("plan", null))
    }
    @Test fun editAndDeleteKeepPriorSnapshotsAndRejectLostUpdates() {
        val f = Fixture(); val t = f.task(); f.repo.planner.save(f.scope, 0, "add", t)
        assertEquals(PlanWrite.CONFLICT, f.repo.planner.save(f.scope, 0, "stale", t.copy(title = "Changed")))
        assertEquals(PlanWrite.SAVED, f.repo.planner.save(f.scope, 1, "edit", t.copy(title = "Changed")))
        assertEquals(PlanWrite.SAVED, f.repo.planner.remove(f.scope, 2, "delete", t.id))
        assertTrue(f.plan().tasks.isEmpty()); assertEquals(t, f.plan().history[1].tasks.single())
    }
    @Test fun linkedCompletionIsProjectionAndManualNeverCompletesLesson() {
        val f = Fixture(); val t = f.task().copy(lesson = StudentCatalog.ref(f.repo.state.value, "limits-practice"), target = 1)
        f.repo.planner.save(f.scope, 0, "add", t)
        f.repo.recordScopedLearning(f.scope, StudentCatalog.ref(f.repo.state.value, "limits-practice"), "fixture-complete") { it }
        assertEquals("LEARNING_EVENT", f.repo.planner.measured(t, f.repo.state.value).source)
        assertEquals(1, f.repo.planner.measured(t, f.repo.state.value).done)
        assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun periodsUseDamascusCalendarIncludingMondayAndMonthBoundary() {
        val sunday = java.time.Instant.parse("2026-10-25T22:00:00Z").toEpochMilli() // Monday 01:00 Damascus
        assertEquals(java.time.Instant.parse("2026-10-25T21:00:00Z").toEpochMilli(), StudyPeriods.start(sunday, PlanPeriod.WEEK))
        val month = StudyPeriods.start(sunday, PlanPeriod.MONTH)
        assertEquals(java.time.Instant.parse("2026-09-30T21:00:00Z").toEpochMilli(), month)
        assertEquals(java.time.Instant.parse("2026-10-31T21:00:00Z").toEpochMilli(), StudyPeriods.next(month, PlanPeriod.MONTH))
    }
}
