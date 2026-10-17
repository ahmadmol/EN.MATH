package com.ahmadmol.enmath.features.solver

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.features.teacher.TeacherReferenceRoutes

/** Uses the same existing solver and history, with teacher-safe callbacks. */
@Composable
fun TeacherSolverWorkspace(app: AppViewModel, state: LearningState, open: (String) -> Unit) {
    val vm: SolverViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    var historyId by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf(false) }
    var unavailable by remember { mutableStateOf(false) }
    val submitted by vm.submitted.collectAsStateWithLifecycle()
    androidx.activity.compose.BackHandler(history || historyId.isNotEmpty()) {
        if (historyId.isNotEmpty()) historyId = "" else history = false
    }
    if (history && historyId.isEmpty()) {
        SolverHistoryScreen(
            state,
            { id ->
                state.product.history
                    .find { it.id == id }
                    ?.let {
                        vm.edit(it.expression)
                        vm.solve()
                        historyId = id
                    }
            },
            { id -> app.product { it.copy(history = it.history.filterNot { h -> h.id == id }) } },
            { app.product { it.copy(history = emptyList()) } },
            { history = false },
        )
    } else if (historyId.isEmpty()) {
        SolverWorkspace(
            vm,
            state,
            { expression, topic -> historyId = app.history(expression, topic) },
            { path ->
                when (path) {
                    "explore" -> open(TeacherReferenceRoutes.Lab)
                    "solver/scan" -> unavailable = true
                    "solver/history" -> history = true
                    else ->
                        state.product.history
                            .find { it.id == path.substringAfterLast('/') }
                            ?.let {
                                vm.edit(it.expression)
                                vm.solve()
                                historyId = it.id
                            }
                }
            },
        )
    } else {
        ProductSolutionScreen(
            DemoSolverEngine().solve(submitted),
            state,
            historyId in state.product.savedProblems,
            { app.bookmark(historyId, true) },
            {
                historyId = ""
                history = false
            },
            { open("teacher/reference/lesson/$it") },
            {
                vm.edit(it)
                historyId = ""
            },
        )
    }
    if (unavailable)
        AlertDialog(
            onDismissRequest = { unavailable = false },
            title = { Text(tr("Camera input", "إدخال بالكاميرا")) },
            text = {
                Text(
                    tr(
                        "Camera OCR is unavailable in this local version. Enter the expression manually.",
                        "التعرف على المعادلات بالكاميرا غير متاح في النسخة المحلية. أدخل المعادلة يدوياً.",
                    )
                )
            },
            confirmButton = { TextButton({ unavailable = false }) { Text(tr("OK", "حسناً")) } },
        )
}
