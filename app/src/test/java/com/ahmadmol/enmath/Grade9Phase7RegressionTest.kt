package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.features.library.*
import com.ahmadmol.enmath.features.student.*
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject

class Grade9Phase7RegressionTest {
    @Test fun newDestinationsAreSecondaryAndGoalIdsAreEncoded() {
        val routes=com.ahmadmol.enmath.core.navigation.Routes
        assertEquals(5,routes.grade9Tabs.size)
        assertFalse(routes.Grade9Activity in routes.grade9Tabs)
        assertFalse(routes.Grade9Goal in routes.grade9Tabs)
        assertFalse(com.ahmadmol.enmath.core.navigation.showStudentNavigation(routes.Grade9Activity,listOf(routes.Grade9Activity),true,StudyStage.G9))
        assertEquals("g9/goal/A%2F1%20%2B",g9GoalRoute("A/1 +"))
    }
    private class Fixture {
        var state=LearningState(user=User("QA","",AccountType.Guest,"00000000-0000-0000-0000-000000000001",StudyStage.G9))
        val stores=mutableMapOf<String,FixturePreferences>()
        fun prefs(name:String)=stores.getOrPut(name){FixturePreferences()}.prefs
        var planner=PlannerRepository(::prefs){state}
        var study=Grade9StudyRepository(::prefs){state}
        var workspace=SourceWorkspaceRepository(::prefs){state}
        val scope get()=StudentCatalog.scope(state)!!
        fun plan()=(planner.read(scope) as PlanRead.Ready).plan
        fun data()=(study.read(scope) as G9StudyRead.Ready).data
        fun restart(){stores.replaceAll {_,old->FixturePreferences(old.file)};planner=PlannerRepository(::prefs){state};study=Grade9StudyRepository(::prefs){state};workspace=SourceWorkspaceRepository(::prefs){state}}
    }
    private val catalog=SourceCurricula.forStage(StudyStage.G9)
    private val unit=catalog.units.first()
    private val file=StudentFile("file","content://fixture/document","الجبر.pdf","application/pdf",1024,3)
    private fun task(id:String,ref:ContentRef?=null)=PlanTask(id,"Goal $id","Math",PlanPeriod.WEEK,StudyPeriods.start(System.currentTimeMillis(),PlanPeriod.WEEK),PlanMode.ACHIEVEMENT,1,lesson=ref)
    @Test fun unitPinIsOneAtomicWriteAndDeduplicatesIndividualPins() {
        val f=Fixture();val before=f.state
        assertEquals(PlanWrite.SAVED,f.planner.pinSource(f.scope,unit.researches.first().ref))
        val store=f.stores.entries.single {it.key.startsWith("enmath_plan_")}.value
        val commits=store.commitCount
        assertEquals(PlanWrite.SAVED,f.planner.pinGrade9Unit(f.scope,unit.id))
        assertEquals(commits+1,store.commitCount)
        val snapshot=f.plan()
        assertEquals(unit.researches.size,snapshot.tasks.size)
        assertEquals(PlanWrite.ALREADY_SAVED,f.planner.pinGrade9Unit(f.scope,unit.id))
        assertEquals(snapshot,f.plan());assertTrue(snapshot.tasks.all {it.done==0 && it.spent==0})
        assertEquals(before,f.state)
        f.restart();assertEquals(snapshot,f.plan())
    }
    @Test fun failedBatchCommitDoesNotLeaveAPartialUnit() {
        val f=Fixture();f.plan()
        f.stores.entries.single {it.key.startsWith("enmath_plan_")}.value.fail=true
        assertEquals(PlanWrite.STORAGE_ERROR,f.planner.pinGrade9Unit(f.scope,unit.id))
        f.restart();assertTrue(f.plan().tasks.isEmpty());assertTrue(f.plan().history.isEmpty())
    }
    @Test fun repinningAUnitRestoresOnlyMissingGoalsAfterDeletion() {
        val f=Fixture();f.planner.pinGrade9Unit(f.scope,unit.id)
        val removed=f.plan().tasks.first()
        f.planner.remove(f.scope,f.plan().revision,"remove-one",removed.id)
        assertEquals(PlanWrite.SAVED,f.planner.pinGrade9Unit(f.scope,unit.id))
        assertEquals(unit.researches.size,f.plan().tasks.size)
        assertEquals(unit.researches.size,f.plan().tasks.map {it.id}.distinct().size)
        assertEquals(PlanWrite.ALREADY_SAVED,f.planner.pinGrade9Unit(f.scope,unit.id))
    }
    @Test fun oldOwnerCommandsCannotChangeAnotherProfile() {
        val f=Fixture();f.study.addFile(f.scope,file);val old=f.scope
        f.state=f.state.copy(user=f.state.user!!.copy(localId="00000000-0000-0000-0000-000000000002"),generation=1)
        assertEquals(PlanWrite.OWNER_CHANGED,f.planner.pinGrade9Unit(old,unit.id))
        assertEquals(PlanWrite.OWNER_CHANGED,f.study.organizeFile(old,file.id,unit.bookId,unit.id,null,true))
        assertTrue(f.data().files.isEmpty());assertTrue(f.plan().tasks.isEmpty())
    }
    @Test fun invalidOrOtherStageUnitsCannotBePinned() {
        val f=Fixture()
        assertEquals(PlanWrite.INVALID,f.planner.pinGrade9Unit(f.scope,"not-a-unit"))
        f.state=f.state.copy(user=f.state.user!!.copy(studyStage=StudyStage.BAC),generation=1)
        assertEquals(PlanWrite.INVALID,f.planner.pinGrade9Unit(f.scope,unit.id))
    }
    @Test fun legacyFilesRemainReadableAndClassificationSurvivesRestart() {
        val f=Fixture();f.study.addFile(f.scope,file)
        val prefs=f.stores.entries.single {it.key.startsWith("enmath_g9_study_")}.value.prefs
        val doc=JSONObject(prefs.getString("study",null)!!);val stored=doc.getJSONArray("files").getJSONObject(0)
        listOf("bookId","unitId","researchId","favorite").forEach(stored::remove)
        prefs.edit().putString("study",doc.toString()).commit();f.restart()
        assertEquals(file,f.data().files.single())
        assertEquals(PlanWrite.SAVED,f.study.organizeFile(f.scope,file.id,unit.bookId,unit.id,unit.researches.first().id,true))
        f.restart();val classified=f.data().files.single()
        assertTrue(classified.favorite);assertEquals(file.uri,classified.uri);assertEquals(3,classified.page)
        assertEquals(unit.researches.first().id,classified.researchId)
    }
    @Test fun classificationRejectsDanglingAndCrossUnitLinksWithoutMutation() {
        val f=Fixture();f.study.addFile(f.scope,file)
        assertEquals(PlanWrite.INVALID,f.study.organizeFile(f.scope,file.id,null,"unknown",null,true))
        assertEquals(PlanWrite.INVALID,f.study.organizeFile(f.scope,file.id,unit.bookId,unit.id,catalog.units.last().researches.first().id,true))
        assertEquals(listOf(file),f.data().files)
    }
    @Test fun favoritesAndSourceFiltersCombineWithFileTypeAndSearch() {
        val classified=file.copy(bookId=unit.bookId,unitId=unit.id,researchId=unit.researches.first().id,favorite=true)
        val selection=G9ResourceSelection(unit.bookId,unit.id,unit.researches.first().id)
        val found=grade9Files(listOf(file,classified.copy(id="second")),"الجبر",G9FileFilter.PDF).filter {selection.matches(it)&&it.favorite}
        assertEquals(listOf("second"),found.map{it.id})
        assertTrue(selection.matches(unit.researches.first()));assertFalse(selection.matches(catalog.units.last().researches.first()))
    }
    @Test fun goalHistoryIncludesUndoAndExcludesOtherGoalEdits() {
        val f=Fixture();val a=task("a");val b=task("b")
        f.planner.save(f.scope,0,"a",a);f.planner.save(f.scope,1,"b",b)
        f.planner.progress(f.scope,2,"done-a",a.id,1,0);f.planner.undo(f.scope,3,"undo")
        val history=grade9GoalChanges(f.plan(),"a")
        assertEquals(listOf("UNDO","MANUAL","SAVE"),history.map{it.action})
        assertEquals(1,history.first().before!!.done);assertEquals(0,history.first().after!!.done)
        f.restart();assertEquals(history,grade9GoalChanges(f.plan(),"a"))
    }
    @Test fun recommendationUsesDraftThenRealMistakeThenCurrentSourceGoal() {
        val plan=StudyPlan(tasks=listOf(task("planned",unit.researches.first().ref)))
        val now=System.currentTimeMillis()
        assertEquals("g9/practice",grade9NextStudy(G9StudyData(draft=G9PracticeDraft("draft")),plan,null,now).route)
        val q=Grade9Pilot.questions.first()
        val attempt=G9SavedAttempt(Attempt(Grade9Pilot.practiceId,mapOf(q.id to "6/8"),0,4,1,now,Grade9Pilot.revision,StudyStage.G9,"attempt"))
        assertEquals("review",grade9NextStudy(G9StudyData(attempts=listOf(attempt)),plan,null,now).route)
        assertEquals("g9/reference/${unit.researches.first().id}",grade9NextStudy(G9StudyData(),plan,null,now).route)
        val stale=StudyPlan(tasks=listOf(task("stale",unit.researches.first().ref.copy(sourceSha256="outdated"))))
        assertEquals("lesson/${Grade9Pilot.lessonId}",grade9NextStudy(G9StudyData(),stale,null,now).route)
        assertEquals("lesson/${Grade9Pilot.lessonId}",grade9NextStudy(G9StudyData(),StudyPlan(),null,now).route)
    }
    @Test fun activityUsesActualTimestampsAndRejectsWrongWalletOwner() {
        val reward=GemRewardEntry("real",GemReward.PRACTICE,5,123,"day","week")
        val account=GemAccount("owner",100,rewards=listOf(reward))
        assertTrue(grade9Activity(G9StudyData(),StudyPlan(),account,"other").isEmpty())
        val events=grade9Activity(G9StudyData(),StudyPlan(),account,"owner")
        assertEquals(1,events.size);assertEquals(123L,events.single().at);assertEquals(0,account.balance)
        assertTrue(grade9Activity(G9StudyData(),StudyPlan(),null,null).isEmpty())
    }
    @Test fun savedReferencesRestoreAndAreFilteredWithoutRewritingBookmarks() {
        val f=Fixture();val ref=unit.researches.first()
        assertEquals(PlanWrite.SAVED,f.workspace.bookmark(f.scope,ref.ref,true));f.restart()
        val saved=(f.workspace.read(f.scope) as SourceWorkspaceRead.Ready).workspace
        assertEquals(setOf(ref.id),saved.savedIds)
        assertTrue(G9ResourceSelection(unit.bookId,unit.id,ref.id).matches(ref))
        assertEquals(PlanWrite.SAVED,f.workspace.bookmark(f.scope,ref.ref,false));f.restart()
        assertTrue((f.workspace.read(f.scope) as SourceWorkspaceRead.Ready).workspace.savedIds.isEmpty())
    }
}
