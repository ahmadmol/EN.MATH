package com.ahmadmol.enmath.features.assignments

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun dueLabel(a: Assignment): String {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(a.due))
    return label(TextKey.t_3992c25ae7, date)
}

fun assignmentStatus(a: Assignment, p: ProductState): Int =
    if (p.submissions.any { it.assignmentId == a.id && it.studentId == "ahmed" }) 1
    else if (a.due < System.currentTimeMillis()) 2 else 0

@Composable
fun AssignmentListScreen(state: LearningState, onOpen: (String) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    ScreenPage(Modifier.testTag("screen-assignments")) {
        PageHeading(label(TextKey.t_8c0dfa71a2), label(TextKey.t_e1af75d936))
        ChipRow(
            listOf(
                label(TextKey.t_6f798f756c),
                label(TextKey.t_8891e609f6),
                label(TextKey.t_834d0bdb63),
            ),
            selected,
            { selected = it },
        )
        val list =
            state.product.assignments.filter { a ->
                state.product.classes.any { it.id == a.classId && "ahmed" in it.studentIds } &&
                    assignmentStatus(a, state.product) == selected
            }
        if (list.isEmpty()) EmptyState(label(TextKey.t_61177eddae), label(TextKey.t_f8891afcdc))
        list.forEach { a ->
            AssignmentCard(
                a,
                state.product.classes.find { it.id == a.classId }?.name?.text().orEmpty(),
                dueLabel(a),
                { onOpen(a.id) },
            )
        }
    }
}

@Composable
fun AssignmentDetailsScreen(
    id: String,
    state: LearningState,
    onWork: () -> Unit,
    onResult: () -> Unit,
) {
    val a = state.product.assignments.find { it.id == id }
    ScreenPage {
        if (a == null) {
            EmptyState(label(TextKey.t_7625c0de23), label(TextKey.t_bd767ed1d0))
            return@ScreenPage
        }
        PageHeading(label(TextKey.t_361f0f2133), a.title.text(), a.description.text())
        Text(dueLabel(a))
        Text(label(TextKey.t_ecb768ca7f, a.points, a.attempts))
        DemoNotice(label(TextKey.t_002749be01))
        a.questionIds.forEach { q ->
            state.product.question(q)?.let { EquationCard(it.expression) }
        }
        val submissions =
            state.product.submissions.count { it.assignmentId == id && it.studentId == "ahmed" }
        if (submissions > 0) PrimaryButton(label(TextKey.t_934f76a30f), onResult)
        if (submissions < a.attempts && (a.late || a.due >= System.currentTimeMillis()))
            PrimaryButton(label(TextKey.t_6c721d035d), onWork, Modifier.testTag("assignment-work"))
        else if (submissions == 0)
            EmptyState(label(TextKey.t_fa515c7f77), label(TextKey.t_3e512eb130))
    }
}

@Composable
fun AssignmentResultScreen(
    id: String,
    state: LearningState,
    studentId: String = "ahmed",
    onFeedback: ((String) -> Unit)? = null,
) {
    val a = state.product.assignments.find { it.id == id }
    val s =
        state.product.submissions.lastOrNull { it.assignmentId == id && it.studentId == studentId }
    ScreenPage(Modifier.testTag("screen-assignment-result")) {
        PageHeading(label(TextKey.t_511d9de5cb), a?.title?.text().orEmpty())
        if (a == null || s == null) {
            EmptyState(label(TextKey.t_34ec2073de), label(TextKey.t_7a0370d97e))
            return@ScreenPage
        }
        ProgressCard(
            label(TextKey.t_9178974d1f),
            "${s.score}/${a.points}",
            s.score.toFloat() / a.points,
        )
        Text(s.feedback.text())
        Text(label(TextKey.t_03e1b7049a, s.seconds))
        if (onFeedback != null) {
            var body by rememberSaveable(id, studentId) { mutableStateOf("") }
            var success by remember { mutableStateOf(false) }
            AppTextField(
                body,
                {
                    body = it
                    success = false
                },
                label(TextKey.t_6f5885c2d7),
                Modifier.testTag("teacher-feedback"),
                singleLine = false,
            )
            PrimaryButton(
                label(TextKey.t_4d07fefeac),
                {
                    onFeedback(body)
                    success = true
                },
                enabled = body.isNotBlank(),
            )
            if (success) DemoNotice(label(TextKey.t_8656fa1998))
        }
        a.questionIds.forEach { qId ->
            state.product.question(qId)?.let { q ->
                AppCard {
                    MathText(q.expression)
                    Text(label(TextKey.t_951cf2f627))
                    MathText(s.answers[qId].orEmpty())
                    Text(
                        if (LearningRules.correct(q, s.answers[qId].orEmpty()))
                            label(TextKey.t_584fa3f349)
                        else label(TextKey.t_ac75c05199)
                    )
                    Text(label(TextKey.t_cb8c6979b1))
                    MathText(q.answer)
                    Text(q.explanation.text())
                }
            }
        }
    }
}
