package com.ahmadmol.enmath.features.courses.interactive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/** Only transient presentation state. Completion/grading remain repository/AppViewModel actions. */
class InteractiveLessonViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val step = saved.getStateFlow("lesson-step", 0)
    val answers = saved.getStateFlow("lesson-answers", hashMapOf<String, String>())
    val checked = saved.getStateFlow("lesson-checked", false)

    fun answer(id: String, value: String) {
        saved["lesson-answers"] = HashMap(answers.value).apply { put(id, value) }
        saved["lesson-checked"] = false
    }

    fun check() {
        saved["lesson-checked"] = true
    }

    fun move(index: Int) {
        saved["lesson-step"] = index
        saved["lesson-checked"] = false
    }
}
