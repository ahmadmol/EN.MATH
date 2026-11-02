package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class PlannerStageModeMatrixTest(private val stage: StudyStage, private val period: PlanPeriod, private val mode: PlanMode) {
    @Test fun completePlanLifecycleRemainsDurableAndSeparateForEveryStagePeriodAndMode() {
        val files=mutableMapOf<String,FixturePreferences>()
        val preferences={name:String -> files.getOrPut(name) { FixturePreferences() }.prefs}
        var r=LocalDemoRepository(false,preferences)
        val originalGlobal=files.getValue("enmath_demo").file.readBytes()
        val owner=r.createLocalProfile("",stage,true)
        val scope=StudentCatalog.scope(r.state.value)!!
        fun plan()=(r.planner.read(StudentCatalog.scope(r.state.value)!!) as PlanRead.Ready).plan
        val task=PlanTask("goal","Study independently","Physics",period,StudyPeriods.start(1793088000000L,period),mode,
            if(mode==PlanMode.TIME) 0 else 3,if(mode==PlanMode.ACHIEVEMENT) 0 else 30)
        assertEquals(PlanWrite.SAVED,r.planner.save(scope,0,"create",task))
        val done=if(mode==PlanMode.TIME) 0 else 1;val spent=if(mode==PlanMode.ACHIEVEMENT) 0 else 5
        assertEquals(PlanWrite.SAVED,r.planner.progress(scope,1,"manual",task.id,done,spent))
        val edited=plan().tasks.single().copy(title="Revised goal",target=if(mode==PlanMode.TIME) 0 else 2,minutes=if(mode==PlanMode.ACHIEVEMENT) 0 else 20)
        assertEquals(PlanWrite.SAVED,r.planner.save(scope,2,"edit",edited))
        assertEquals(done,plan().tasks.single().done);assertEquals(spent,plan().tasks.single().spent)
        assertEquals(PlanWrite.SAVED,r.planner.carry(scope,3,"carry",task.id,StudyPeriods.next(task.start,period),true))
        assertEquals(PlanWrite.ALREADY_SAVED,r.planner.carry(scope,3,"carry",task.id,StudyPeriods.next(task.start,period),true))
        val carried=plan().tasks.last();assertEquals(task.id,carried.parent)
        assertEquals((edited.target-done).coerceAtLeast(0),carried.target)
        assertEquals((edited.minutes-spent).coerceAtLeast(0),carried.minutes)
        assertEquals(PlanWrite.SAVED,r.planner.remove(scope,4,"remove",task.id))
        assertEquals(5,plan().history.size);assertEquals(listOf(carried),plan().tasks)
        r.createLocalProfile("Other",if(stage==StudyStage.G9) StudyStage.BAC else StudyStage.G9,true)
        assertTrue(plan().tasks.isEmpty());assertEquals(PlanRead.OwnerChanged,r.planner.read(scope))
        r.selectLocalProfile(owner.id);files.replaceAll { _,f -> FixturePreferences(f.file) };r=LocalDemoRepository(false,preferences)
        assertEquals(owner.id,r.state.value.user?.localId);assertEquals(listOf(carried),plan().tasks)
        assertTrue(r.state.value.product.pendingGemEvents.isEmpty());assertTrue(r.state.value.completed.isEmpty())
        assertArrayEquals(originalGlobal,files.getValue("enmath_demo").file.readBytes())
    }
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}-{1}-{2}") fun variants():List<Array<Any>> =
            StudyStage.entries.flatMap { stage -> PlanPeriod.entries.flatMap { period -> PlanMode.entries.map { mode -> arrayOf(stage,period,mode) } } }
    }
}
