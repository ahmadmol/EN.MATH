package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.model.*

object Elevation {
    val low = Space.xs
    val medium = Space.sm
    val high = Space.md
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick, modifier.fillMaxWidth().heightIn(min = Space.touch)) { Text(label) }
}

@Composable
fun DialogColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Space.md),
        content = content,
    )
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit) =
    AppTextField(value, onChange, label(TextKey.t_67b32d1cb9), Modifier.testTag("search-input"))

@Composable
fun PasswordField(value: String, onChange: (String) -> Unit) =
    AppTextField(value, onChange, label(TextKey.t_a98160205a), password = true)

@Composable
fun OtpInput(value: String, onChange: (String) -> Unit) =
    AppTextField(value, { onChange(it.filter(Char::isDigit).take(6)) }, label(TextKey.t_f7cdc994c6))

@Composable
fun EquationCard(value: String) {
    AppCard(tonal = true) { MathText(value) }
}

@Composable
fun ProgressRing(progress: Float) {
    Box {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(Space.symbol),
            strokeWidth = Space.xs,
        )
        Text("${(progress*100).toInt()}%", modifier = Modifier.padding(Space.md))
    }
}

@Composable
fun MasteryBadge(mastery: TopicMastery) {
    val label =
        when (mastery.stage) {
            Mastery.NotStarted -> label(TextKey.t_b00f71719c)
            Mastery.Started -> label(TextKey.t_2456e13f3d)
            Mastery.Learning -> label(TextKey.t_5afd491027)
            Mastery.Practicing -> label(TextKey.t_c91c031f4f)
            Mastery.Proficient -> label(TextKey.t_9f6792bbf7)
            Mastery.Mastered -> label(TextKey.t_0aa59cb367)
        }
    Surface(
        color =
            when (mastery.stage) {
                Mastery.NotStarted -> MaterialTheme.colorScheme.surfaceVariant
                Mastery.Started -> MaterialTheme.colorScheme.primaryContainer
                Mastery.Learning -> MaterialTheme.colorScheme.tertiaryContainer
                Mastery.Practicing -> MaterialTheme.colorScheme.primaryContainer
                Mastery.Proficient -> MaterialTheme.colorScheme.secondaryContainer
                Mastery.Mastered -> MaterialTheme.colorScheme.secondary
            },
        shape = MaterialTheme.shapes.small,
    ) {
        Row(Modifier.padding(Space.sm), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            Icon(
                when (mastery.stage) {
                    Mastery.NotStarted -> Icons.Outlined.RadioButtonUnchecked
                    Mastery.Started -> Icons.Outlined.Flag
                    Mastery.Learning -> Icons.Outlined.School
                    Mastery.Practicing -> Icons.Outlined.EditNote
                    Mastery.Proficient -> Icons.Outlined.Verified
                    Mastery.Mastered -> Icons.Outlined.EmojiEvents
                },
                null,
            )
            Text("$label · ${mastery.score}%")
        }
    }
}

@Composable
fun StreakBadge(days: Int) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(Modifier.padding(Space.md), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            Icon(Icons.Outlined.LocalFireDepartment, null)
            Text(label(TextKey.t_4cf108fea1, days))
        }
    }
}

@Composable
fun Avatar(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.size(Space.symbol),
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(
                name
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first() }
                    .joinToString(""),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson, done: Boolean, onClick: () -> Unit) {
    AppCard(onClick = onClick, modifier = Modifier.testTag("lesson-${lesson.id}")) {
        Text(lesson.title.text(), style = MaterialTheme.typography.titleMedium)
        Text(label(TextKey.t_516f1fe3b4, lesson.minutes))
        if (done) CompletedLabel()
    }
}

@Composable
fun AssignmentCard(a: Assignment, className: String, status: String, onClick: () -> Unit) {
    AppCard(onClick = onClick, modifier = Modifier.testTag("assignment-${a.id}")) {
        Text(a.title.text(), style = MaterialTheme.typography.titleLarge)
        Text(className)
        Text(status)
        Text(label(TextKey.t_143f33467e, a.questionIds.size, a.points))
    }
}

@Composable
fun StudentRow(name: String, mastery: TopicMastery, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Text(name, style = MaterialTheme.typography.titleMedium)
        MasteryBadge(mastery)
    }
}

@Composable
fun TeacherInsightCard(title: String, description: String, onClick: () -> Unit) {
    AppCard(onClick = onClick, tonal = true) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description)
    }
}

@Composable
fun NotificationItem(note: AppNotification, read: Boolean, onClick: () -> Unit) {
    AppCard(onClick = onClick, tonal = !read) {
        Text(note.title.text(), style = MaterialTheme.typography.titleMedium)
        Text(note.body.text())
        if (!read) Text(label(TextKey.t_03e25447ec), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorState(detail: String, retry: () -> Unit) {
    EmptyState(label(TextKey.t_751040dfab), detail)
    SecondaryButton(label(TextKey.t_00cac031ea), retry)
}

@Composable
fun OfflineState() {
    DemoNotice(label(TextKey.t_e43176af35))
}

@Composable
fun ChipRow(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        labels.forEachIndexed { i, label ->
            FilterChip(selected == i, { onSelect(i) }, label = { Text(label) })
        }
    }
}

@Composable
fun BookmarkButton(saved: Boolean, onClick: () -> Unit) {
    TextButton(onClick, Modifier.testTag("bookmark")) {
        Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null)
        Text(if (saved) label(TextKey.t_39b8bf7830) else label(TextKey.t_ab3669edfa))
    }
}

@Composable
fun Breadcrumbs(book: Book?, topic: Topic?, lesson: Lesson? = null) {
    Text(
        listOfNotNull(book?.title?.text(), topic?.title?.text(), lesson?.title?.text())
            .joinToString(" › "),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun AccountPrompt(onDismiss: () -> Unit, onCreate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label(TextKey.t_fccd2dda9f)) },
        text = { Text(label(TextKey.t_01ec0c6e4e)) },
        confirmButton = { TextButton(onCreate) { Text(label(TextKey.t_604824dd52)) } },
        dismissButton = { TextButton(onDismiss) { Text(label(TextKey.t_90d7e21b2f)) } },
    )
}

@Composable
fun AdaptivePair(first: @Composable () -> Unit, second: @Composable () -> Unit) {
    BoxWithConstraints {
        if (maxWidth >= Space.formMax + Space.xl)
            Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
                    first()
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
                    second()
                }
            }
        else
            Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
                first()
                second()
            }
    }
}
