package com.ahmadmol.enmath.features.teacher

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.practice.QuizQuestion
import java.util.Calendar
import java.util.UUID

@Composable
fun AssignmentBuilderScreen(
    state: LearningState,
    vm: AssignmentBuilderViewModel,
    onQuestion: (Question) -> Unit,
    onPublish: (Assignment) -> Unit,
) {
    val step by vm.step.collectAsStateWithLifecycle()
    val title by vm.title.collectAsStateWithLifecycle()
    val desc by vm.description.collectAsStateWithLifecycle()
    val cls by vm.classId.collectAsStateWithLifecycle()
    val topic by vm.topicId.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val due by vm.due.collectAsStateWithLifecycle()
    val points by vm.points.collectAsStateWithLifecycle()
    val attempts by vm.attempts.collectAsStateWithLifecycle()
    val hints by vm.hints.collectAsStateWithLifecycle()
    val late by vm.late.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var preview by remember { mutableStateOf(false) }
    var custom by remember { mutableStateOf(false) }
    var prompt by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (cls.isBlank()) state.product.classes.firstOrNull()?.let { vm.field("class", it.id) }
    }
    ScreenPage(Modifier.testTag("screen-builder")) {
        PageHeading(label(TextKey.t_26d50e153b), label(TextKey.t_8f4e83b5d6, step + 1))
        LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth())
        when (step) {
            0 -> {
                AppTextField(
                    title,
                    { vm.field("title", it) },
                    label(TextKey.t_111b3c1828),
                    Modifier.testTag("builder-title"),
                )
                AppTextField(
                    desc,
                    { vm.field("description", it) },
                    label(TextKey.t_2ba4b141ce),
                    Modifier.testTag("builder-description"),
                    singleLine = false,
                )
                state.product.classes.forEach { c ->
                    Row {
                        RadioButton(cls == c.id, { vm.field("class", c.id) })
                        TextButton({ vm.field("class", c.id) }) { Text(c.name.text()) }
                    }
                }
            }
            1 -> {
                val topics = state.books.flatMap { it.topics }
                ChipRow(
                    topics.map { it.title.text() },
                    topics.indexOfFirst { it.id == topic },
                    { vm.field("topic", topics[it].id) },
                )
                state.topic(topic)?.lessons?.forEach {
                    Text(it.title.text(), style = MaterialTheme.typography.labelLarge)
                }
                (state.questions)
                    .filter { it.topicId == topic }
                    .forEach { q ->
                        AppCard(onClick = { vm.toggle(q.id) }) {
                            Row {
                                Checkbox(
                                    q.id in selected,
                                    { vm.toggle(q.id) },
                                    Modifier.testTag("select-${q.id}"),
                                )
                                MathText(q.expression)
                            }
                        }
                    }
                SecondaryButton(label(TextKey.t_4d4f2c0264), { custom = true })
            }
            2 -> {
                SecondaryButton(
                    label(TextKey.t_c01b19362d),
                    {
                        val cal = Calendar.getInstance().apply { timeInMillis = due }
                        DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val next =
                                        Calendar.getInstance().apply { set(y, m, d, 23, 59, 0) }
                                    vm.due(next.timeInMillis)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH),
                            )
                            .apply { datePicker.minDate = System.currentTimeMillis() }
                            .show()
                    },
                )
                AppTextField(points, { vm.field("points", it) }, label(TextKey.t_273c73d9d4))
                AppTextField(attempts, { vm.field("attempts", it) }, label(TextKey.t_387917ebc5))
                Row {
                    Checkbox(hints, { vm.settings("hints", it) })
                    Text(label(TextKey.t_b0f2d09240))
                }
                Row {
                    Checkbox(late, { vm.settings("late", it) })
                    Text(label(TextKey.t_4261644d59))
                }
            }
            3 -> {
                AppCard {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(desc)
                    Text(state.product.classes.find { it.id == cls }?.name?.text().orEmpty())
                    Text(label(TextKey.t_143f33467e, selected.size, points))
                    selected.forEach {
                        state.product.question(it)?.let { q -> MathText(q.expression) }
                    }
                }
                SecondaryButton(label(TextKey.t_96f7bb3115), { preview = true })
            }
        }
        if (error) Text(label(TextKey.t_7207c84ac9), color = MaterialTheme.colorScheme.error)
        if (step > 0) SecondaryButton(label(TextKey.t_bc14eec819), vm::back)
        PrimaryButton(
            if (step == 3) label(TextKey.t_2019af0928) else label(TextKey.t_704198b924),
            {
                if (step == 3) {
                    if (vm.next()) onPublish(vm.assignment())
                } else vm.next()
            },
            Modifier.testTag("builder-next"),
        )
    }
    if (preview)
        AlertDialog(
            onDismissRequest = { preview = false },
            title = { Text(title) },
            text = {
                DialogColumn {
                    Text(desc)
                    selected.firstOrNull()?.let {
                        state.product.question(it)?.let { q -> QuizQuestion(q, "", {}) }
                    }
                }
            },
            confirmButton = {
                TextButton({ preview = false }) { Text(label(TextKey.t_3cf6b002fb)) }
            },
        )
    if (custom)
        AlertDialog(
            onDismissRequest = { custom = false },
            title = { Text(label(TextKey.t_baef42cd26)) },
            text = {
                DialogColumn {
                    AppTextField(prompt, { prompt = it }, label(TextKey.t_da20e47035))
                    AppTextField(answer, { answer = it }, label(TextKey.t_cb8c6979b1))
                }
            },
            confirmButton = {
                TextButton(
                    {
                        val id = UUID.randomUUID().toString()
                        onQuestion(
                            Question(
                                id,
                                topic,
                                Copy(prompt, prompt),
                                prompt,
                                QuestionKind.Numeric,
                                emptyList(),
                                answer,
                                Copy("Check your calculation.", "تحقّق من الحساب."),
                                Copy("Teacher curated question.", "سؤال أعدّه المعلّم."),
                            )
                        )
                        vm.toggle(id)
                        custom = false
                        prompt = ""
                        answer = ""
                    },
                    enabled = prompt.isNotBlank() && answer.toDoubleOrNull() != null,
                ) {
                    Text(label(TextKey.t_6b1a0cf76e))
                }
            },
        )
}
