package com.ahmadmol.enmath.features.courses.interactive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.model.Question

/** Only transient presentation state. Completion/grading remain repository/AppViewModel actions. */
class InteractiveLessonViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val step = saved.getStateFlow("lesson-step", 0)
    val answers = saved.getStateFlow("lesson-answers", hashMapOf<String, String>())
    val checked = saved.getStateFlow("lesson-checked", false)
    val explorationX = saved.getStateFlow("lesson-exploration-x", 2.7f)

    fun exploreX(value: Float) {
        if (value.isFinite()) saved["lesson-exploration-x"] = LimitExploration.sliderValue(value)
    }

    /** Bind new answers to their actual local account; old restored answers have no reward provenance. */
    fun gemsOwner(current: String?): String? {
        if (!saved.contains("local-gems-owner"))
            saved["local-gems-owner"] = if (answers.value.isEmpty()) current.orEmpty() else ""
        return saved.get<String>("local-gems-owner")?.takeIf { it.isNotBlank() }
    }

    fun answer(id: String, value: String) {
        if (id in verified()) return
        saved["lesson-answers"] = HashMap(answers.value).apply { put(id, value) }
        saved["lesson-checked"] = false
        saved["lesson-reviewed"] = ArrayList(saved.get<ArrayList<String>>("lesson-reviewed").orEmpty() - id)
    }

    private fun verified() = saved.get<ArrayList<String>>("lesson-verified").orEmpty()

    /** Older sessions kept only the current checked flag; earlier steps were gated on success. */
    fun restoreLegacyReview(blocks: List<LessonBlock>) {
        if (saved.contains("lesson-review-version")) return
        saved["lesson-review-version"] = 1
        val passed = blocks.take(step.value.coerceAtLeast(0)).mapNotNull { it.question() }
            .filter { correct(it) }.map { it.id }
        val current = blocks.getOrNull(step.value)?.question()
        val reviewed = passed + listOfNotNull(current?.takeIf { checked.value }?.id)
        saved["lesson-reviewed"] = ArrayList(saved.get<ArrayList<String>>("lesson-reviewed").orEmpty().toSet() + reviewed)
        saved["lesson-verified"] = ArrayList(verified().toSet() + passed +
            listOfNotNull(current?.takeIf { checked.value && correct(it) }?.id))
    }

    fun correct(q: Question) = LearningRules.correct(q, answers.value[q.id].orEmpty())

    fun check(q: Question) {
        if (checked.value || answers.value[q.id].isNullOrBlank()) return
        saved["lesson-checked"] = true
        saved["lesson-reviewed"] = ArrayList(saved.get<ArrayList<String>>("lesson-reviewed").orEmpty().toSet() + q.id)
        if (correct(q)) saved["lesson-verified"] = ArrayList(verified().toSet() + q.id)
    }

    fun move(index: Int, blocks: List<LessonBlock>) {
        val current = step.value
        if (index !in blocks.indices || kotlin.math.abs(index - current) != 1) return
        val q = blocks.getOrNull(current)?.question()
        if (index > current && q != null && !(checked.value && correct(q))) return
        saved["lesson-step"] = index
        saved["lesson-checked"] = blocks[index].question()?.id in saved.get<ArrayList<String>>("lesson-reviewed").orEmpty()
    }

    fun releaseCompletion() { saved["lesson-completion-sent"] = false }

    fun claimCompletion(blocks: List<LessonBlock>): Boolean {
        if (saved.get<Boolean>("lesson-completion-sent") == true || blocks.getOrNull(step.value) !is LessonBlock.Completion) return false
        if (blocks.mapNotNull { it.question() }.any { !correct(it) || it.id !in verified() }) return false
        saved["lesson-completion-sent"] = true
        return true
    }
}
