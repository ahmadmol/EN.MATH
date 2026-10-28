package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.*

/** Additive catalog adapter: BAC records/IDs and persistent learner snapshots remain unchanged. */
object G9SourceCatalog {
    val sources get() = G9CatalogData.sources
    val units get() = G9CatalogData.units
    val indexedLessons get() = units.flatMap { it.lessons }
    val pages get() = G9CatalogData.pages
    val patterns get() = G9CatalogData.patterns
    val draftReviews get() = G9CatalogData.draftReviews
    // The supplied page map does not verify individual activities. Do not generate blocks from patterns.
    val elements: List<ReviewedBookElement> = emptyList()
    val pageReviews get() = pages.map { ContentReview("${it.ref.sourceBookId}:page:${it.pdfPage}", it.ref, ReviewLevel.PAGE, it.pdfPage) }
    fun lesson(id: String) = indexedLessons.find { it.ref.lessonId == id }
    fun unit(id: String) = units.find { it.ref.unitId == id }
    fun contains(ref: G9SourceRef) = ref.stage == StudyStage.G9 && lesson(ref.lessonId.orEmpty())?.ref == ref
    fun ref(id: String): ContentRef? = lesson(id)?.ref?.let {
        ContentRef(StudyStage.G9, it.edition, id, 1, it.sourceBookId, ContentKind.SOURCE_INDEX, it.sourceSha256)
    }
    fun accepts(state: LearningState, ref: ContentRef) = state.catalogAvailable && state.storageIssue == null &&
        state.user?.studyStage == StudyStage.G9 && ref.kind == ContentKind.SOURCE_INDEX && this.ref(ref.lessonId) == ref
    fun visibleElements(ref: G9SourceRef) = elements.filter {
        it.review.ref == ref && it.sourcePresenceVerified && it.review.pdfPage != null &&
            pages.any { page -> page.ref == ref && page.pdfPage == it.review.pdfPage } &&
            it.review.mathReview == ReviewStatus.APPROVED && it.review.rightsReview == ReviewStatus.APPROVED
    }
    val readyLessonCount get() = indexedLessons.count { it.status == ContentStatus.READY }
    val educationalCoveragePercent get() = 100 * readyLessonCount / indexedLessons.size.coerceAtLeast(1)

    data class SearchResult(val title: String, val route: String)
    fun search(query: String): List<SearchResult> {
        val term = LearningRules.search(query)
        if (term.isBlank()) return emptyList()
        return sources.filter { LearningRules.search(it.title).contains(term) }.map { SearchResult(it.title, "book/${it.ref.sourceBookId}") } +
            units.filter { LearningRules.search(it.title).contains(term) }.map { SearchResult(it.title, "topic/${it.ref.unitId}") } +
            indexedLessons.filter { LearningRules.search("${it.title} ${it.summary} ${it.draftObjective}").contains(term) }
                .map { SearchResult(it.title, "lesson/${it.ref.lessonId}") }
    }

}
