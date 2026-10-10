package com.ahmadmol.enmath.features.solver

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SolverViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val engine: SolverEngine = DemoSolverEngine()
    val expression = saved.getStateFlow("expression", "")
    val selectedCategory = saved.getStateFlow("category", "all")
    val recent = saved.getStateFlow("recent", arrayListOf<String>())
    val submitted = saved.getStateFlow("submitted", "")
    val solution
        get() = engine.solve(submitted.value)

    private val undoStack = ArrayDeque<String>()

    fun edit(value: String) {
        if (value != expression.value) {
            undoStack.addLast(expression.value)
            if (undoStack.size > 50) undoStack.removeFirst()
        }
        saved["expression"] = value.take(500)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) saved["expression"] = undoStack.removeLast()
    }

    fun key(value: String) =
        edit(
            when (value) {
                "⌫" -> expression.value.dropLast(1)
                "Clear" -> ""
                "sin",
                "cos",
                "tan",
                "log",
                "ln",
                "√" -> expression.value + "$value("
                "a/b" -> expression.value + "/"
                "xʸ" -> expression.value + "^"
                else -> expression.value + value
            }
        )

    fun category(value: String) {
        saved["category"] = value
    }

    fun solve(): Boolean {
        val input = expression.value.trim()
        if (input.isBlank()) return false
        saved["submitted"] = input
        saved["recent"] = ArrayList((listOf(input) + recent.value).distinct().take(5))
        return true
    }

    fun reset() {
        saved["expression"] = ""
        saved["submitted"] = ""
        saved["category"] = "all"
        saved["recent"] = arrayListOf<String>()
    }
}
