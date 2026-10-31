package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.*

/** Edition labels describe the actual bundled source, never an official curriculum claim. */
data class StudentScope(val ownerId: String, val stage: StudyStage, val edition: String, val generation: Long)
enum class ContentKind { LESSON, SOURCE_INDEX }
enum class ContentAvailability { NOT_PUBLISHED, BUNDLED_DEMO }
data class CurriculumSource(val id: String, val title: Copy, val official: Boolean, val referenceUrl: String? = null)
data class ContentRef(val stage: StudyStage, val edition: String, val lessonId: String,
    val revision: Int = 1, val sourceId: String = "enmath-demo", val kind: ContentKind = ContentKind.LESSON,
    val sourceSha256: String? = null)
/** Next content delivery supplies these existing domain models per stage/edition. */
data class CurriculumCatalog(val stage: StudyStage, val edition: String, val books: List<Book>,
    val sets: List<PracticeSet>, val questions: List<Question>, val availability: ContentAvailability,
    val revision: Int = 1, val source: CurriculumSource = CurriculumSource("enmath-demo",
        Copy("EN.MATH partial demonstration material", "محتوى EN.MATH تجريبي جزئي"), false),
    val sourceIndex: List<BacSourceBook> = emptyList(), val grade9SourceIndex: List<CatalogSource> = emptyList())
object StudentCatalog {
    fun catalog(stage: StudyStage) = when (stage) {
        StudyStage.G9 -> CurriculumCatalog(stage, G9_EDITION, emptyList(), emptyList(), emptyList(), ContentAvailability.NOT_PUBLISHED, source = CurriculumSource("g9-source-index", Copy("Grade 9 source index; content pending review", "فهرس مصدر التاسع؛ المحتوى قيد المراجعة"), false), grade9SourceIndex = G9SourceCatalog.sources)
        StudyStage.BAC -> CurriculumCatalog(stage, BAC_EDITION, DemoContent.books, ProductSeed.sets, ProductSeed.questions, ContentAvailability.BUNDLED_DEMO, sourceIndex = BacSourceCatalog.books)
    }
    const val BAC_EDITION = "bundled-bac-demo-v1"
    const val G9_EDITION = "g9-unpublished"
    fun scope(s: LearningState): StudentScope? = s.user?.localId?.let {
        StudentScope(it, requireNotNull(s.user.studyStage), catalog(requireNotNull(s.user.studyStage)).edition, s.generation)
    }
    fun accepts(s: LearningState, scope: StudentScope) = scope(s) == scope && s.storageIssue == null
    /** Planning/source lookup is distinct from permission to complete a lesson. */
    fun acceptsReference(s: LearningState, ref: ContentRef) =
        if (ref.kind == ContentKind.SOURCE_INDEX) when (ref.stage) {
            StudyStage.G9 -> G9SourceCatalog.accepts(s, ref)
            StudyStage.BAC -> BacSourceCatalog.accepts(s, ref)
        } else accepts(s, ref)
    fun sourceTitle(ref: ContentRef) = when (ref.stage) {
        StudyStage.G9 -> G9SourceCatalog.lesson(ref.lessonId)?.title
        StudyStage.BAC -> BacSourceCatalog.title(ref)
    }
    fun accepts(s: LearningState, ref: ContentRef) = s.user?.studyStage == ref.stage &&
        scope(s)?.edition == ref.edition && s.storageIssue == null &&
        catalog(ref.stage).revision == ref.revision && catalog(ref.stage).source.id == ref.sourceId &&
        ref.kind == ContentKind.LESSON && ref.sourceSha256 == null && s.lesson(ref.lessonId) != null
    fun ref(s: LearningState, id: String) = scope(s)?.let { ContentRef(it.stage, it.edition, id, catalog(it.stage).revision, catalog(it.stage).source.id) }
        ?.takeIf { accepts(s, it) }
    fun validDelta(s: LearningState, next: ProductState): Boolean {
        val lessonIds = s.lessons.map { it.id }.toSet()
        val sets = s.practiceSets.map { it.id }.toSet()
        return next.completionRefs == s.product.completionRefs && next.learningCommands == s.product.learningCommands && next.commandReceipts == s.product.commandReceipts &&
            s.product.attempts.all { it in next.attempts } &&
            next.pendingGemEvents.map { it.id }.distinct().size == next.pendingGemEvents.size && (next.savedLessons - s.product.savedLessons).all { it in lessonIds } &&
            (next.verifiedLessons.keys - s.product.verifiedLessons.keys).all { it in lessonIds } &&
            next.attempts.filterNot { it in s.product.attempts }.all { a ->
                val set = s.practiceSets.find { it.id == a.setId }
                set != null && (a.edition == scope(s)?.edition) &&
                    (a.stage == s.user?.studyStage) && a.commandId?.isNotBlank() == true && a.seconds >= 0 && a.total == set.questionIds.size &&
                    a.answers.keys.all { it in set.questionIds } && a.correct == set.questionIds.count { id ->
                        s.questions.find { it.id == id }?.let { LearningRules.correct(it, a.answers[id].orEmpty()) } == true
                    }
            } &&
            next.history.filterNot { it in s.product.history }.all { it.id.isNotBlank() && it.expression.isNotBlank() } &&
            next.pendingGemEvents.filterNot { it in s.product.pendingGemEvents }.all {
                it.owner == s.user?.localId && s.user?.type == AccountType.Student &&
                    when (it.kind) { "LESSON" -> it.contentId in lessonIds; "PRACTICE" -> it.contentId in sets
                        "ASSIGNMENT" -> s.product.assignments.any { a -> a.id == it.contentId }; else -> false }
            }
    }
    fun practiceForQuestion(s: LearningState, questionId: String): PracticeSet? =
        s.practiceSets.firstOrNull { questionId in it.questionIds && !it.quiz }
            ?: s.practiceSets.firstOrNull { questionId in it.questionIds }
    fun practiceForLesson(s: LearningState, lessonId: String): PracticeSet? =
        s.topicOf(lessonId)?.let { topic -> s.practiceSets.firstOrNull { it.topicId == topic.id && !it.quiz } }
    fun referenceKey(ref: ContentRef) = "${ref.stage.name}|${ref.edition}|${ref.revision}|${ref.sourceId}|${ref.lessonId}" +
        if (ref.kind == ContentKind.SOURCE_INDEX) "|SOURCE_INDEX|${ref.sourceSha256}" else ""
    fun completed(s: LearningState) = s.completed.filter { id ->
        val ref = ref(s, id)
        ref != null && (s.user?.localId == null || s.product.completionRefs[id] == referenceKey(ref))
    }.toSet()
    fun attempts(s: LearningState) = s.product.attempts.filter { a ->
        s.practiceSets.any { it.id == a.setId } && a.edition == scope(s)?.edition &&
            a.stage == s.user?.studyStage && a.commandId != null
    }
    fun hasUnverifiedHistory(s: LearningState) = s.reviewRequired ||
        s.completed.any { it !in completed(s) } || s.product.attempts.any { it !in attempts(s) }
}
