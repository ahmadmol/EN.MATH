package com.ahmadmol.enmath.features.courses

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

@Composable
fun StudyScreen(
    state: LearningState,
    onBook: (String) -> Unit,
    onContinue: (String) -> Unit,
    onExtra: (String) -> Unit = {},
) {
    ScreenPage(Modifier.testTag("screen-study")) {
        PageHeading(
            label(TextKey.t_95d012eb32),
            label(TextKey.t_0f4d747002),
            label(TextKey.t_62e9205126),
        )
        DemoNotice(label(TextKey.t_a9ea6ff36b))
        AdaptiveGrid(state.books) { book -> CourseCard(book, state.completed) { onBook(book.id) } }
        PrimaryButton(label(TextKey.t_ef47493ab6), { onContinue(state.lastLessonId) })
        listOf(
                "assignments" to label(TextKey.t_f6202fc5ef),
                "library" to label(TextKey.t_ba46bc2e8b),
                "explore" to label(TextKey.t_a7e385f50a),
            )
            .forEach { (route, label) -> SecondaryButton(label, { onExtra(route) }) }
    }
}

@Composable
fun BookDetailsScreen(
    id: String,
    state: LearningState,
    onTopic: (String) -> Unit,
    onLesson: (String) -> Unit,
) {
    val book = state.books.find { it.id == id }
    ScreenPage(Modifier.testTag("screen-book")) {
        if (book == null) {
            EmptyState(label(TextKey.t_c42ca1990b), label(TextKey.t_107d03c5ec))
            return@ScreenPage
        }
        PageHeading(label(TextKey.t_a56054dc89), book.title.text(), book.subtitle.text())
        val count = book.lessons.count { it.id in state.completed }
        ProgressCard(
            label(TextKey.t_078816e010),
            label(TextKey.t_355ef9b130, count, book.lessons.size),
            count.toFloat() / book.lessons.size,
        )
        val next = book.lessons.firstOrNull { it.id !in state.completed } ?: book.lessons.first()
        PrimaryButton(label(TextKey.t_b64f5160ae), { onLesson(next.id) })
        SectionTitle(label(TextKey.t_f73882a802))
        AdaptiveGrid(book.topics) { topic ->
            TopicCard(topic, state.completed) { onTopic(topic.id) }
        }
    }
}

@Composable
fun TopicLessonsScreen(
    id: String,
    state: LearningState,
    onLesson: (String) -> Unit,
    onPractice: (String) -> Unit = {},
) {
    val topic = state.topic(id)
    ScreenPage(Modifier.testTag("screen-topic")) {
        if (topic == null) {
            EmptyState(label(TextKey.t_77c17be0af), label(TextKey.t_1fa70d1974))
            return@ScreenPage
        }
        PageHeading(label(TextKey.t_12412caf31), topic.title.text(), label(TextKey.t_d3b6dc6e16))
        val book = state.books.find { topic in it.topics }
        Breadcrumbs(book, topic)
        Text(topic.lessons.first().introduction.text())
        MasteryBadge(LearningRules.topic(state, id))
        topic.lessons.forEachIndexed { index, lesson ->
            AppCard(
                onClick = { onLesson(lesson.id) },
                modifier = Modifier.testTag("lesson-${lesson.id}"),
            ) {
                Text(
                    label(TextKey.t_f4aa837b09, index + 1),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(lesson.title.text(), style = MaterialTheme.typography.titleLarge)
                Text(label(TextKey.t_d8b77b269d, lesson.minutes))
                if (lesson.id in state.completed) CompletedLabel()
            }
        }
        PrimaryButton(
            label(TextKey.t_1e9b65ba66),
            { onPractice("$id-practice-set") },
            Modifier.testTag("topic-practice"),
        )
        SecondaryButton(label(TextKey.t_1832fb89d1), { onPractice("$id-quiz") })
    }
}

@Composable
fun LessonScreen(
    id: String,
    state: LearningState,
    onComplete: () -> Unit,
    onPractice: (String) -> Unit = {},
    onNext: (String) -> Unit = {},
    onBookmark: () -> Unit = {},
) {
    val lesson = state.lesson(id)
    ScreenPage(Modifier.testTag("screen-lesson")) {
        if (lesson == null) {
            EmptyState(label(TextKey.t_52542da01d), label(TextKey.t_bbaaeb95d0))
            return@ScreenPage
        }
        PageHeading(
            label(TextKey.t_36df6355b7),
            lesson.title.text(),
            label(TextKey.t_bcc752cc57, lesson.minutes),
        )
        DemoNotice(label(TextKey.t_56f9d36d01))
        val topic = state.topicOf(id)
        Breadcrumbs(state.books.find { b -> b.topics.any { it.id == topic?.id } }, topic, lesson)
        BookmarkButton(id in state.product.savedLessons, onBookmark)
        SectionTitle(label(TextKey.t_bff8e80540))
        Text(lesson.objectives.text())
        Text(lesson.introduction.text(), style = MaterialTheme.typography.bodyLarge)
        AppCard(tonal = true) {
            Text(label(TextKey.t_2622928a98), style = MaterialTheme.typography.titleMedium)
            MathText(lesson.example)
        }
        AppCard {
            Text(label(TextKey.t_2afe0c1a6a), style = MaterialTheme.typography.titleLarge)
            Text(lesson.explanation.text())
        }
        AppCard {
            Text(label(TextKey.t_1486cae7f0), style = MaterialTheme.typography.titleMedium)
            Text(lesson.rule.text())
            Text(label(TextKey.t_b09e391c65), color = MaterialTheme.colorScheme.error)
            Text(lesson.mistake.text())
        }
        if (lesson.checkpoint.isNotBlank()) {
            var answer by rememberSaveable(id) { mutableStateOf("") }
            var checked by rememberSaveable(id) { mutableStateOf(false) }
            AppCard {
                Text(label(TextKey.t_50b6918489))
                MathText(lesson.checkpoint)
                AppTextField(
                    answer,
                    {
                        answer = it
                        checked = false
                    },
                    label(TextKey.t_8c507ee733),
                )
                SecondaryButton(label(TextKey.t_875f805f63), { checked = true })
                if (checked)
                    Text(
                        if (answer.trim() == lesson.checkpointAnswer) label(TextKey.t_584fa3f349)
                        else label(TextKey.t_78843a538d)
                    )
            }
        }
        AppCard {
            Text(label(TextKey.t_25ad5c35f7), style = MaterialTheme.typography.titleMedium)
            Text(label(TextKey.t_e5a3201c0e))
        }
        if (id in state.completed) CompletedLabel()
        PrimaryButton(
            if (id in state.completed) label(TextKey.t_c3a665fd1c) else label(TextKey.t_0c7cde1bf8),
            onComplete,
            Modifier.testTag("complete-lesson"),
            enabled = id !in state.completed,
        )
        PrimaryButton(label(TextKey.t_aadf42c032), { onPractice("${topic?.id}-practice-set") })
        val next = topic?.lessons?.dropWhile { it.id != id }?.drop(1)?.firstOrNull()
        if (next != null) SecondaryButton(label(TextKey.t_f07af32616), { onNext(next.id) })
    }
}
