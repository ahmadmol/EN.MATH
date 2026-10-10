package com.ahmadmol.enmath.features.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.model.Question

class QuizViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val index = saved.getStateFlow("index", 0)
    val answers = saved.getStateFlow("answers", hashMapOf<String, String>())
    val checked = saved.getStateFlow("checked", false)
    val hint = saved.getStateFlow("hint", false)
    private val start: Long =
        saved.get<Long>("start") ?: System.currentTimeMillis().also { saved["start"] = it }

    fun answer(id: String, value: String) {
        saved["answers"] = HashMap(answers.value).apply { put(id, value) }
        saved["checked"] = false
    }

    fun check() {
        saved["checked"] = true
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

    fun correct(q: Question) = LearningRules.correct(q, answers.value[q.id].orEmpty())

    fun seconds() = ((System.currentTimeMillis() - start) / 1000).toInt()
}
