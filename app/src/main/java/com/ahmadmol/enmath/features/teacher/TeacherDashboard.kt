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
fun TeacherDashboard(state: LearningState, onRoute: (String) -> Unit) {
    val p = state.product
    val active = p.assignments.count { it.due >= System.currentTimeMillis() }
    val needs =
        state.students.filter { LearningRules.topic(state, "derivatives", it.id).score < 45 }
    ScreenPage(Modifier.testTag("screen-teacher")) {
        PageHeading(
            label(TextKey.t_f64fd3a47a),
            label(TextKey.t_3b34128f7d),
            label(TextKey.t_9305ec4b3f),
        )
        OfflineState()
        AdaptivePair(
            first = {
                AppCard {
                    Text(label(TextKey.t_5c0aca2c59, p.classes.size))
                    Text(
                        label(
                            TextKey.t_9e729bd400,
                            p.classes.flatMap { it.studentIds }.distinct().size,
                        )
                    )
                }
            },
            second = {
                AppCard {
                    Text(label(TextKey.t_00f5382f8b, active))
                    Text(label(TextKey.t_55cee8e73c, p.submissions.count { !it.reviewed }))
                }
            },
        )
        PrimaryButton(
            label(TextKey.t_4c5461762c),
            { onRoute("teacher/assignments/new") },
            Modifier.testTag("create-assignment"),
        )
        SecondaryButton(label(TextKey.t_2ade063f4e), { onRoute("teacher/classes") })
        SectionTitle(label(TextKey.t_fff2e85454))
        p.classes.forEach { c ->
            TeacherInsightCard(c.name.text(), label(TextKey.t_a71cff4692, classAverage(state, c))) {
                onRoute("teacher/classes/${c.id}")
            }
        }
        SectionTitle(label(TextKey.t_52188cab48))
        DemoNotice(label(TextKey.t_b1e90dd5fe))
        needs.take(4).forEach { s ->
            val c = p.classes.find { s.id in it.studentIds }
            StudentRow(s.name.text(), LearningRules.topic(state, "derivatives", s.id)) {
                if (c != null) onRoute("teacher/classes/${c.id}/students/${s.id}")
            }
        }
        SectionTitle(label(TextKey.t_210473961b))
        p.submissions
            .sortedByDescending { it.timestamp }
            .take(4)
            .forEach { s ->
                AppCard(
                    onClick = {
                        onRoute("teacher/assignments/${s.assignmentId}/responses/${s.studentId}")
                    }
                ) {
                    Text(state.students.find { it.id == s.studentId }?.name?.text().orEmpty())
                    Text(p.assignments.find { it.id == s.assignmentId }?.title?.text().orEmpty())
                }
            }
    }
}
