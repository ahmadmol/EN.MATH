package com.ahmadmol.enmath.core.model

enum class ContentStatus { INDEX_ONLY, PENDING_REVIEW, BLOCKED_EDITION_REVIEW, READY }
enum class ReviewStatus { PENDING, APPROVED, REJECTED }
enum class ReviewLevel { PAGE, LESSON, ACTIVITY, QUESTION }

/** Source identity is independent of a student's UUID/wallet and is never inferred from a title. */
data class G9SourceRef(
    val stage: StudyStage,
    val sourceBookId: String,
    val edition: String,
    val sourceSha256: String,
    val contentRevision: String,
    val unitId: String? = null,
    val lessonId: String? = null,
    val sourceIndexSha256: String? = null,
)

data class SourcePages(val from: Int, val to: Int) {
    init { require(from > 0 && to >= from) }
    operator fun contains(page: Int) = page in from..to
}

data class CatalogSource(val ref: G9SourceRef, val title: String, val subject: String, val pages: Int)
data class CatalogUnit(val ref: G9SourceRef, val order: Int, val title: String,
    val pages: SourcePages, val introductionPages: SourcePages, val reviewPages: SourcePages,
    val lessons: List<CatalogLesson>)
data class CatalogLesson(val ref: G9SourceRef, val order: Int, val title: String,
    val pages: SourcePages, val summary: String, val draftObjective: String,
    val status: ContentStatus = ContentStatus.INDEX_ONLY)
data class SourcePage(val ref: G9SourceRef, val pdfPage: Int, val role: String,
    val fullTextReviewed: Boolean, val figuresReviewed: Boolean, val exercisesTranscribed: Boolean)

/** Null page/figure/exercise number means unknown, never fabricated from a lesson's first page. */
data class ContentReview(
    val id: String,
    val ref: G9SourceRef,
    val level: ReviewLevel,
    val pdfPage: Int?,
    val rightsReview: ReviewStatus = ReviewStatus.PENDING,
    val mathReview: ReviewStatus = ReviewStatus.PENDING,
    val figureRequired: Boolean? = null,
    val sourceExerciseNumber: String? = null,
)

/** These types describe supported presentation, not evidence that a block exists in a lesson. */
enum class BookElementKind {
    prerequisite_check, guided_activity, concept_explanation, rule_card, worked_example,
    guided_practice, formative_check, independent_practice, unit_review, challenge_optional,
}
data class BookElementPattern(val kind: BookElementKind, val heading: String, val purpose: String)
data class ReviewedBookElement(val id: String, val kind: BookElementKind, val body: Copy,
    val review: ContentReview, val sourcePresenceVerified: Boolean)

