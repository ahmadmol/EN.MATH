package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.interactive.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class LocalGemsPolicyTest {
    @Test fun historicalWeeklyAndMasteryBalancesSurviveWhileAllNewGrantsAreRejected() {
        val rewards = listOf(
            GemRewardEntry("WEEKLY:old", GemReward.WEEKLY, 15, now, LocalGemsPolicy.day(now), LocalGemsPolicy.week(now)),
            GemRewardEntry("MASTERY:old", GemReward.MASTERY, 20, now, LocalGemsPolicy.day(now), LocalGemsPolicy.week(now)))
        val old = account().copy(balance = 35, rewards = rewards).also(LocalGemsPolicy::validate)
        assertEquals(old, LocalGemsPolicy.award(old, GemReward.WEEKLY, "new", now + 86_400_000))
        assertEquals(old, LocalGemsPolicy.award(old, GemReward.MASTERY, "new", now + 86_400_000))
        assertEquals(35, old.balance)
    }
    private val now=Instant.parse("2026-10-20T09:00:00Z").toEpochMilli()
    private fun account()=GemAccount("student@example.test",now)
    private val state get()=LearningState(completed=emptySet(),product=ProductState())
    private fun earned40(): GemAccount=(1..4).fold(account()) { a,i -> LocalGemsPolicy.award(a,GemReward.LESSON,"lesson$i",now) }
    private fun answers(id:String)=ProductSeed.sets.first {it.id==id}.questionIds.associateWith {ProductSeed.question(it)!!.answer}
    @Test fun identityIsCanonicalStudentEmail() {
        assertEquals("a@example.test",LocalGemsPolicy.owner(User("A"," A@Example.TEST ",AccountType.Student)))
        assertNull(LocalGemsPolicy.owner(User("A","",AccountType.Student)))
        assertNull(LocalGemsPolicy.owner(User("A","a@test",AccountType.Guest)))
        assertNull(LocalGemsPolicy.owner(User("A","a@test",AccountType.Teacher)))
        assertNull(LocalGemsPolicy.owner(null))
    }
    @Test fun missingOrForeignActivityOwnerIsRejected() {
        val a=User("A","a@test",AccountType.Student)
        assertFalse(LocalGemsPolicy.attributable(a,null))
        assertFalse(LocalGemsPolicy.attributable(a,"b@test"))
        assertTrue(LocalGemsPolicy.attributable(a,"a@test"))
        assertFalse(LocalGemsPolicy.attributable(a.copy(type=AccountType.Teacher),"a@test"))
    }
    @Test fun activationNeverSeedsCurrency() { assertEquals(0,account().balance);assertTrue(account().rewards.isEmpty()) }
    @Test fun excludesLegacyAchievementsAndOldEvents() {
        val old=account().copy(excluded=setOf("LESSON:old"))
        assertEquals(old,LocalGemsPolicy.award(old,GemReward.LESSON,"old",now))
        assertEquals(old,LocalGemsPolicy.award(old,GemReward.LESSON,"new",now-1))
    }
    @Test fun rewardReplayCannotMintAgain() {
        val first=LocalGemsPolicy.award(account(),GemReward.LESSON,"one",now)
        assertEquals(10,first.balance)
        assertEquals(first,LocalGemsPolicy.award(first,GemReward.LESSON,"one",now+86_400_000))
    }
    @Test fun dailyCapPartiallyGrantsAndConsumesRemainder() {
        var a=(1..3).fold(account()) {v,i->LocalGemsPolicy.award(v,GemReward.LESSON,"l$i",now)}
        a=LocalGemsPolicy.award(a,GemReward.PRACTICE,"practice",now)
        a=LocalGemsPolicy.award(a,GemReward.LESSON,"partial",now)
        assertEquals(40,a.balance);assertEquals(5,a.rewards.last().units)
        a=LocalGemsPolicy.award(a,GemReward.LESSON,"capped",now)
        assertEquals(0,a.rewards.last().units)
        assertEquals(a,LocalGemsPolicy.award(a,GemReward.LESSON,"capped",now+86_400_000))
    }
    @Test fun weeklyCapPersistsAcrossDays() {
        var a=account()
        for(day in 0..2) for(i in 1..4) a=LocalGemsPolicy.award(a,GemReward.LESSON,"$day-$i",now+day*86_400_000L)
        assertEquals(120,a.balance)
        a=LocalGemsPolicy.award(a,GemReward.LESSON,"weekly-capped-lesson",now+3*86_400_000L)
        assertEquals(0,a.rewards.last().units);LocalGemsPolicy.validate(a)
    }
    @Test fun damascusDayAndWeekBoundary() {
        val sunday=Instant.parse("2026-10-25T20:59:59Z").toEpochMilli();val monday=sunday+1000
        assertEquals("2026-10-25",LocalGemsPolicy.day(sunday));assertEquals("2026-10-26",LocalGemsPolicy.day(monday))
        assertEquals("2026-10-19",LocalGemsPolicy.week(sunday));assertEquals("2026-10-26",LocalGemsPolicy.week(monday))
    }
    @Test fun weekKeyDoesNotResetAtYearBoundary() {
        assertEquals(LocalGemsPolicy.week(Instant.parse("2025-12-31T12:00:00Z").toEpochMilli()),LocalGemsPolicy.week(Instant.parse("2026-01-01T12:00:00Z").toEpochMilli()))
    }
    @Test fun qualificationRequiresEveryAnswerAndEightyPercent() {
        val ids=(1..5).map(Int::toString);val all=ids.associateWith {"x"}
        assertTrue(LocalGemsPolicy.qualified(ids,all,4));assertFalse(LocalGemsPolicy.qualified(ids,all,3))
        assertFalse(LocalGemsPolicy.qualified(ids,all-"5",4));assertFalse(LocalGemsPolicy.qualified(ids,all+("5" to " "),4))
        assertFalse(LocalGemsPolicy.qualified(emptyList(),emptyMap(),0))
        assertFalse(LocalGemsPolicy.qualified(ids,all,6))
    }
    @Test fun unlockDeductsOnceAndReopeningIsFree() {
        val a=LocalGemsPolicy.unlock(earned40(),LocalGemCatalog.items.first(),now)
        assertEquals(0,a.balance);assertEquals(1,a.unlocks.size)
        assertEquals(a,LocalGemsPolicy.unlock(a,LocalGemCatalog.items.first(),now+1));LocalGemsPolicy.validate(a)
    }
    @Test(expected=InsufficientLocalGems::class) fun insufficientBalanceDoesNotUnlock() {
        LocalGemsPolicy.unlock(account(),LocalGemCatalog.items.first(),now)
    }
    @Test(expected=IllegalStateException::class) fun inconsistentCachedBalanceFailsClosed() {
        LocalGemsPolicy.validate(account().copy(balance=40))
    }
    @Test fun noCurrencyMovesBetweenOwners() {
        val a=earned40();val b=account().copy(owner="b@example.test")
        assertEquals(40,a.balance);assertEquals(0,b.balance);assertTrue(b.unlocks.isEmpty())
    }
    @Test fun actualLessonCheckpointIsRequired() {
        val lesson=DemoContent.books.flatMap { it.lessons }.first()
        assertFalse(LocalLearningGems.lessonVerified(state,lesson.id,""))
        assertFalse(LocalLearningGems.lessonVerified(state,lesson.id,"wrong"))
        assertTrue(LocalLearningGems.lessonVerified(state,lesson.id,lesson.checkpointAnswer))
        assertFalse(LocalLearningGems.lessonVerified(state,"unknown",lesson.checkpointAnswer))
    }
    @Test fun interactiveLessonNeedsAllExistingChecks() {
        val questions=LimitsLessonSequence.blocks(state).mapNotNull {it.question()}.distinctBy {it.id}
        val all=questions.associate {it.id to it.answer}
        assertTrue(LocalLearningGems.interactiveVerified(state,LimitsLessonSequence.lessonId,all))
        assertFalse(LocalLearningGems.interactiveVerified(state,LimitsLessonSequence.lessonId,all-questions.first().id))
        assertFalse(LocalLearningGems.interactiveVerified(state,LimitsLessonSequence.lessonId,all+(questions.first().id to "wrong")))
    }
    @Test fun practiceUsesRealGraderAndAllowsImprovementOnce() {
        val id=ProductSeed.sets.first().id;val all=answers(id)
        val wrong=all.mapValues {"incorrect"}
        assertTrue(LocalLearningGems.practice(account(),state,id,wrong,now).rewards.isEmpty())
        val reward=LocalLearningGems.practice(account(),state,id,all,now)
        assertEquals(5,reward.balance)
        assertEquals(5,LocalLearningGems.practice(reward,state,id,all,now+1).balance)
        assertTrue(LocalLearningGems.practice(account(),state,id,all-all.keys.first(),now).rewards.isEmpty())
    }
    @Test fun practiceThresholdUsesQuestionCountNotRoundedPercentage() {
        val id=ProductSeed.sets.first().id;val all=answers(id)
        val oneWrong=all+(all.keys.first() to "incorrect")
        assertEquals(5,LocalLearningGems.practice(account(),state,id,oneWrong,now).balance)
        val twoWrong=oneWrong+(all.keys.drop(1).first() to "incorrect")
        assertEquals(0,LocalLearningGems.practice(account(),state,id,twoWrong,now).balance)
    }
    @Test fun baselineBlocksImportedSharedAttemptsAndCompletions() {
        val original=LearningState();val id=ProductSeed.sets.first().id
        val old=original.copy(product=original.product.copy(attempts=listOf(Attempt(id,answers(id),6,6,60,now-1000))))
        val excluded=LocalLearningGems.baseline(old)
        assertTrue(excluded.containsAll(old.completed.map {"LESSON:$it"}));assertTrue("PRACTICE:$id" in excluded)
        assertEquals(0,LocalLearningGems.practice(account().copy(excluded=excluded),old,id,answers(id),now).balance)
    }
    @Test fun assignmentAnswersAreGradedAndDeduplicated() {
        val s=LearningState();val assignment=s.product.assignments.first()
        val all=assignment.questionIds.associateWith {s.product.question(it)!!.answer}
        val a=LocalLearningGems.assignment(account(),s,assignment.id,all,now)
        assertEquals(5,a.balance)
        assertEquals(a,LocalLearningGems.assignment(a,s,assignment.id,all,now+1))
        assertEquals(0,LocalLearningGems.assignment(account(),s,assignment.id,all-all.keys.first(),now).balance)
    }
    private fun masteredState(): LearningState {
        val topic=DemoContent.books.flatMap { it.topics }.first();val sets=ProductSeed.sets.filter {it.topicId==topic.id}
        return state.copy(completed=topic.lessons.map {it.id}.toSet(),product=ProductState(attempts=sets.map {Attempt(it.id,answers(it.id),it.questionIds.size,it.questionIds.size,90,now)}))
    }
    @Test fun foreignOrSeedMasteryCannotRewardThisOwner() {
        val s=masteredState();assertEquals(Mastery.Mastered,LearningRules.topic(s,s.topics.first().id).stage)
        assertEquals(0,LocalLearningGems.mastery(account(),s,now).balance)
    }
    @Test fun masteryEvidenceCannotMintUnapprovedNewGems() {
        val s=masteredState();val topic=s.topics.first()
        val own=account().copy(lessons=topic.lessons.map {it.id}.toSet(),assessments=ProductSeed.sets.filter {it.topicId==topic.id}.map {GemAssessment(it.id,it.topicId,it.quiz,1f)})
        val a=LocalLearningGems.mastery(own,s,now);assertEquals(own,a)
        assertEquals(a,LocalLearningGems.mastery(a,s,now+1))
        val blocked=own.copy(excluded=setOf("MASTERY:${topic.id}"));assertEquals(0,LocalLearningGems.mastery(blocked,s,now).balance)
    }
    private fun reviewFixture(): Pair<GemAccount,LearningState> {
        val lessons=DemoContent.books.flatMap { it.lessons }.distinctBy {it.checkpoint}.take(5).map {it.id}.toSet()
        return account().copy(lessons=lessons) to state.copy(completed=lessons)
    }
    @Test fun weeklyIsUnavailableWithoutRealLessonProvenance() {
        assertNull(LocalLearningGems.weekly(account(),LearningState(),now))
        val (a,s)=reviewFixture();assertNull(LocalLearningGems.weekly(a.copy(lessons=a.lessons.take(2).toSet()),s,now))
        assertNull(LocalLearningGems.weekly(a,s.copy(completed=emptySet()),now))
    }
    @Test fun weeklyUsesFiveExistingDistinctLessonCheckpoints() {
        val (a,s)=reviewFixture();val review=LocalLearningGems.weekly(a,s,now)!!
        assertEquals(5,review.questions.size);assertTrue(review.questions.map {it.lessonId}.toSet().size>=2)
        review.questions.forEach { source ->
            val lesson=s.lesson(source.lessonId)!!;assertEquals(lesson.checkpoint,source.question.expression);assertEquals(lesson.checkpointAnswer,source.question.answer)
        }
    }
    @Test fun weeklyReviewNeverAwardsNewGemsUnderD06() {
        val (a,s)=reviewFixture();val review=LocalLearningGems.weekly(a,s,now)!!;val all=review.questions.associate {it.question.id to it.question.answer}
        val four=all+(all.keys.first() to "wrong")
        val result=LocalLearningGems.finishWeekly(a,s,review,four,now)
        assertEquals(a,result);assertEquals(0,result.balance);assertTrue(result.rewards.isEmpty())
        assertEquals(result,LocalLearningGems.finishWeekly(result,s,review,all,now+1))
        assertEquals(a,LocalLearningGems.finishWeekly(a,s,review,all-all.keys.first(),now))
    }
    @Test fun staleWeeklyReviewCannotMintNextWeeksReward() {
        val (a,s)=reviewFixture();val review=LocalLearningGems.weekly(a,s,now)!!;val all=review.questions.associate {it.question.id to it.question.answer}
        assertEquals(a,LocalLearningGems.finishWeekly(a,s,review,all,now+7*86_400_000L))
    }
    @Test fun rewardAdaptersDoNotMutateLearningDataOrMastery() {
        val s=LearningState();val snapshot=s.copy();val before=s.topics.associate {it.id to LearningRules.topic(s,it.id)}
        LocalLearningGems.practice(account(),s,ProductSeed.sets.first().id,answers(ProductSeed.sets.first().id),now)
        assertEquals(snapshot,s);assertEquals(before,s.topics.associate {it.id to LearningRules.topic(s,it.id)})
    }
}
