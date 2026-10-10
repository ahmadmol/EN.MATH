package com.ahmadmol.enmath.features.solver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun MathKeyboard(group: Int, onGroup: (Int) -> Unit, onKey: (String) -> Unit) {
    ChipRow(
        listOf(
            label(TextKey.t_02c7e9b066),
            label(TextKey.t_88130f2bb9),
            label(TextKey.t_0101d5fb65),
            label(TextKey.t_9d90e24064),
            label(TextKey.t_62cfa1d22a),
            label(TextKey.t_6ac26cc029),
        ),
        group,
        onGroup,
    )
    val extras =
        when (group) {
            1 -> listOf("+", "−", "×", "÷", "a/b", "abs(")
            2 -> listOf("x", "y", "=", "^", "√(", "root(")
            3 -> listOf("lim x→", "d/dx(", "∫", "dx", "∑", "∞")
            4 -> listOf("sin(", "cos(", "tan(", "log(", "ln(", "π")
            5 -> listOf("[[", ",", "]]", "det ", ";", "I")
            else -> listOf("(", ")", "x", "y", "π", "e")
        }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        (extras.chunked(3) +
                listOf(
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "−"),
                    listOf("0", ".", "=", "+"),
                ))
            .forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    row.forEach { key ->
                        FilledTonalButton(
                            { onKey(key) },
                            Modifier.weight(1f).heightIn(min = Space.touch).testTag("key-$key"),
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(Space.xs),
                        ) {
                            Text(key)
                        }
                    }
                }
            }
    }
}

@Composable
fun SolverWorkspace(
    vm: SolverViewModel,
    state: LearningState,
    onSolve: (String, String) -> Unit,
    onRoute: (String) -> Unit,
) {
    val expression by vm.expression.collectAsStateWithLifecycle()
    var field by remember {
        mutableStateOf(TextFieldValue(expression, TextRange(expression.length)))
    }
    LaunchedEffect(expression) {
        if (expression != field.text)
            field = TextFieldValue(expression, TextRange(expression.length))
    }
    var group by rememberSaveable { mutableIntStateOf(0) }
    var mode by rememberSaveable { mutableIntStateOf(1) }
    fun insert(raw: String) {
        val key = if (raw == "a/b") "/" else raw
        val start = field.selection.min
        val end = field.selection.max
        val text = field.text.replaceRange(start, end, key)
        field = TextFieldValue(text, TextRange(start + key.length))
        vm.edit(text)
    }
    ScreenPage(Modifier.testTag("screen-solver")) {
        PageHeading(label(TextKey.t_df6c0f6e87), label(TextKey.t_b8077ff8ea))
        DemoNotice(label(TextKey.t_14b88455cb))
        AdaptivePair(
            first = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        field,
                        {
                            field = it
                            vm.edit(it.text)
                        },
                        label = { Text(label(TextKey.t_189ecce295)) },
                        modifier = Modifier.fillMaxWidth().testTag("solver-input"),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
                if (expression.isNotBlank()) EquationCard(expression)
                ChipRow(
                    listOf(
                        label(TextKey.t_c55ce786b5),
                        label(TextKey.t_7e3305f812),
                        label(TextKey.t_685c4edda1),
                    ),
                    mode,
                    {
                        mode = it
                        if (it == 2) onRoute("solver/scan")
                    },
                )
                if (mode == 1) MathKeyboard(group, { group = it }, ::insert)
                Row {
                    TextButton({ vm.edit("") }, Modifier.testTag("solver-clear")) {
                        Text(label(TextKey.t_aeaec2c65d))
                    }
                    TextButton({
                        if (field.selection.collapsed && field.selection.start > 0) {
                            val p = field.selection.start
                            val text = field.text.removeRange(p - 1, p)
                            field = TextFieldValue(text, TextRange(p - 1))
                            vm.edit(text)
                        } else if (!field.selection.collapsed) insert("")
                    }) {
                        Text(label(TextKey.t_3ffdefc981))
                    }
                    TextButton(vm::undo) { Text(label(TextKey.t_be2a7c624f)) }
                }
                PrimaryButton(
                    label(TextKey.t_1b9e6ceaba),
                    { if (vm.solve()) onSolve(expression, vm.solution.topicId) },
                    Modifier.testTag("solve"),
                    expression.isNotBlank(),
                )
            },
            second = {
                SectionTitle(label(TextKey.t_cee84ba17a))
                state.examples.forEachIndexed { i, e ->
                    AppCard(
                        onClick = { vm.edit(e.expression) },
                        modifier = Modifier.testTag("example-$i"),
                    ) {
                        Text(e.category.text())
                        MathText(e.expression)
                    }
                }
                SecondaryButton(label(TextKey.t_ae1c6ca083), { onRoute("solver/history") })
                SecondaryButton(label(TextKey.t_a7e385f50a), { onRoute("explore") })
                state.product.history.take(3).forEach { h ->
                    AppCard(onClick = { onRoute("solver/solution/${h.id}") }) {
                        MathText(h.expression)
                        SecondaryButton(
                            label(TextKey.t_0bbe6c9c93),
                            { onRoute("solver/solution/${h.id}") },
                        )
                    }
                }
            },
        )
    }
}

@Composable
fun ProductSolutionScreen(
    solution: Solution,
    state: LearningState,
    saved: Boolean,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onLesson: (String) -> Unit,
    onExample: (String) -> Unit,
) {
    var method by rememberSaveable(solution.expression) { mutableIntStateOf(0) }
    var hint by rememberSaveable(solution.expression) { mutableIntStateOf(0) }
    var share by remember { mutableStateOf(false) }
    ScreenPage(Modifier.testTag("screen-solution")) {
        PageHeading(label(TextKey.t_7a0c16fd84), label(TextKey.t_1dd6ad45d7))
        DemoNotice(label(TextKey.t_1df282cbf8))
        EquationCard(solution.expression)
        if (solution.answer == null) {
            EmptyState(label(TextKey.t_93efc76003), label(TextKey.t_2f5557f305))
            state.examples.forEach { e ->
                AppCard(onClick = { onExample(e.expression) }) {
                    MathText(e.expression)
                    SecondaryButton(label(TextKey.t_1931081814), { onExample(e.expression) })
                }
            }
        } else {
            AppCard(tonal = true) {
                Text(label(TextKey.t_b067c69b60))
                MathText(solution.answer, MaterialTheme.typography.headlineLarge)
            }
            if (solution.methods.isNotEmpty())
                ChipRow(solution.methods.map { it.name.text() }, method, { method = it })
            val steps = solution.methods.getOrNull(method)?.steps ?: solution.steps
            steps.forEachIndexed { i, step ->
                var expand by
                    rememberSaveable(solution.expression, method, i) { mutableStateOf(true) }
                AppCard(onClick = { expand = !expand }) {
                    Text(label(TextKey.t_377ff08181, i + 1))
                    Text(step.title.text(), style = MaterialTheme.typography.titleMedium)
                    MathText(step.expression)
                    if (expand) {
                        Text(step.explanation.text())
                        Text(step.reason.text(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            TextButton({ hint = (hint + 1).coerceAtMost(steps.size) }) {
                Text(label(TextKey.t_98340e1f7e))
            }
            steps.take(hint).forEach { DemoNotice(it.reason.text()) }
            BookmarkButton(saved, onSave)
            SecondaryButton(label(TextKey.t_9d8c72545a), { share = true })
            val topic = state.topic(solution.topicId) ?: state.topic("derivatives")!!
            SecondaryButton(label(TextKey.t_6a87ec3397), { onLesson(topic.lessons.first().id) })
            SectionTitle(label(TextKey.t_ecd7a0348c))
            state.questions
                .filter { it.topicId == topic.id }
                .take(2)
                .forEach { q ->
                    AppCard(onClick = { onExample(q.expression) }) {
                        MathText(q.expression)
                        SecondaryButton(label(TextKey.t_6841cd8fe8), { onExample(q.expression) })
                    }
                }
        }
        PrimaryButton(label(TextKey.t_f762e78c7a), onEdit, Modifier.testTag("edit-problem"))
    }
    if (share)
        AlertDialog(
            onDismissRequest = { share = false },
            title = { Text(label(TextKey.t_606419751d)) },
            text = { Text(label(TextKey.t_b097319490)) },
            confirmButton = { TextButton({ share = false }) { Text(label(TextKey.t_115c64b035)) } },
        )
}

@Composable
fun SolverScanScreen(onRecognized: (String) -> Unit) {
    var recognized by rememberSaveable { mutableStateOf(false) }
    ScreenPage {
        PageHeading(label(TextKey.t_dc79b60266), label(TextKey.t_628fb25033))
        DemoNotice(label(TextKey.t_3be2255f32))
        AppCard(tonal = true) {
            Box(Modifier.fillMaxWidth().height(Space.chart + Space.hero)) {
                MathText("┌   2x + 4 = 10   ┐")
            }
        }
        SecondaryButton(label(TextKey.t_0f6bdbd212), { recognized = true })
        SecondaryButton(label(TextKey.t_0a7ea713f4), { recognized = true })
        if (recognized) {
            EquationCard("2x + 4 = 10")
            PrimaryButton(label(TextKey.t_bb0767c28e), { onRecognized("2x + 4 = 10") })
        }
    }
}

@Composable
fun SolverHistoryScreen(
    state: LearningState,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClear: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val topics = listOf("all", "limits", "derivatives", "integrals", "matrices", "probability")
    ScreenPage {
        PageHeading(label(TextKey.t_bd4d7a258d), label(TextKey.t_1361fe9c87))
        SearchField(query, { query = it })
        ChipRow(
            topics.map {
                if (it == "all") label(TextKey.t_40d8dc7f19) else state.topic(it)!!.title.text()
            },
            filter,
            { filter = it },
        )
        SecondaryButton(label(TextKey.t_05cfbeaf8f), onClear)
        val filtered =
            state.product.history.filter {
                it.expression.contains(query, true) && (filter == 0 || it.topicId == topics[filter])
            }
        if (filtered.isEmpty()) EmptyState(label(TextKey.t_c75fccb635), label(TextKey.t_57276af9a8))
        filtered
            .groupBy { ((System.currentTimeMillis() - it.timestamp) / 86_400_000).coerceAtLeast(0) }
            .forEach { (day, items) ->
                SectionTitle(
                    if (day == 0L) label(TextKey.t_e54621f53e)
                    else if (day == 1L) label(TextKey.t_61ec47c295) else label(TextKey.t_de69ae3b7a)
                )
                items.forEach { h ->
                    AppCard(onClick = { onOpen(h.id) }) {
                        MathText(h.expression)
                        SecondaryButton(label(TextKey.t_0bbe6c9c93), { onOpen(h.id) })
                        TextButton({ onDelete(h.id) }) { Text(label(TextKey.t_3ffdefc981)) }
                    }
                }
            }
    }
}
