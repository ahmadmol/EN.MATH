package com.ahmadmol.enmath.core.design.components.teacher

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*

@Composable
fun TeacherSummary(name: String, students: Int, classes: Int, average: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading(
            tr("Teacher workspace", "مساحة المعلم"),
            name,
            tr("See clearly. Choose your next action.", "رؤية واضحة، ثم إجراء مناسب."),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(
                    students to tr("students", "طالباً"),
                    classes to tr("classes", "شعب"),
                    average to tr("mastery %", "إتقان %"),
                )
                .forEach { (value, title) ->
                    Column(Modifier.weight(1f)) {
                        Text("$value", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
        }
    }
}

@Composable
fun TeacherInsightPanel(
    title: String,
    description: String,
    action: String,
    onAction: () -> Unit,
    tag: String = "teacher-insight",
    actionEnabled: Boolean = true,
) {
    ExperiencePanel(Modifier.testTag(tag)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodyMedium)
        TextButton(onAction, enabled = actionEnabled) { Text(action) }
    }
}

@Composable
fun ClassRow(name: String, description: String, score: Int, onOpen: () -> Unit, tag: String) {
    AppCard(Modifier.testTag(tag), onClick = onOpen) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "$score%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun ClassPerformance(
    topic: String,
    score: Int,
    struggling: Int,
    assigned: Int,
    tag: String = "class-performance",
) {
    Column(Modifier.testTag(tag), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailRow(topic, "$score%")
        LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth())
        Text(
            tr(
                "$struggling need support · $assigned assigned practice sets",
                "$struggling بحاجة لدعم · $assigned تدريبات مسندة",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun StudentPerformanceRow(
    name: String,
    score: Int,
    detail: String,
    onOpen: () -> Unit,
    tag: String,
) {
    AppCard(Modifier.testTag(tag), onClick = onOpen) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "$score%",
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (score < 45) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun AssignmentSummary(title: String, detail: String, onOpen: () -> Unit, tag: String) {
    AppCard(Modifier.testTag(tag), onClick = onOpen) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherActionBar(actions: List<Pair<String, () -> Unit>>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        actions.forEachIndexed { index, (title, action) ->
            OutlinedButton(action, Modifier.testTag("teacher-action-$index")) { Text(title) }
        }
    }
}

@Composable
fun TeacherFilterBar(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    prefix: String = "teacher-filter-",
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, title ->
            FilterChip(
                selected == index,
                { onSelect(index) },
                label = { Text(title) },
                modifier = Modifier.testTag("$prefix$index"),
            )
        }
    }
}
