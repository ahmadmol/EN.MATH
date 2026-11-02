package com.ahmadmol.enmath.core.data

import android.content.Context
import android.content.pm.ApplicationInfo
import com.ahmadmol.enmath.core.model.StudyStage
import org.json.JSONObject
import java.math.BigInteger

enum class DraftInteraction { SINGLE_CHOICE, SHORT_INPUT, TRUE_FALSE, FILL_BLANK, ERROR_CORRECTION, WORKED_PROBLEM }
enum class DraftGrade { CORRECT, INCORRECT, NEEDS_TEACHER_REVIEW }
data class DraftLessonMapping(val draftId: String, val sourceId: String, val unitId: String, val bookId: String, val goal: String)
data class DraftExercise(val id: String, val sourceLessonId: String, val draftLessonId: String,
    val skill: String, val difficulty: Int, val interaction: DraftInteraction, val prompt: String,
    val answer: String, val choices: List<String>, val booleanAnswer: Boolean?, val explanation: String,
    val hint: String, val gradingMode: String, val contentVersion: Int, val reviewStatus: String,
    val sourceProvenance: String)

/** Content providers are stage-specific. No source index or QA bank grants Release publication. */
interface ExerciseContentSource<T> { val stage: StudyStage; fun forLesson(id: String): List<T> }
/** Preserve the existing BAC questions and graders. Source-index researches have no invented bank. */
class BacDemoExerciseSource(private val state: LearningState) : ExerciseContentSource<com.ahmadmol.enmath.core.model.Question> {
    override val stage = StudyStage.BAC
    fun forSet(id: String): List<com.ahmadmol.enmath.core.model.Question> {
        if (state.user?.studyStage != stage || !state.catalogAvailable) return emptyList()
        val set = state.practiceSets.find { it.id == id } ?: return emptyList()
        return set.questionIds.mapNotNull { key -> state.questions.find { it.id == key } }
    }
    override fun forLesson(id: String): List<com.ahmadmol.enmath.core.model.Question> {
        if (state.user?.studyStage != stage || state.lesson(id) == null) return emptyList()
        return StudentCatalog.practiceForLesson(state, id)?.let { forSet(it.id) }.orEmpty()
    }
}
class OriginalDraftBank private constructor(val mappings: List<DraftLessonMapping>, val questions: List<DraftExercise>) : ExerciseContentSource<DraftExercise> {
    override val stage = StudyStage.G9
    override fun forLesson(id: String) = questions.filter { it.sourceLessonId == id }.sortedWith(compareBy<DraftExercise> { it.difficulty }.thenBy { it.id })
    companion object {
        const val ASSET = "content/g9_original_draft_v1.json"
        /** Both boundaries matter: asset exists only in src/debug AND runtime must be debuggable. */
        fun loadQa(context: Context): OriginalDraftBank? {
            if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return null
            return runCatching { context.assets.open(ASSET).bufferedReader(Charsets.UTF_8).use { parse(it.readText()) } }.getOrNull()
        }
        internal fun parse(text: String): OriginalDraftBank {
            val doc = JSONObject(text)
            require(doc.getString("schemaVersion") == "1.0-draft")
            require(doc.getJSONObject("importProvenance").getString("publication") == "DEBUG_QA_ONLY")
            val rows = doc.getJSONArray("lessonMapping")
            val catalog = SourceCurricula.forStage(StudyStage.G9)
            val mappings = (0 until rows.length()).map { i -> rows.getJSONObject(i).let { row ->
                DraftLessonMapping(row.getString("draftLessonId"), row.getString("sourceLessonId"),
                    row.getString("sourceUnitId"), row.getString("sourceBookId"), row.getString("goal")).also { m ->
                    val research = requireNotNull(catalog.research(m.sourceId))
                    require(research.unitId == m.unitId && catalog.unit(m.unitId)?.bookId == m.bookId)
                    require(research.title == row.getString("sourceTitle"))
                }
            } }
            require(mappings.size == 32 && mappings.map { it.draftId }.distinct().size == 32 && mappings.map { it.sourceId }.toSet() == catalog.researches.map { it.id }.toSet())
            val questions = doc.getJSONArray("questions").let { a -> (0 until a.length()).map { i ->
                val q = a.getJSONObject(i); val m = mappings.single { it.draftId == q.getString("lessonId") }
                val choices = q.optJSONArray("choices")?.let { c -> (0 until c.length()).map(c::getString) }.orEmpty()
                require(q.getString("unitId") == m.draftId.substringBeforeLast("-L"))
                require(q.getString("stage") == "grade9" && q.getString("book") == if (m.bookId == "G9-ALG") "الجبر" else "الهندسة")
                DraftExercise(q.getString("id"), m.sourceId, m.draftId, q.getString("skillId"), q.getInt("difficulty"),
                    DraftInteraction.valueOf(q.getString("interaction").uppercase(java.util.Locale.ROOT)), q.getString("prompt"),
                    q.getString("answer"), choices, if (q.has("correctBoolean")) q.getBoolean("correctBoolean") else null,
                    q.getString("explanation"), q.getString("hint"), q.getString("gradingMode"), q.getInt("contentVersion"),
                    q.getString("reviewStatus"), q.toString()).also { exercise ->
                    require(exercise.id.startsWith(m.draftId + "-Q") && exercise.difficulty in 1..3 && exercise.contentVersion == 1)
                    require(exercise.reviewStatus == "draft_needs_teacher_review" && q.getBoolean("requiresTeacherReview"))
                    require(listOf(exercise.prompt, exercise.answer, exercise.explanation, exercise.hint, exercise.skill).all(String::isNotBlank))
                    if (exercise.interaction == DraftInteraction.SINGLE_CHOICE) require(choices[q.getInt("correctOptionIndex")] == exercise.answer)
                }
            } }
            require(questions.size == 192 && questions.map { it.id }.distinct().size == 192)
            require(questions.groupBy { it.sourceLessonId }.values.all { qs -> qs.size == 6 && qs.map { it.interaction }.toSet().size == 6 && qs.map { it.difficulty } == listOf(1, 1, 2, 2, 3, 3) })
            return OriginalDraftBank(mappings, questions)
        }
    }
}

/** Bounded deterministic marking only; free proofs and algebra never use string-equality grading. */
object OriginalDraftGrader {
    fun grade(q: DraftExercise, response: String): DraftGrade {
        if (response.isBlank() || response.length > 2048) return DraftGrade.INCORRECT
        return when (q.gradingMode) {
            "single_choice_exact" -> if (response in q.choices && response == q.answer) DraftGrade.CORRECT else DraftGrade.INCORRECT
            "boolean_exact" -> if (response == q.booleanAnswer.toString()) DraftGrade.CORRECT else DraftGrade.INCORRECT
            "normalize_numeric_or_teacher_review" -> {
                if (MathAnswer.number(q.answer) == null) return DraftGrade.NEEDS_TEACHER_REVIEW
                if ((q.prompt.contains("مختزل") || q.prompt.contains("اختزل")) && q.answer.contains('/')) {
                    val parts = MathInput.normalize(response).split('/')
                    if (parts.size != 2 || !parts.all { it.matches(Regex("-?[0-9]{1,30}")) }) return DraftGrade.INCORRECT
                    val a = BigInteger(parts[0]); val b = BigInteger(parts[1])
                    if (b <= BigInteger.ZERO || a.gcd(b) != BigInteger.ONE) return DraftGrade.INCORRECT
                }
                if (MathAnswer.matches(q.answer, response)) DraftGrade.CORRECT else DraftGrade.INCORRECT
            }
            else -> DraftGrade.NEEDS_TEACHER_REVIEW
        }
    }
}

/** Preserve paragraphs inside each worked step. A newline is never a field separator. */
object WorkedDraftResponse {
    private const val SEPARATOR = '\u001F'
    fun encode(parts: List<String>): String {
        require(parts.size == 3 && parts.all { it.length <= 600 })
        return parts.joinToString(SEPARATOR.toString()) { it.replace(SEPARATOR, ' ') }
    }
    fun decode(value: String): List<String> {
        val parts = if (SEPARATOR in value) value.split(SEPARATOR, limit = 3)
            else value.split('\n', limit = 3) // Preserve the first internal prototype's saved text.
        return parts + List((3 - parts.size).coerceAtLeast(0)) { "" }
    }
}

/** Pure geometry shared by drawing, layout and tests; physical mirroring happens exactly once. */
object JourneyLayout {
    fun bias(index: Int): Float = listOf(0f, -1f, 0f, 1f)[Math.floorMod(index, 4)]
    fun center(width: Float, nodeWidth: Float, index: Int, rtl: Boolean): Float = width / 2f +
        bias(index) * (if (rtl) -1 else 1) * ((width - nodeWidth).coerceAtLeast(0f) / 2f) * .9f
    fun unitIndex(catalog: SourceCurriculum, id: String): Int? {
        var index = 4 // status, greeting, progress, selector
        catalog.units.forEach { unit ->
            if (unit.id == id) return index
            index += 1 + unit.researches.size
        }
        return null
    }
}
