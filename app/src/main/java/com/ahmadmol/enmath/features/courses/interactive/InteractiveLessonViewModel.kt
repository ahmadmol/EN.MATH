package com.ahmadmol.enmath.features.courses.interactive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/** Only transient presentation state. Completion/grading remain repository/AppViewModel actions. */
class InteractiveLessonViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val step = saved.getStateFlow("lesson-step", 0)
    val answers = saved.getStateFlow("lesson-answers", hashMapOf<String, String>())
    val checked = saved.getStateFlow("lesson-checked", false)

    /** Bind new answers to their actual local account; old restored answers have no reward provenance. */
    fun gemsOwner(current: String?): String? {
        if (!saved.contains("local-gems-owner"))
            saved["local-gems-owner"] = if (answers.value.isEmpty()) current.orEmpty() else ""
        return saved.get<String>("local-gems-owner")?.takeIf { it.isNotBlank() }
    }

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
