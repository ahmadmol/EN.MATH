package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.sourcePathEntries
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class SourceStudentExperienceTest {
    private class Stores(stage: StudyStage = StudyStage.G9) {
        val files = mutableMapOf<String, FixturePreferences>()
        val preferences = { name: String -> files.getOrPut(name) { FixturePreferences() }.prefs }
        var repo = LocalDemoRepository(false, preferences)
        init { repo.createLocalProfile("A", stage, false) }
        val state get() = repo.state.value
        val scope get() = StudentCatalog.scope(state)!!
        val first get() = SourceCurricula.forStage(scope.stage).researches.first().ref
        fun restart() { files.replaceAll { _, old -> FixturePreferences(old.file) }; repo = LocalDemoRepository(false, preferences) }
        fun workspace() = (repo.sourceWorkspace.read(scope) as SourceWorkspaceRead.Ready).workspace
        fun plan() = (repo.planner.read(scope) as PlanRead.Ready).plan
    }
    @Test fun grade9BooksHaveExactSourceSequenceAndIdentity() {
        val c = SourceCurricula.forStage(StudyStage.G9)
        assertEquals(listOf(6,4), c.books.map { it.units.size }); assertEquals(listOf(19,13), c.books.map { it.researches.size })
        assertEquals(G9SourceCatalog.indexedLessons.map { it.ref.lessonId }, c.researches.map { it.id })
        c.researches.forEach { assertEquals(G9SourceCatalog.ref(it.id), it.ref); assertEquals("lesson/${it.id}", it.route) }
    }
    @Test fun bacSecondBookNeverFallsBackToBookOneOrDemo() {
        val c = SourceCurricula.forStage(StudyStage.BAC); val b = c.books[1]
        assertEquals(listOf(7,7), c.books.map { it.units.size }); assertEquals(listOf(34,29), c.books.map { it.researches.size })
        assertEquals(29, b.researches.size)
        b.researches.forEach { assertEquals(b.id, it.ref.sourceId); assertEquals(it, c.unit(it.unitId)!!.researches.find { r -> r.id == it.id }) }
        assertTrue(c.researches.none { DemoContent.lesson(it.id) != null })
    }
    @Test fun everySourceHasTheSameTitleAndIdInPathBookUnitAndSearch() {
        val allIds = mutableSetOf<String>()
        StudyStage.entries.forEach { stage ->
            val c = SourceCurricula.forStage(stage); val path = sourcePathEntries(c)
            assertEquals(path.size, path.map { it.key }.distinct().size)
            assertEquals(c.researches, path.mapNotNull { it.research })
            c.researches.forEach { r ->
                assertTrue(allIds.add(r.id)); assertTrue(c.search(r.title).any { it.second == r.route })
                assertEquals(r, c.book(c.unit(r.unitId)!!.bookId)!!.researches.find { it.id == r.id })
            }
        }
        assertEquals(95, allIds.size)
    }
    @Test fun lazyStationOrderIsStableUnderReverseAndRepeatedReads() {
        StudyStage.entries.forEach { stage ->
            val c = SourceCurricula.forStage(stage); val first = sourcePathEntries(c)
            first.asReversed().forEach { entry -> assertEquals(entry, sourcePathEntries(c).single { it.key == entry.key }) }
            c.units.forEach { unit ->
                val nodes = first.filter { it.research?.unitId == unit.id }
                assertEquals(unit.researches.indices.toList(), nodes.map { it.station })
                assertFalse(nodes.last().hasNext)
            }
        }
    }
    @Test fun unknownAndCrossStageIdsHaveNoFallback() {
        val g = SourceCurricula.forStage(StudyStage.G9); val b = SourceCurricula.forStage(StudyStage.BAC)
        listOf("missing", "limits", "limits-practice", b.books[0].id).forEach { assertNull(g.book(it)); assertNull(g.unit(it)); assertNull(g.research(it)) }
        assertNull(b.research(g.researches.first().id)); assertNull(b.unit("missing"))
    }
    @Test fun searchNormalizesArabicAndWordSpacingWithoutCrossStageResults() {
        val g = SourceCurricula.forStage(StudyStage.G9)
        assertTrue(g.search("  أَعْدَاد   وَكُسُور  ").any { it.second == "topic/G9-ALG-U01" })
        assertTrue(g.search("النهايات").isEmpty()); assertTrue(g.search("  ").isEmpty())
    }
    @Test fun visitsAndBookmarksSurviveRestartAndDoNotWriteLearning() {
        StudyStage.entries.forEach { stage ->
            val f = Stores(stage); val before = f.state
            assertEquals(PlanWrite.SAVED, f.repo.sourceWorkspace.visit(f.scope, f.first))
            assertEquals(PlanWrite.SAVED, f.repo.sourceWorkspace.bookmark(f.scope, f.first, true))
            assertEquals(PlanWrite.ALREADY_SAVED, f.repo.sourceWorkspace.bookmark(f.scope, f.first, true))
            val workspace = f.workspace(); f.restart()
            assertEquals(workspace, f.workspace()); assertEquals(before.product, f.state.product); assertEquals(before.user, f.state.user)
            assertEquals(before.completed, f.state.completed); assertEquals(before.lastLessonId, f.state.lastLessonId)
            assertEquals(PlanWrite.SAVED, f.repo.sourceWorkspace.bookmark(f.scope, f.first, false))
            assertTrue(f.workspace().savedIds.isEmpty()); assertEquals(workspace.visitedId, f.workspace().visitedId)
        }
    }
    @Test fun eachBookRestoresItsOwnLastSourceReferenceAfterRestart() {
        StudyStage.entries.forEach { stage ->
            val f=Stores(stage); val catalog=SourceCurricula.forStage(stage)
            catalog.books.forEach { book -> assertEquals(PlanWrite.SAVED,f.repo.sourceWorkspace.visit(f.scope,book.researches.last().ref)) }
            f.restart()
            assertEquals(catalog.books.associate { it.id to it.researches.last().id },f.workspace().lastVisitedByBook)
        }
    }
    @Test fun stagePalettesMeetNormalTextContrastInBothThemes() {
        StudyStage.entries.forEach { stage -> listOf(false,true).forEach { dark ->
            val c=com.ahmadmol.enmath.core.design.studentColorScheme(dark,stage)
            listOf(c.primary to c.onPrimary,c.surface to c.onSurface,c.surface to c.onSurfaceVariant,
                c.primaryContainer to c.onPrimaryContainer,c.secondaryContainer to c.onSecondaryContainer).forEach { (a,b) ->
                fun lum(color: androidx.compose.ui.graphics.Color): Float {
                    fun linear(v: Float)=if(v <= .04045f) v/12.92f else Math.pow(((v+.055f)/1.055f).toDouble(),2.4).toFloat()
                    return .2126f*linear(color.red)+.7152f*linear(color.green)+.0722f*linear(color.blue)
                }
                assertTrue("$stage dark=$dark",(maxOf(lum(a),lum(b))+.05f)/(minOf(lum(a),lum(b))+.05f)>=4.5f)
            }
        } }
    }
    @Test fun staleScopeCannotReadOrWriteEvenAfterSwitchingBack() {
        val f = Stores(); val firstOwner = f.scope; val ref = f.first
        f.repo.sourceWorkspace.bookmark(firstOwner, ref, true)
        val bac = f.repo.createLocalProfile("B", StudyStage.BAC, true)
        assertEquals(SourceWorkspaceRead.OwnerChanged, f.repo.sourceWorkspace.read(firstOwner))
        assertEquals(PlanWrite.OWNER_CHANGED, f.repo.sourceWorkspace.visit(firstOwner, ref))
        assertTrue(f.workspace().savedIds.isEmpty())
        f.repo.selectLocalProfile(firstOwner.ownerId)
        assertEquals(PlanWrite.OWNER_CHANGED, f.repo.sourceWorkspace.bookmark(firstOwner, ref, false))
        assertEquals(setOf(ref.lessonId), f.workspace().savedIds)
    }
    @Test fun invalidSourceRevisionEditionHashOrKindCannotBeBookmarked() {
        val f = Stores(); val ref = f.first
        listOf(ref.copy(edition="wrong"),ref.copy(revision=42),ref.copy(sourceSha256="wrong"),ref.copy(sourceId="wrong"),
            ref.copy(stage=StudyStage.BAC),ref.copy(kind=ContentKind.LESSON),ref.copy(lessonId="missing")).forEach {
            assertEquals(PlanWrite.INVALID, f.repo.sourceWorkspace.bookmark(f.scope,it,true))
        }
        assertEquals(SourceWorkspace(), f.workspace())
    }
    @Test fun corruptOrNewerWorkspaceIsNeverReplaced() {
        val f = Stores(); f.repo.sourceWorkspace.visit(f.scope, f.first)
        val store = f.files.entries.single { it.key.startsWith("enmath_source_workspace_") }.value
        listOf("{", "{\"version\":99}").forEach { raw ->
            store.prefs.edit().putString("workspace",raw).commit()
            assertEquals(SourceWorkspaceRead.StorageError,f.repo.sourceWorkspace.read(f.scope))
            assertEquals(PlanWrite.STORAGE_ERROR,f.repo.sourceWorkspace.visit(f.scope,f.first))
            assertEquals(raw,store.prefs.getString("workspace",null))
        }
    }
    @Test fun failedWorkspaceCommitPreservesBytesAndRequiresRestart() {
        val f = Stores(); f.repo.sourceWorkspace.visit(f.scope,f.first)
        val store = f.files.entries.single { it.key.startsWith("enmath_source_workspace_") }.value
        val bytes=store.file.readBytes(); store.fail=true
        assertEquals(PlanWrite.STORAGE_ERROR,f.repo.sourceWorkspace.bookmark(f.scope,f.first,true))
        assertArrayEquals(bytes,store.file.readBytes()); assertEquals(SourceWorkspaceRead.StorageError,f.repo.sourceWorkspace.read(f.scope))
        f.restart(); assertTrue(f.workspace().savedIds.isEmpty())
        assertEquals(PlanWrite.SAVED,f.repo.sourceWorkspace.bookmark(f.scope,f.first,true))
    }
    @Test fun sourceWorkspaceNeverMutatesTeacherDocument() {
        val f = Stores(); val prefs=f.preferences("enmath_demo"); ProductCodec.save(prefs,ProductSeed.seed())
        val before=prefs.getString("product_v1",null)
        f.repo.sourceWorkspace.visit(f.scope,f.first); f.repo.sourceWorkspace.bookmark(f.scope,f.first,true)
        f.restart(); assertEquals(before,prefs.getString("product_v1",null))
    }
    private fun task(f: Stores) = PlanTask("goal", "Review", "Math", PlanPeriod.WEEK,
        StudyPeriods.start(Instant.parse("2026-10-28T12:00:00Z").toEpochMilli(),PlanPeriod.WEEK), PlanMode.ACHIEVEMENT,2,lesson=f.first)
    @Test fun planCompletionUndoAndDeleteUndoPersistWithStableReceipts() {
        val f=Stores(); val task=task(f); val before=f.state
        assertEquals(PlanWrite.SAVED,f.repo.planner.save(f.scope,0,"add",task))
        assertEquals(PlanWrite.SAVED,f.repo.planner.progress(f.scope,1,"done",task.id,2,0))
        assertTrue(f.plan().tasks.single().isAchieved())
        assertEquals(PlanWrite.SAVED,f.repo.planner.undo(f.scope,2,"undo-progress"))
        assertEquals(PlanWrite.ALREADY_SAVED,f.repo.planner.undo(f.scope,2,"undo-progress"))
        assertEquals(0,f.plan().tasks.single().done)
        assertEquals(PlanWrite.INVALID,f.repo.planner.undo(f.scope,3,"double-undo"))
        assertEquals(PlanWrite.SAVED,f.repo.planner.remove(f.scope,3,"delete",task.id))
        assertTrue(f.plan().tasks.isEmpty())
        assertEquals(PlanWrite.SAVED,f.repo.planner.undo(f.scope,4,"undo-delete"))
        val plan=f.plan(); f.restart(); assertEquals(plan,f.plan()); assertEquals(before.product,f.state.product)
    }
    @Test fun undoRejectsStaleRevisionAndOwner() {
        val f=Stores();f.repo.planner.save(f.scope,0,"add",task(f))
        assertEquals(PlanWrite.CONFLICT,f.repo.planner.undo(f.scope,0,"stale"))
        val scope=f.scope;f.repo.createLocalProfile("B",StudyStage.BAC,false)
        assertEquals(PlanWrite.OWNER_CHANGED,f.repo.planner.undo(scope,1,"stale-owner"))
    }
    @Test fun dayWeekAndMonthReadTheSameWeeklyGoalWithoutCarryOrMutation() {
        val f=Stores();val task=task(f); f.repo.planner.save(f.scope,0,"add",task)
        val plan=f.plan(); val at=Instant.parse("2026-10-28T12:00:00Z").toEpochMilli()
        PlanPeriod.entries.forEach { assertEquals(listOf(task),planTasksInView(plan,it,at)) }
        val next=StudyPeriods.next(task.start,PlanPeriod.WEEK)
        assertTrue(planTasksInView(plan,PlanPeriod.WEEK,next).isEmpty());assertEquals(plan,f.plan())
    }
    @Test fun hybridCompletionNeedsBothDimensionsAndTimeHasNoAchievementUnits() {
        val f=Stores(); val task=task(f)
        assertFalse(task.copy(mode=PlanMode.HYBRID,done=2,minutes=30,spent=29).isAchieved())
        assertTrue(task.copy(mode=PlanMode.HYBRID,done=2,minutes=30,spent=30).isAchieved())
        assertTrue(task.copy(mode=PlanMode.TIME,done=0,minutes=30,spent=30).isAchieved())
    }
    @Test fun resultHistoryResolvesRequestedAttemptAndRejectsMissingOrOtherProfile() {
        val f=Stores(StudyStage.BAC);val app=AppViewModel(f.repo);val set=f.state.practiceSets.first();val ticket=LearningRules.sessionOwner(f.state)
        assertTrue(app.recordPractice(set.id,emptyMap(),5,ticket,"first"))
        val correct=set.questionIds.associateWith { id -> f.state.questions.single { it.id==id }.answer }
        assertTrue(app.recordPractice(set.id,correct,10,ticket,"second"))
        assertEquals("first",savedPracticeAttempt(f.state,set.id,"first")?.commandId)
        assertEquals(0,savedPracticeAttempt(f.state,set.id,"first")?.correct)
        assertEquals("second",savedPracticeAttempt(f.state,set.id)?.commandId)
        assertNull(savedPracticeAttempt(f.state,set.id,"missing"))
        f.repo.createLocalProfile("B",StudyStage.G9,false)
        assertNull(savedPracticeAttempt(f.state,set.id,"first"))
    }
    @Test fun grade9ToolHistoryIsScopedAndNeverGrantsRewards() {
        val f=Stores();val app=AppViewModel(f.repo);val before=f.state;val ticket=LearningRules.sessionOwner(before)
        val id=app.history("2x+4=10","equations",ticket); assertTrue(id.isNotBlank())
        assertTrue(app.saveGraph("x^2",ticket));assertFalse(app.saveGraph("unsupported",ticket))
        assertEquals(before.product.pendingGemEvents,f.state.product.pendingGemEvents);assertTrue(f.state.completed.isEmpty())
        f.repo.createLocalProfile("B",StudyStage.BAC,true)
        assertTrue(f.state.product.history.isEmpty());assertEquals("",app.history("x","equations",ticket))
    }
}
