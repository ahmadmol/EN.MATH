package com.ahmadmol.enmath.core.data

import android.content.Context
import com.ahmadmol.enmath.core.model.StudyStage
import org.json.JSONObject

/** A separate, 6-question pilot asset. The original 192 draft questions are not published. */
data class NaturePracticeQuestion(
    val id: String,
    val lessonId: String,
    val prompt: String,
    val answer: String,
    val explanation: String,
    val hint: String,
    val difficulty: Int,
    val interaction: String,
    val choices: List<String>,
    val reviewStatus: String,
    val guidedParts: List<String> = emptyList(),
    val classOptions: List<String> = emptyList(),
    val classificationTokens: List<String> = emptyList(),
    val groups: List<Pair<String, String>> = emptyList()
)

object NaturePracticeBank {
    private const val PATH = "content/g9/nature_numbers_pilot.json"
    private val TYPES = listOf("single_choice", "short_input", "true_false", "fill_blank", "error_correction", "worked_problem")
    const val PILOT_DISPLAY_TITLE = "طبيعة الأعداد"
    /** Runtime catalog is G9-ALG-...; uploaded bank uses G9-A-... . Match explicitly by position and name. */
    fun matches(research: SourceResearch): Boolean {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val first = catalog.book("G9-ALG")?.units?.firstOrNull()?.researches?.firstOrNull()
        return first?.id == research.id && first.title.trim().contains(PILOT_DISPLAY_TITLE) &&
            first.ref.stage == StudyStage.G9
    }

    fun load(context: Context): List<NaturePracticeQuestion> {
        val text = context.assets.open(PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val doc = JSONObject(text)
        require(doc.getInt("schemaVersion") == 1 && doc.getString("sourceLessonId") == NaturePracticeGrader.SOURCE_LESSON)
        require(doc.getString("contentStatus") == "DRAFT_NEEDS_TEACHER_REVIEW")
        val specs = doc.getJSONObject("derivedGradingSpecs")
        val rows = doc.getJSONArray("questions")
        require(rows.length() == 6)
        val items = (0 until rows.length()).map { i ->
            val q = rows.getJSONObject(i)
            val choices = q.optJSONArray("choices")?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty()
            val spec = specs.optJSONObject(q.getString("id"))
            fun words(key: String) = spec?.optJSONArray(key)?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty()
            val groups = spec?.optJSONArray("groups")?.let { a -> (0 until a.length()).map { a.getJSONObject(it).let { it.getString("key") to it.getString("label") } } }.orEmpty()
            NaturePracticeQuestion(q.getString("id"), q.getString("lessonId"), q.getString("prompt"),
                q.getString("answer"), q.getString("explanation"), q.getString("hint"),
                q.getInt("difficulty"), q.getString("interaction"), choices, q.getString("reviewStatus"),
                words("parts"), words("classOptions"), words("tokens"), groups)
        }
        require(items.map { it.id }.distinct().size == 6)
        require(items.map { it.id } == (1..6).map { "${NaturePracticeGrader.SOURCE_LESSON}-Q${it.toString().padStart(2, '0')}" })
        require(items.map { it.interaction } == TYPES)
        require(items.map { it.difficulty } == listOf(1, 1, 2, 2, 3, 3))
        require(items.all { it.lessonId == NaturePracticeGrader.SOURCE_LESSON && it.reviewStatus == "draft_needs_teacher_review" &&
            it.prompt.isNotBlank() && it.answer.isNotBlank() && it.explanation.isNotBlank() })
        require(items.first().choices.size == 4 && items.first().choices[2] == items.first().answer && items.first().answer == "√3")
        require(items[1].answer == "3/8" && items[2].answer == "صحيح" && items[3].answer == "غير منتهية ودورية")
        require(items[4].answer.contains("√25 = 5") && items[5].answer.contains("2/5"))
        val expected = doc.getJSONObject("derivedGradingSpecs")
        require(expected.length() == 4)
        require(expected.getJSONObject("${NaturePracticeGrader.SOURCE_LESSON}-Q02").getString("mode") == "reduced_fraction")
        require(expected.getJSONObject("${NaturePracticeGrader.SOURCE_LESSON}-Q04").getString("mode") == "select_all_parts")
        require(expected.getJSONObject("${NaturePracticeGrader.SOURCE_LESSON}-Q05").getString("mode") == "root_value_and_class")
        require(expected.getJSONObject("${NaturePracticeGrader.SOURCE_LESSON}-Q06").getString("mode") == "smallest_number_group")
        return items
    }
}
