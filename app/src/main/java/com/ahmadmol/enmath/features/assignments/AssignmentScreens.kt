package com.ahmadmol.enmath.features.assignments

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

fun assignmentStatus(a: Assignment, p: ProductState, studentId: String = "ahmed"): Int =
    if (p.submissions.any { it.assignmentId == a.id && it.studentId == studentId }) 1
    else if (a.due < System.currentTimeMillis()) 2 else 0

private data class AssignmentTabItem(val index: Int, val title: String, val icon: ImageVector)

@Composable
fun AssignmentListScreen(state: LearningState, onOpen: (String) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    ScreenPage(Modifier.testTag("screen-assignments")) {
        // Hero Section matching Image 3
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Text(
                        text = tr("Grade", "الصف"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = label(TextKey.t_e1af75d936),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tr("Track your assignments and complete them on time.", "تابع واجباتك وحلّها في الوقت المناسب."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssignmentsHeroArtwork(Modifier.size(100.dp))
            }
        }

        // Tabs/Filters Bar matching Image 3
        val tabs = listOf(
            AssignmentTabItem(0, label(TextKey.t_6f798f756c), Icons.AutoMirrored.Outlined.List),
            AssignmentTabItem(1, label(TextKey.t_8891e609f6), Icons.Outlined.CheckCircle),
            AssignmentTabItem(2, label(TextKey.t_834d0bdb63), Icons.Outlined.AccessTime)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            tabs.forEach { item ->
                val isSelected = selected == item.index
                Surface(
                    onClick = { selected = item.index },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Space.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        val studentId = state.activeStudentId
        val list = remember(selected, state.product, refreshTrigger, studentId) {
            state.product.assignments.filter { a ->
                state.product.classes.any { it.id == a.classId && studentId in it.studentIds } &&
                    assignmentStatus(a, state.product, studentId) == selected
            }
        }

        // Empty State matching Image 3
        if (list.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.sm),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.xxl, horizontal = Space.lg),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    AssignmentsEmptyArtwork(Modifier.size(130.dp))

                    Text(
                        text = tr("No assignments here", "لا توجد واجبات هنا"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = tr("Check another tab or wait for teacher assignments.", "راجع تبويبًا آخر أو انتظر واجب المعلم."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(Space.xs))

                    Surface(
                        onClick = { refreshTrigger++ },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.md),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.sm)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = tr("Try again", "إعادة المحاولة"),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
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
}

@Composable
fun AssignmentDetailsScreen(
    id: String,
    state: LearningState,
    onWork: () -> Unit,
    onResult: () -> Unit,
) {
    val a = state.product.assignments.find { it.id == id }
    ScreenPage(Modifier.testTag("screen-assignment-details")) {
        if (a == null) {
            RefEmptyState(
                title = label(TextKey.t_7625c0de23),
                subtitle = label(TextKey.t_bd767ed1d0),
                artwork = { AssignmentsEmptyArtwork() }
            )
            return@ScreenPage
        }

        // Hero Card matching Image 5
        RefHeroCard(
            title = a.title.text(),
            subtitle = a.description.text(),
            badgeText = state.product.classes.find { it.id == a.classId }?.name?.text(),
            badgeColor = RefColors.LightBlueHeroBgStart,
            badgeTextColor = RefColors.PrimaryBlue,
            artwork = { AssignmentsHeroArtwork() }
        )

        // Stats Row Container matching Image 5
        val dueDateFormatted = java.text.SimpleDateFormat("HH:mm dd-MM-yyyy", java.util.Locale.US).format(java.util.Date(a.due))
        val stats = listOf(
            RefStatItem(
                icon = androidx.compose.material.icons.Icons.Outlined.Refresh,
                value = "${a.attempts}",
                label = tr("Attempts", "محاولات")
            ),
            RefStatItem(
                icon = androidx.compose.material.icons.Icons.Outlined.Star,
                value = "${a.points}",
                label = tr("Points", "نقطة")
            ),
            RefStatItem(
                icon = Icons.Outlined.Event,
                value = dueDateFormatted,
                label = tr("Due date", "موعد التسليم")
            )
        )
        RefStatsRow(stats = stats)

        // Notice Callout Banner matching Image 5
        RefInfoBanner(
            text = tr("Read the rules and justify your answer before submitting.", "اقرأ القواعد وبَرّر إجابتك قبل التسليم.")
        )

        val studentId = state.activeStudentId
        val submissions = state.product.submissions.count { it.assignmentId == id && it.studentId == studentId }

        // Action Buttons matching Image 5
        if (submissions > 0) {
            RefPrimaryButton(
                text = label(TextKey.t_934f76a30f),
                onClick = onResult
            )
        }
        if (submissions < a.attempts && (a.late || a.due >= System.currentTimeMillis())) {
            RefPrimaryButton(
                text = label(TextKey.t_6c721d035d),
                onClick = onWork,
                modifier = Modifier.testTag("assignment-work")
            )
        } else if (submissions == 0) {
            RefEmptyState(
                title = label(TextKey.t_fa515c7f77),
                subtitle = label(TextKey.t_3e512eb130)
            )
        }

        // Questions Section Header & List matching Image 5
        SectionTitle(tr("Questions", "الأسئلة"))
        a.questionIds.forEachIndexed { idx, qId ->
            state.product.question(qId)?.let { q ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("assignment-question-$idx"),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RefColors.CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Space.lg),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MathText(q.expression.ifEmpty { q.prompt.text() })
                        }
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = RefColors.LightBlueHeroBgStart,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    ),
                                    color = RefColors.PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentResultScreen(
    id: String,
    state: LearningState,
    studentId: String = state.activeStudentId,
    onFeedback: ((String) -> Unit)? = null,
) {
    val a = state.product.assignments.find { it.id == id }
    val s =
        state.product.submissions.lastOrNull { it.assignmentId == id && it.studentId == studentId }
    ScreenPage(Modifier.testTag("screen-assignment-result")) {
        RefHeroCard(
            title = a?.title?.text().orEmpty().ifEmpty { label(TextKey.t_511d9de5cb) },
            subtitle = tr("Assignment Submission Result", "نتيجة تسليم الواجب"),
            badgeText = tr("Result", "النتيجة"),
            badgeColor = RefColors.LightBlueHeroBgStart,
            badgeTextColor = RefColors.PrimaryBlue,
            artwork = { AssignmentsHeroArtwork() }
        )

        if (a == null || s == null) {
            RefEmptyState(
                title = label(TextKey.t_34ec2073de),
                subtitle = label(TextKey.t_7a0370d97e),
                artwork = { AssignmentsEmptyArtwork() }
            )
            return@ScreenPage
        }

        RefStatsRow(
            stats = listOf(
                RefStatItem(
                    icon = Icons.Outlined.Star,
                    value = "${s.score}/${a.points}",
                    label = label(TextKey.t_9178974d1f)
                ),
                RefStatItem(
                    icon = Icons.Outlined.AccessTime,
                    value = "${s.seconds}s",
                    label = tr("Time", "الوقت")
                ),
                RefStatItem(
                    icon = Icons.Outlined.CheckCircle,
                    value = "${(s.score.toFloat() / a.points * 100).toInt()}%",
                    label = tr("Accuracy", "الدقة")
                )
            )
        )

        if (s.feedback.text().isNotBlank()) {
            RefInfoBanner(
                text = s.feedback.text()
            )
        }
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
