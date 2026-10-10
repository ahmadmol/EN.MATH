package com.ahmadmol.enmath.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun StudentHomeScreen(state: LearningState, onRoute: (String) -> Unit) {
    val stats = LearningRules.stats(state)
    val lesson = state.lesson(state.lastLessonId) ?: state.books.first().lessons.first()
    val name =
        if (state.user?.type == AccountType.Guest) label(TextKey.t_5aef859af5)
        else state.user?.name?.takeIf { it.isNotBlank() } ?: state.students.first().name.text()
    ScreenPage(Modifier.testTag("screen-home")) {
        PageHeading(
            label(TextKey.t_90986b28c4),
            label(TextKey.t_d04051cfbc, name),
            label(TextKey.t_954c01e15f),
        )
        StreakBadge(stats.streak)
        AdaptivePair(
            first = {
                AppCard(tonal = true) {
                    Text(label(TextKey.t_b511f62c6c))
                    Text(lesson.title.text(), style = MaterialTheme.typography.titleLarge)
                    PrimaryButton(
                        label(TextKey.t_708dd82b5a),
                        { onRoute("lesson/${lesson.id}") },
                        Modifier.testTag("continue-lesson"),
                    )
                }
            },
            second = {
                ProgressCard(
                    label(TextKey.t_4d829a1a74),
                    "${state.completed.size}/${state.books.sumOf {it.lessons.size}}",
                    stats.lessonFraction,
                )
                val goal = state.dailyGoal
                val done =
                    state.product.activity.count {
                        it.timestamp / 86_400_000 == System.currentTimeMillis() / 86_400_000
                    }
                ProgressCard(
                    label(TextKey.t_5bb34a4d86),
                    label(TextKey.t_4a0914c00b, done, goal),
                    (done.toFloat() / goal.coerceAtLeast(1)).coerceAtMost(1f),
                )
            },
        )
        SectionTitle(label(TextKey.t_cc8437d5b9))
        AdaptivePair(
            first = {
                PrimaryButton(label(TextKey.t_771e1a0fee), { onRoute("solver") })
                SecondaryButton(label(TextKey.t_a7e385f50a), { onRoute("explore") })
            },
            second = {
                SecondaryButton(label(TextKey.t_627d3b1618), { onRoute("search") })
                SecondaryButton(label(TextKey.t_e069c044c0), { onRoute("library") })
            },
        )
        val upcoming =
            state.product.assignments
                .filter { a ->
                    a.due >= System.currentTimeMillis() &&
                        state.product.classes.any {
                            it.id == a.classId && "ahmed" in it.studentIds
                        } &&
                        state.product.submissions.none {
                            it.assignmentId == a.id && it.studentId == "ahmed"
                        }
                }
                .minByOrNull { it.due }
        if (upcoming != null) {
            SectionTitle(label(TextKey.t_306c0f93f4))
            AppCard(onClick = { onRoute("assignments/${upcoming.id}") }) {
                Text(upcoming.title.text())
                Text(upcoming.description.text())
            }
        }
        val recommended =
            state.books
                .flatMap { it.topics }
                .minByOrNull { LearningRules.topic(state, it.id).score }!!
        SectionTitle(label(TextKey.t_e7e7796656))
        TopicCard(recommended, state.completed) { onRoute("topic/${recommended.id}") }
        SectionTitle(label(TextKey.t_210473961b))
        state.product.activity
            .sortedByDescending { it.timestamp }
            .take(4)
            .forEach { a ->
                AppCard {
                    Text(a.title.text())
                    Text(label(TextKey.t_a5ceb9dc53, a.minutes))
                }
            }
    }
}
