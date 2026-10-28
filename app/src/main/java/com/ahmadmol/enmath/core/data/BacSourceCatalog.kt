package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.StudyStage

/** Source index alongside existing demo content. It cannot mint completion, attempts or reward IDs. */
object BacSourceCatalog {
    val books get() = BacSourceData.books
    val units get() = BacSourceData.units
    val lessons get() = BacSourceData.lessons
    val sections get() = BacSourceData.sections
    val pages get() = BacSourceData.pages
    val drafts get() = BacSourceData.drafts
    val legacyRelations get() = BacSourceData.legacyRelations
    val reviewedLessonCount get() = 0
    fun book(id: String) = books.find { it.id == id || it.existingBookId == id }
    fun unit(id: String) = units.find { it.id == id }
    fun lesson(id: String) = lessons.find { it.id == id }
    fun section(id: String) = sections.find { it.id == id }
    fun relatedLegacy(id: String) = legacyRelations.filter { id in it.relatedSourceLessonIds }.mapNotNull { DemoContent.lesson(it.legacyLessonId) }
    fun ref(id: String): ContentRef? = lesson(id)?.let { entry ->
        ContentRef(StudyStage.BAC, entry.edition, entry.id, 1, entry.sourceId,
            ContentKind.SOURCE_INDEX, book(entry.sourceId)?.sha256)
    }
    fun accepts(state: LearningState, ref: ContentRef): Boolean = state.catalogAvailable && state.storageIssue == null &&
        state.user?.studyStage == StudyStage.BAC && ref.kind == ContentKind.SOURCE_INDEX && this.ref(ref.lessonId) == ref
    fun title(ref: ContentRef) = lesson(ref.lessonId)?.title
    data class SearchResult(val title: String, val route: String)
    fun search(query: String): List<SearchResult> {
        val term = LearningRules.search(query)
        if (term.isBlank()) return emptyList()
        return books.filter { LearningRules.search(it.title).contains(term) }.map { SearchResult(it.title, "book/${it.existingBookId}") } +
            units.filter { LearningRules.search(it.title).contains(term) }.map { SearchResult(it.title, "topic/${it.id}") } +
            lessons.filter { LearningRules.search(it.title).contains(term) }.map { SearchResult(it.title, "lesson/${it.id}") }
    }
}
