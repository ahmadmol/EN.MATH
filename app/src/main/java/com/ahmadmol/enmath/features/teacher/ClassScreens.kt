package com.ahmadmol.enmath.features.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun ClassesScreen(state: LearningState, onCreate: (String) -> Unit, onOpen: (String) -> Unit) {
    var modal by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    ScreenPage(Modifier.testTag("screen-classes")) {
        PageHeading(label(TextKey.t_71927f007e), label(TextKey.t_6bd79c49cf))
        PrimaryButton(
            label(TextKey.t_8930947435),
            { modal = true },
            Modifier.testTag("create-class"),
        )
        if (state.product.classes.isEmpty())
            EmptyState(label(TextKey.t_8834284075), label(TextKey.t_6a4eb9f1da))
        state.product.classes.forEach { c ->
            AppCard(onClick = { onOpen(c.id) }) {
                Text(c.name.text(), style = MaterialTheme.typography.titleLarge)
                Text(label(TextKey.t_2741e5a6bc, c.studentIds.size, classAverage(state, c)))
                Text(
                    label(
                        TextKey.t_e5ef7b2777,
                        state.product.assignments.count {
                            it.classId == c.id && it.due >= System.currentTimeMillis()
                        },
                    )
                )
            }
        }
    }
    if (modal)
        AlertDialog(
            onDismissRequest = { modal = false },
            title = { Text(label(TextKey.t_05791afd64)) },
            text = {
                AppTextField(
                    name,
                    { name = it },
                    label(TextKey.t_cb0926a9d0),
                    Modifier.testTag("class-name"),
                )
            },
            confirmButton = {
                TextButton(
                    {
                        onCreate(name.trim())
                        modal = false
                        name = ""
                    },
                    enabled = name.trim().length >= 3,
                    modifier = Modifier.testTag("class-save"),
                ) {
                    Text(label(TextKey.t_ca1b37c1d5))
                }
            },
            dismissButton = { TextButton({ modal = false }) { Text(label(TextKey.t_823a3ad20c)) } },
        )
}

@Composable
fun ClassDetailsScreen(
    id: String,
    state: LearningState,
    onStudent: (String) -> Unit,
    onAssignment: (String) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var sort by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val c = state.product.classes.find { it.id == id }
    ScreenPage {
        if (c == null) {
            EmptyState(label(TextKey.t_4dc29f72b5), label(TextKey.t_9c1fa20a5e))
            return@ScreenPage
        }
        PageHeading(label(TextKey.t_f29b07fbb1), c.name.text())
        AppCard {
            Text(label(TextKey.t_7ffa43d992))
            MathText(c.code)
            SecondaryButton(
                if (copied) label(TextKey.t_e10c7acb96) else label(TextKey.t_6d7c14d4a9),
                {
                    (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager)
                        .setPrimaryClip(android.content.ClipData.newPlainText(c.code, c.code))
                    copied = true
                },
            )
        }
        ChipRow(
            listOf(
                label(TextKey.t_453a44248c),
                label(TextKey.t_bd98530a61),
                label(TextKey.t_f6202fc5ef),
                label(TextKey.t_693fec8aa7),
            ),
            tab,
            { tab = it },
        )
        when (tab) {
            0 -> {
                ProgressCard(
                    label(TextKey.t_d43aa2eb48),
                    state.topic(c.topicId)?.title?.text().orEmpty(),
                    classAverage(state, c) / 100f,
                )
                Text(label(TextKey.t_d80f76e3c2, c.studentIds.size))
                SectionTitle(label(TextKey.t_e1598e46ee))
                state.product.submissions
                    .filter { s ->
                        state.product.assignments.any {
                            it.id == s.assignmentId && it.classId == id
                        }
                    }
                    .take(5)
                    .forEach {
                        Text(
                            state.students
                                .find { p -> p.id == it.studentId }
                                ?.name
                                ?.text()
                                .orEmpty()
                        )
                    }
            }
            1 -> {
                TextButton({ sort = !sort }) { Text(label(TextKey.t_c9f366dc0e)) }
                val students = state.students.filter { it.id in c.studentIds }
                (if (sort) students.sortedBy { LearningRules.topic(state, c.topicId, it.id).score }
                    else students)
                    .forEach { s ->
                        StudentRow(s.name.text(), LearningRules.topic(state, c.topicId, s.id)) {
                            onStudent(s.id)
                        }
                    }
                if (students.isEmpty())
                    EmptyState(label(TextKey.t_c185218689), label(TextKey.t_6e6253393d))
            }
            2 -> {
                state.product.assignments
                    .filter { it.classId == id }
                    .forEach { a -> AssignmentCard(a, c.name.text(), "", { onAssignment(a.id) }) }
            }
            else -> {
                state.books
                    .flatMap { it.topics }
                    .forEach { t ->
                        val average =
                            if (c.studentIds.isEmpty()) 0
                            else
                                c.studentIds
                                    .map { LearningRules.topic(state, t.id, it).score }
                                    .average()
                                    .toInt()
                        ProgressCard(t.title.text(), label(TextKey.t_95a7630817), average / 100f)
                    }
            }
        }
    }
}

@Composable
fun StudentDetailsScreen(id: String, state: LearningState, onAssignment: (String) -> Unit) {
    val student = state.students.find { it.id == id }
    ScreenPage {
        PageHeading(label(TextKey.t_cb7e56cc66), student?.name?.text().orEmpty())
        if (student == null) {
            EmptyState(label(TextKey.t_5500aaa704), label(TextKey.t_c7bd6f097a))
            return@ScreenPage
        }
        state.books
            .flatMap { it.topics }
            .forEach { t ->
                AppCard {
                    Text(t.title.text())
                    MasteryBadge(LearningRules.topic(state, t.id, id))
                }
            }
        val completed = if (id == "ahmed") state.completed.size else student.completed.size
        Text(label(TextKey.t_077e621825, completed))
        val accuracy =
            if (id == "ahmed") LearningRules.stats(state).accuracy else student.practiceAccuracy
        Text(label(TextKey.t_e317f22089, (accuracy * 100).toInt()))
        SectionTitle(label(TextKey.t_f6202fc5ef))
        state.product.assignments
            .filter { a -> state.product.classes.any { it.id == a.classId && id in it.studentIds } }
            .forEach { a -> AssignmentCard(a, "", "", { onAssignment(a.id) }) }
        DemoNotice(label(TextKey.t_1e037b60b5))
        if (id == "ahmed") state.product.activity.takeLast(3).forEach { Text(it.title.text()) }
    }
}
