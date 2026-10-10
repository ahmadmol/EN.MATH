package com.ahmadmol.enmath.features.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun TeacherAssignmentsScreen(
    state: LearningState,
    onCreate: () -> Unit,
    onResult: (String) -> Unit,
) {
    ScreenPage(Modifier.testTag("screen-teacher-assignments")) {
        PageHeading(label(TextKey.t_c0a8e05970), label(TextKey.t_0c59f4a1fc))
        PrimaryButton(label(TextKey.t_4c5461762c), onCreate, Modifier.testTag("create-assignment"))
        if (state.product.assignments.isEmpty())
            EmptyState(label(TextKey.t_4c6c96d9ab), label(TextKey.t_9df1de9197))
        state.product.assignments.forEach { a ->
            AssignmentCard(
                a,
                state.product.classes.find { it.id == a.classId }?.name?.text().orEmpty(),
                label(
                    TextKey.t_d34a495278,
                    state.product.submissions
                        .filter { it.assignmentId == a.id }
                        .distinctBy { it.studentId }
                        .size,
                ),
                { onResult(a.id) },
            )
        }
    }
}

@Composable
fun TeacherResultsScreen(id: String, state: LearningState, onStudent: (String) -> Unit) {
    val a = state.product.assignments.find { it.id == id }
    val submissions =
        state.product.submissions
            .filter { it.assignmentId == id }
            .sortedByDescending { it.timestamp }
            .distinctBy { it.studentId }
    val students = state.product.classes.find { it.id == a?.classId }?.studentIds.orEmpty()
    ScreenPage(Modifier.testTag("screen-teacher-results")) {
        PageHeading(label(TextKey.t_02debd4de3), a?.title?.text().orEmpty())
        if (a == null) {
            EmptyState(label(TextKey.t_7625c0de23), label(TextKey.t_bd767ed1d0))
            return@ScreenPage
        }
        ProgressCard(
            label(TextKey.t_46011237a5),
            "${submissions.size}/${students.size}",
            if (students.isEmpty()) 0f else submissions.size.toFloat() / students.size,
        )
        Text(
            label(
                TextKey.t_570d74456a,
                if (submissions.isEmpty()) 0 else submissions.map { it.score }.average().toInt(),
                a.points,
            )
        )
        if (submissions.isEmpty())
            EmptyState(label(TextKey.t_b8132a67be), label(TextKey.t_0e8ad197d6))
        students.forEach { idStudent ->
            AppCard(onClick = { onStudent(idStudent) }) {
                Text(state.students.find { it.id == idStudent }?.name?.text().orEmpty())
                val s = submissions.find { it.studentId == idStudent }
                Text(if (s == null) label(TextKey.t_34ec2073de) else "${s.score}/${a.points}")
                if (s != null) Text(label(TextKey.t_5771670ed0, s.seconds))
            }
        }
    }
}
