package com.ahmadmol.enmath.core.localgems

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence
import com.ahmadmol.enmath.features.courses.interactive.question

data class GemReviewQuestion(val lessonId: String, val question: Question)
data class GemWeeklyReview(val week: String, val questions: List<GemReviewQuestion>)
object LocalLearningGems {
    fun baseline(state: LearningState): Set<String> = buildSet {
        state.completed.forEach { add("LESSON:$it") }
        state.product.attempts.forEach { add("PRACTICE:${it.setId}") }
        state.product.submissions.forEach { add("ASSIGNMENT:${it.assignmentId}") }
        state.topics.filter { LearningRules.topic(state,it.id).stage==Mastery.Mastered }.forEach { add("MASTERY:${it.id}") }
        state.product.pendingGemEvents.filter { it.owner == LearningRules.commandOwner(state) }.forEach {
            remove("${it.kind}:${it.contentId}")
        }
    }
    fun lessonVerified(state: LearningState, id: String, answer: String): Boolean {
        val lesson=state.lesson(id) ?: return false
        // Same checkpoint and acceptance condition as the existing LessonScreen, not a new quiz.
        return lesson.checkpoint.isNotBlank() && lesson.checkpointAnswer.isNotBlank() && MathAnswer.matches(lesson.checkpointAnswer, answer)
    }
    fun interactiveVerified(state: LearningState, id: String, answers: Map<String,String>): Boolean {
        if (state.lesson(id) == null) return false
        if(id!=LimitsLessonSequence.lessonId) return false
        val questions=LimitsLessonSequence.blocks(state).mapNotNull { it.question() }.distinctBy { it.id }
        return questions.isNotEmpty() && questions.all { !answers[it.id].isNullOrBlank() && LearningRules.correct(it,answers.getValue(it.id)) }
    }
    fun practice(account: GemAccount, state: LearningState, id: String, answers: Map<String,String>, at: Long): GemAccount {
        val set=state.practiceSets.find { it.id==id } ?: return account
        val questions=set.questionIds.mapNotNull { q -> state.questions.find { it.id == q } }
        if(questions.size!=set.questionIds.size || questions.isEmpty() || questions.any { answers[it.id].isNullOrBlank() }) return account
        val correct=questions.count { LearningRules.correct(it,answers.getValue(it.id)) }
        val accuracy=correct.toFloat()/questions.size
        val previous=account.assessments.find { it.id==id }
        val evidence=GemAssessment(id,set.topicId,set.quiz,if(set.quiz) maxOf(previous?.accuracy ?: 0f,accuracy) else accuracy)
        var next=account.copy(assessments=account.assessments.filterNot { it.id==id }+evidence)
        if(LocalGemsPolicy.qualified(set.questionIds,answers,correct)) next=LocalGemsPolicy.award(next,GemReward.PRACTICE,id,at)
        return mastery(next,state,at)
    }
    fun assignment(account: GemAccount, state: LearningState, id: String, answers: Map<String,String>, at: Long): GemAccount {
        val assignment=state.product.assignments.find { it.id==id } ?: return account
        val questions=assignment.questionIds.mapNotNull(state.product::question)
        if(questions.size!=assignment.questionIds.size) return account
        val correct=questions.count { LearningRules.correct(it,answers[it.id].orEmpty()) }
        return if(LocalGemsPolicy.qualified(assignment.questionIds,answers,correct))
            LocalGemsPolicy.award(account,GemReward.ASSIGNMENT,id,at) else account
    }
    fun mastery(account: GemAccount, state: LearningState, at: Long): GemAccount {
        var next=account
        state.topics.forEach { topic ->
            val practice=next.assessments.lastOrNull { it.topic==topic.id && !it.quiz }?.accuracy ?: 0f
            val quiz=next.assessments.filter { it.topic==topic.id && it.quiz }.maxOfOrNull { it.accuracy } ?: 0f
            // Require this owner's new evidence too: shared legacy/other-account scores cannot mint Gems.
            val owned=LearningRules.mastery(topic,next.lessons,practice,quiz)
            if(owned.stage==Mastery.Mastered && LearningRules.topic(state,topic.id).stage==Mastery.Mastered)
                next=LocalGemsPolicy.award(next,GemReward.MASTERY,topic.id,at)
        }
        return next
    }
    fun weekly(account: GemAccount, state: LearningState, at: Long): GemWeeklyReview? {
        val week=LocalGemsPolicy.week(at)
        if(account.rewards.any { it.key=="WEEKLY:$week" }) return null
        // Direct lesson -> existing checkpoint provenance; never infer provenance from a topic match.
        val questions=state.lessons.filter { it.id in account.lessons && it.id in state.completed && it.checkpoint.isNotBlank() && it.checkpointAnswer.isNotBlank() }
            .distinctBy { it.checkpoint.trim() to it.checkpointAnswer.trim() }.sortedBy { it.id }.take(5).map { lesson ->
                GemReviewQuestion(lesson.id,Question("weekly-check-${lesson.id}",state.topicOf(lesson.id)?.id.orEmpty(),
                    Copy("Recall this lesson's checkpoint", "راجع نقطة التحقق من هذا الدرس"),lesson.checkpoint,QuestionKind.Expression,emptyList(),lesson.checkpointAnswer,lesson.rule,lesson.explanation))
            }
        if(questions.size!=5 || questions.map { it.lessonId }.distinct().size<2) return null
        return GemWeeklyReview(week,questions)
    }
    fun finishWeekly(account: GemAccount, state: LearningState, review: GemWeeklyReview, answers: Map<String,String>, at: Long): GemAccount {
        // D06: educational review only. Historical WEEKLY entries remain readable.
        return account
    }
}
