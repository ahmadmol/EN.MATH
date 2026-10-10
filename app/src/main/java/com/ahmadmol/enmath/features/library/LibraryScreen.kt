package com.ahmadmol.enmath.features.library

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

@Composable
fun LibraryScreen(state: LearningState, onLesson: (String) -> Unit, onProblem: (String) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableIntStateOf(0) }
    var resourceType by rememberSaveable { mutableIntStateOf(0) }
    val topics = state.books.flatMap { it.topics }
    val normalized = LearningRules.search(query)
    ScreenPage {
        PageHeading(label(TextKey.t_ac68e53c7f), label(TextKey.t_0f029af391))
        SearchField(query, { query = it })
        ChipRow(
            listOf(
                label(TextKey.t_6c48dfb4c3),
                label(TextKey.t_10881e833c),
                label(TextKey.t_4ae85809f8),
            ),
            tab,
            { tab = it },
        )
        ChipRow(
            listOf(label(TextKey.t_dd0a8d9c19)) + topics.map { it.title.text() },
            topic,
            { topic = it },
        )
        var count = 0
        if (tab == 0)
            state.books
                .flatMap { it.lessons }
                .filter {
                    it.id in state.product.savedLessons &&
                        LearningRules.search(it.title.en + it.title.ar).contains(normalized) &&
                        (topic == 0 || it.id.startsWith(topics[topic - 1].id))
                }
                .forEach { l ->
                    count++
                    LessonCard(l, l.id in state.completed, { onLesson(l.id) })
                }
        if (tab == 1)
            state.product.history
                .filter {
                    it.id in state.product.savedProblems &&
                        LearningRules.search(it.expression).contains(normalized) &&
                        (topic == 0 || it.topicId == topics[topic - 1].id)
                }
                .forEach { h ->
                    count++
                    AppCard(onClick = { onProblem(h.id) }) {
                        MathText(h.expression)
                        SecondaryButton(label(TextKey.t_0bbe6c9c93), { onProblem(h.id) })
                    }
                }
        if (tab == 2) {
            ChipRow(
                listOf(label(TextKey.t_d9742902f4)) +
                    com.ahmadmol.enmath.core.model.ResourceKind.entries.map {
                        resourceKindLabel(it)
                    },
                resourceType,
                { resourceType = it },
            )
            state.product.resources
                .filter { r ->
                    r.classIds.any { c ->
                        state.product.classes.any { it.id == c && "ahmed" in it.studentIds }
                    } &&
                        LearningRules.search(r.title.en + r.title.ar).contains(normalized) &&
                        (topic == 0 || r.topicId == topics[topic - 1].id) &&
                        (resourceType == 0 ||
                            r.kind ==
                                com.ahmadmol.enmath.core.model.ResourceKind.entries[
                                        resourceType - 1])
                }
                .forEach { r ->
                    count++
                    AppCard {
                        Text(r.title.text(), style = MaterialTheme.typography.titleLarge)
                        Text(resourceKindLabel(r.kind))
                        Text(r.body.text())
                    }
                }
        }
        if (count == 0) EmptyState(label(TextKey.t_88fac26dd6), label(TextKey.t_3fc2deecf5))
    }
}

@Composable
fun resourceKindLabel(kind: com.ahmadmol.enmath.core.model.ResourceKind) =
    when (kind) {
        com.ahmadmol.enmath.core.model.ResourceKind.LessonNote -> label(TextKey.t_d9613968c1)
        com.ahmadmol.enmath.core.model.ResourceKind.FormulaSheet -> label(TextKey.t_7722c61c7e)
        com.ahmadmol.enmath.core.model.ResourceKind.PracticeSet -> label(TextKey.t_3b45300a1e)
        else -> label(TextKey.t_7a5643ef0a)
    }
