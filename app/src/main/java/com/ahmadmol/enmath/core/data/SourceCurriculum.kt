package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.StudyStage

/** Read-only presentation of the existing source catalogs. No lesson/reward authority. */
data class SourceResearch(val ref: ContentRef, val unitId: String, val order: Int, val title: String,
    val pages: SourceSpan, val summary: String, val needsBoundaryReview: Boolean = false) {
    val id get() = ref.lessonId
    val route get() = "lesson/$id"
}
data class SourceUnit(val id: String, val bookId: String, val order: Int, val title: String,
    val pages: SourceSpan, val researches: List<SourceResearch>) {
    val route get() = "topic/$id"
}
data class SourceBook(val id: String, val title: String, val edition: String, val pages: Int,
    val units: List<SourceUnit>) {
    val route get() = "book/$id"
    val researches get() = units.flatMap { it.researches }
}
data class SourceCurriculum(val stage: StudyStage, val books: List<SourceBook>, val reviewed: Int) {
    val units get() = books.flatMap { it.units }
    val researches get() = units.flatMap { it.researches }
    fun book(id: String) = books.find { it.id == id }
    fun unit(id: String) = units.find { it.id == id }
    fun research(id: String) = researches.find { it.id == id }
    fun search(query: String): List<Pair<String, String>> {
        val words = LearningRules.search(query).split(Regex("\\s+")).filter(String::isNotBlank)
        if (words.isEmpty()) return emptyList()
        fun matches(text: String): Boolean {
            val normalized = LearningRules.search(text)
            return words.all { normalized.contains(it) || (it.length > 3 && it.startsWith("و") && normalized.contains(it.drop(1))) }
        }
        return books.filter { matches(it.title) }.map { it.title to it.route } +
            units.filter { matches(it.title) }.map { it.title to it.route } +
            researches.filter { matches("${it.title} ${it.summary}") }.map { it.title to it.route }
    }
}

object SourceCurricula {
    private val grade9 by lazy {
        SourceCurriculum(StudyStage.G9, G9SourceCatalog.sources.map { source ->
            SourceBook(source.ref.sourceBookId, source.title, source.ref.edition, source.pages,
                G9SourceCatalog.units.filter { it.ref.sourceBookId == source.ref.sourceBookId }.map { unit ->
                    SourceUnit(requireNotNull(unit.ref.unitId), source.ref.sourceBookId, unit.order, unit.title,
                        SourceSpan(unit.pages.from, unit.pages.to, unit.pages.from, unit.pages.to), unit.lessons.map { lesson ->
                            SourceResearch(requireNotNull(G9SourceCatalog.ref(requireNotNull(lesson.ref.lessonId))),
                                requireNotNull(unit.ref.unitId), lesson.order, lesson.title,
                                SourceSpan(lesson.pages.from, lesson.pages.to, lesson.pages.from, lesson.pages.to), lesson.summary)
                        })
                })
        }, G9SourceCatalog.readyLessonCount)
    }
    private val bac by lazy {
        SourceCurriculum(StudyStage.BAC, BacSourceCatalog.books.map { book ->
            SourceBook(book.id, book.title, book.edition, book.pdfPages,
                BacSourceCatalog.units.filter { it.sourceId == book.id }.map { unit ->
                    SourceUnit(unit.id, book.id, unit.order, unit.title, unit.span,
                        unit.lessonIds.map { id ->
                            val lesson = requireNotNull(BacSourceCatalog.lesson(id))
                            SourceResearch(requireNotNull(BacSourceCatalog.ref(id)), unit.id, lesson.order,
                                lesson.title, lesson.span, lesson.notes, lesson.issues.isNotEmpty() || unit.issues.isNotEmpty())
                        })
                })
        }, BacSourceCatalog.reviewedLessonCount)
    }
    fun forStage(stage: StudyStage) = if (stage == StudyStage.G9) grade9 else bac
    fun forState(state: LearningState) = state.user?.let { user -> user.studyStage ?: StudyStage.BAC.takeIf {
        user.localId == null && user.type != com.ahmadmol.enmath.core.model.AccountType.Teacher
    } }?.takeIf { state.catalogAvailable }?.let(::forStage)
}

/** Select an actual attempt when opened from history; legacy route still means latest for that set. */
fun savedPracticeAttempt(state: LearningState, setId: String, commandId: String? = null) =
    StudentCatalog.attempts(state).lastOrNull { it.setId == setId && (commandId == null || it.commandId == commandId) }
