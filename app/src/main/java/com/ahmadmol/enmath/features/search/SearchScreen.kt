package com.ahmadmol.enmath.features.search

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

@Composable
fun SearchScreen(state: LearningState, onSearch: (String) -> Unit, onRoute: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val term = LearningRules.search(query)
    ScreenPage {
        PageHeading(label(TextKey.t_a33bc556a4), label(TextKey.t_4fb8ba1914))
        SearchField(query, { query = it })
        SecondaryButton(label(TextKey.t_00965319c2), { onSearch(query) })
        if (term.isBlank()) {
            SectionTitle(label(TextKey.t_b67d86605d))
            state.product.searches.forEach { s -> TextButton({ query = s }) { Text(s) } }
            ChipRow(
                listOf(
                    label(TextKey.t_81ed0ddb40),
                    label(TextKey.t_5bb2f73c0f),
                    label(TextKey.t_492519ffec),
                    label(TextKey.t_584f9760f5),
                ),
                -1,
                { query = listOf("limits", "derivatives", "integrals", "matrices")[it] },
            )
            return@ScreenPage
        }
        fun match(en: String, ar: String) = LearningRules.search(en + " " + ar).contains(term)
        val topics = state.books.flatMap { it.topics }.filter { match(it.title.en, it.title.ar) }
        val lessons = state.books.flatMap { it.lessons }.filter { match(it.title.en, it.title.ar) }
        val concepts = state.books.flatMap { it.lessons }.filter { match(it.rule.en, it.rule.ar) }
        val questions =
            state.questions.filter {
                match(it.prompt.en + it.expression, it.prompt.ar + it.topicId)
            }
        if (topics.isEmpty() && lessons.isEmpty() && concepts.isEmpty() && questions.isEmpty())
            EmptyState(label(TextKey.t_b8ee1e0f8c), label(TextKey.t_95644465fb))
        if (topics.isNotEmpty()) {
            SectionTitle(label(TextKey.t_519b0bc526))
            topics.forEach { t ->
                TopicCard(t, state.completed) {
                    onSearch(query)
                    onRoute("topic/${t.id}")
                }
            }
        }
        if (lessons.isNotEmpty()) {
            SectionTitle(label(TextKey.t_6c48dfb4c3))
            lessons.forEach { l ->
                LessonCard(l, l.id in state.completed) {
                    onSearch(query)
                    onRoute("lesson/${l.id}")
                }
            }
        }
        if (concepts.isNotEmpty()) {
            SectionTitle(label(TextKey.t_bdb85c717d))
            concepts.take(8).forEach { l ->
                AppCard(onClick = { onRoute("lesson/${l.id}") }) {
                    Text(l.title.text())
                    Text(l.rule.text())
                }
            }
        }
        if (questions.isNotEmpty()) {
            SectionTitle(label(TextKey.t_353becf694))
            questions.take(8).forEach { q ->
                AppCard(onClick = { onRoute("practice/${q.topicId}-practice-set") }) {
                    MathText(q.expression)
                    SecondaryButton(
                        label(TextKey.t_0991118449),
                        { onRoute("practice/${q.topicId}-practice-set") },
                    )
                }
            }
        }
    }
}
