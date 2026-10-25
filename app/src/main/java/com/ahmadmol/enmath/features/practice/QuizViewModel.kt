package com.ahmadmol.enmath.features.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.model.Question

class QuizViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val commandId: String = saved.get<String>("learning-command-id")
        ?: java.util.UUID.randomUUID().toString().also { saved["learning-command-id"] = it }
    val index = saved.getStateFlow("index", 0)
    val answers = saved.getStateFlow("answers", hashMapOf<String, String>())
    val checked = saved.getStateFlow("checked", false)
    val hint = saved.getStateFlow("hint", false)
    private val start: Long =
        saved.get<Long>("start") ?: System.currentTimeMillis().also { saved["start"] = it }

    /** Bind new answers to their actual local account; old restored answers have no reward provenance. */
    fun gemsOwner(current: String?): String? {
        if (!saved.contains("local-gems-owner"))
            saved["local-gems-owner"] = if (answers.value.isEmpty()) current.orEmpty() else ""
        return saved.get<String>("local-gems-owner")?.takeIf { it.isNotBlank() }
    }

    fun answer(id: String, value: String) {
        if (checked.value || saved.get<Boolean>("finished") == true) return
        saved["answers"] = HashMap(answers.value).apply { put(id, value) }
        saved["checked"] = false
    }

    fun check() {
        if (checked.value || answers.value.values.none { it.isNotBlank() }) return
        saved["checked"] = true
    }

    fun check(q: Question) {
        if (answers.value[q.id].isNullOrBlank()) return
        check()
    }

    fun hint() {
        saved["hint"] = !hint.value
    }

    fun retry() {
        saved["checked"] = false
    }

    fun next() {
        saved["index"] = index.value + 1
        saved["checked"] = false
        saved["hint"] = false
    }

    /** Reject a second tap from the previous composition before it can skip a question. */
    fun next(expectedIndex: Int, requireChecked: Boolean = true) {
        if (index.value != expectedIndex || (requireChecked && !checked.value)) return
        next()
    }

    fun claimFinish(questionIds: List<String>, requireChecked: Boolean = true): Boolean {
        if (saved.get<Boolean>("finished") == true) return false
        if (questionIds.isEmpty() || index.value != questionIds.lastIndex || (requireChecked && !checked.value)) return false
        if (questionIds.any { answers.value[it].isNullOrBlank() }) return false
        saved["finished"] = true
        return true
    }

    fun correct(q: Question) = LearningRules.correct(q, answers.value[q.id].orEmpty())

    fun seconds() = ((System.currentTimeMillis() - start) / 1000).toInt()
}
