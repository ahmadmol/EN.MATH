package com.ahmadmol.enmath.features.teacher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ahmadmol.enmath.core.model.*
import java.util.UUID

class AssignmentBuilderViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val step = saved.getStateFlow("step", 0)
    val title = saved.getStateFlow("title", "")
    val description = saved.getStateFlow("description", "")
    val classId = saved.getStateFlow("class", "")
    val topicId = saved.getStateFlow("topic", "derivatives")
    val selected = saved.getStateFlow("questions", arrayListOf<String>())
    val due = saved.getStateFlow("due", System.currentTimeMillis() + 172_800_000L)
    val points = saved.getStateFlow("points", "100")
    val attempts = saved.getStateFlow("attempts", "1")
    val hints = saved.getStateFlow("hints", true)
    val late = saved.getStateFlow("late", true)
    val error = saved.getStateFlow("error", false)

    fun field(key: String, value: String) {
        saved[key] = value
        saved["error"] = false
    }

    fun settings(key: String, value: Boolean) {
        saved[key] = value
    }

    fun due(value: Long) {
        saved["due"] = value
    }

    fun toggle(id: String) {
        saved["questions"] =
            ArrayList(if (id in selected.value) selected.value - id else selected.value + id)
    }

    fun next(): Boolean {
        val valid =
            when (step.value) {
                0 ->
                    title.value.trim().length >= 3 &&
                        description.value.isNotBlank() &&
                        classId.value.isNotBlank()
                1 -> selected.value.isNotEmpty()
                2 ->
                    (points.value.toIntOrNull() ?: 0) in 1..1000 &&
                        (attempts.value.toIntOrNull() ?: 0) in 1..5 &&
                        due.value > System.currentTimeMillis()
                else -> true
            }
        saved["error"] = !valid
        if (valid && step.value < 3) saved["step"] = step.value + 1
        return valid
    }

    fun back() {
        saved["step"] = (step.value - 1).coerceAtLeast(0)
        saved["error"] = false
    }

    fun assignment() =
        Assignment(
            UUID.randomUUID().toString(),
            Copy(title.value.trim(), title.value.trim()),
            Copy(description.value.trim(), description.value.trim()),
            classId.value,
            selected.value.toList(),
            due.value,
            points.value.toInt(),
            attempts.value.toInt(),
            hints.value,
            late.value,
        )
}
