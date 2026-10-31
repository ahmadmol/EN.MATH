package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class Grade9Phase6RegressionTest {
    private fun attempt(id:String, answers:Map<String,String>) = G9SavedAttempt(Attempt(
        Grade9Pilot.practiceId,answers,Grade9Pilot.questions.count { Grade9Pilot.correct(it,answers[it.id].orEmpty()) },
        4,1,1,Grade9Pilot.revision,StudyStage.G9,id))
    @Test fun skipsDoNotBecomeMistakesOrEraseEarlierWrongAnswers() {
        val q=Grade9Pilot.questions.first()
        val wrong=attempt("wrong",mapOf(q.id to "6/8"))
        val skipped=attempt("skip",emptyMap())
        assertEquals(listOf(q.id),grade9LatestMistakes(G9StudyData(attempts=listOf(wrong,skipped))).map {it.first.id})
        assertEquals("wrong",grade9LatestMistakes(G9StudyData(attempts=listOf(wrong,skipped))).single().second.record.commandId)
        assertTrue(grade9LatestMistakes(G9StudyData(attempts=listOf(skipped))).isEmpty())
    }
    @Test fun laterCorrectAnswerResolvesOnlyThatQuestion() {
        val a=Grade9Pilot.questions[0];val b=Grade9Pilot.questions[1]
        val old=attempt("old",mapOf(a.id to "6/8",b.id to "1/2"))
        val newer=attempt("new",mapOf(a.id to a.answer))
        assertEquals(listOf(b.id),grade9LatestMistakes(G9StudyData(attempts=listOf(old,newer))).map {it.first.id})
    }
    @Test fun previousQuestionPreservesLockedAnswerAndRestoresPositionFromRepository() {
        val state=LearningState(user=User("QA","",AccountType.Guest,"00000000-0000-0000-0000-000000000001",StudyStage.G9))
        val prefs=FixturePreferences().prefs;val repo=Grade9StudyRepository({prefs}){state}
        val scope=StudentCatalog.scope(state)!!;val jobs=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val vm=Grade9PracticeViewModel(Dispatchers.Unconfined,jobs){}
            vm.bind(scope,repo);val id=vm.draft.value!!.id
            vm.previous();assertEquals(0,vm.draft.value!!.index)
            vm.edit("6/8");vm.check();vm.next();assertEquals(1,vm.draft.value!!.index)
            vm.previous();vm.edit("3/4")
            assertEquals("6/8",vm.draft.value!!.answers[Grade9Pilot.questions.first().id])
            val restored=Grade9PracticeViewModel(Dispatchers.Unconfined,jobs){}
            restored.bind(scope,repo)
            assertEquals(id,restored.draft.value!!.id);assertEquals(0,restored.draft.value!!.index)
            assertEquals(scope,restored.boundOwner.value)
            assertNull(restored.finish())
        } finally {jobs.cancel()}
    }
}
