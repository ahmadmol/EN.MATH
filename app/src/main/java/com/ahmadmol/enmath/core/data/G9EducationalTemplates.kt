package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.Copy

/** Presentation contracts, not publishing authority. Editorial files are never loaded as releases. */
data class G9LessonTemplate(val ref: ContentRef, val revision: String, val title: Copy,
    val blocks: List<G9LessonBlock>, val examples: List<G9WorkedExample>, val summary: Copy) {
    val sectionCount get() = blocks.size + examples.size + 1
}

sealed interface G9AnswerContract {
    data class ReducedPositiveFraction(val numerator: Int, val denominator: Int) : G9AnswerContract
    data class ExactNumberDraft(val expected: String, val validationExpression: String) : G9AnswerContract
    data class ChoiceDraft(val options: List<Copy>, val expectedIndex: Int) : G9AnswerContract
    data class ManualRubric(val criteria: List<Copy>, val figureDescription: Copy? = null) : G9AnswerContract
}

data class G9QuestionTemplate(val id: String, val prompt: Copy, val math: String,
    val hint: Copy, val correction: Copy, val contract: G9AnswerContract)

/** Only the pilot's already-tested fraction contract has an automatic answer engine in this release. */
fun G9QuestionTemplate.hasImplementedGrader() = contract is G9AnswerContract.ReducedPositiveFraction
