package com.ahmadmol.enmath.core.data

/** Raw ranges are retained, including invalid source values. Never silently repair source data. */
data class SourceSpan(val printedFrom: Int, val printedTo: Int, val pdfFrom: Int, val pdfTo: Int) {
    val valid get() = printedFrom > 0 && printedTo >= printedFrom && pdfFrom > 0 && pdfTo >= pdfFrom
    fun containsPdf(page: Int) = valid && page in pdfFrom..pdfTo
}
enum class SourcePublication { BLOCKED_EDITION_REVIEW }
data class BacSourceBook(val id: String, val existingBookId: String, val volume: Int, val title: String,
    val edition: String, val sha256: String, val pdfPages: Int, val printedOffset: Int,
    val publication: SourcePublication = SourcePublication.BLOCKED_EDITION_REVIEW)
data class BacSourceLesson(val id: String, val unitId: String, val sourceId: String, val order: Int,
    val title: String, val edition: String, val span: SourceSpan, val notes: String, val issues: List<String>,
    val publication: SourcePublication = SourcePublication.BLOCKED_EDITION_REVIEW)
data class BacSourceSection(val id: String, val unitId: String, val sourceId: String, val kind: String,
    val title: String, val span: SourceSpan, val issues: List<String>)
data class BacSourceUnit(val id: String, val sourceId: String, val order: Int, val title: String,
    val span: SourceSpan, val lessonIds: List<String>, val sectionIds: List<String>, val issues: List<String>)
data class BacInventoryPage(val sourceId: String, val pdfPage: Int, val printedPage: Int?, val unitId: String?,
    val estimatedRole: String, val sectionIds: List<String>, val candidateIds: List<String>, val issues: List<String>)
/** Review metadata only. Draft text/answers are deliberately excluded from the student binary. */
data class BacDraftReview(val id: String, val unitId: String, val lessonId: String? = null)
data class LegacySourceRelation(val legacyLessonId: String, val relatedSourceLessonIds: List<String>)
