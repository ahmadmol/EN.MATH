package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.DetailRow
import com.ahmadmol.enmath.core.design.components.student.ProgressFeedback
import com.ahmadmol.enmath.core.design.tokens.ApprovedHomeVisuals as V
import com.ahmadmol.enmath.core.design.tokens.ResourceDimensions as D
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence

/** Approved L06/D06 presentation; existing Home data and destinations are unchanged. */
@Composable
fun StudentLearningHome(state: LearningState, open: (String) -> Unit) {
    val next = state.lesson(state.lastLessonId) ?: state.lesson(LimitsLessonSequence.lessonId)
    val assignment =
        state.product.assignments
            .filter { a ->
                state.product.classes.any { it.id == a.classId && "ahmed" in it.studentIds } &&
                    state.product.submissions.none {
                        it.assignmentId == a.id && it.studentId == "ahmed"
                    } &&
                    a.due >= System.currentTimeMillis()
            }
            .minByOrNull { it.due }
    val progress = state.progress
    val mastery = LearningRules.topic(state, "limits")
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < .5f
    BoxWithConstraints(
        Modifier.fillMaxSize()
            .testTag("screen-home")
            .background(
                Brush.verticalGradient(
                    listOf(if (dark) V.darkHeader else V.lightHeader, scheme.background)
                )
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = D.contentMax)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (maxWidth < 600.dp) D.phoneInset else D.tabletInset,
                    vertical = D.cardInset,
                ),
            verticalArrangement = Arrangement.spacedBy(D.gutter),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(D.gutter),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        tr(
                            "Hello, ${state.user?.name.orEmpty()}",
                            "أهلاً، ${state.user?.name.orEmpty()}",
                        ),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        tr("A little understanding, every day", "فهم أعمق، كل يوم"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Surface(shape = CircleShape, color = scheme.primaryContainer) {
                    Icon(
                        Icons.Outlined.PersonOutline,
                        null,
                        Modifier.padding(D.gutter).size(D.icon),
                        tint = scheme.onPrimaryContainer,
                    )
                }
            }
            ApprovedPanel(Modifier.testTag("daily-learning-goal")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(D.gutter),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            tr("Today's learning goal", "هدف التعلم اليوم"),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            tr(
                                "${progress.todayCompleted} of ${state.dailyGoal} learning activities",
                                "${progress.todayCompleted} من ${state.dailyGoal} أنشطة تعلم",
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    Box(Modifier.size(V.compactGoal), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = {
                                (progress.todayCompleted.toFloat() /
                                        state.dailyGoal.coerceAtLeast(1))
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxSize(),
                            color = scheme.secondary,
                            trackColor = scheme.surfaceVariant,
                        )
                        Text(
                            "${progress.todayCompleted}/${state.dailyGoal}",
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    textDirection = TextDirection.Ltr
                                ),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = {
                        (progress.todayCompleted.toFloat() / state.dailyGoal.coerceAtLeast(1))
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = scheme.secondary,
                    trackColor = scheme.surfaceVariant,
                )
            }
            Surface(
                onClick = { open("lesson/${next?.id ?: LimitsLessonSequence.lessonId}") },
                modifier =
                    Modifier.fillMaxWidth()
                        .heightIn(min = D.prominentButton)
                        .testTag("student-continue"),
                shape = RoundedCornerShape(D.radius),
                color = androidx.compose.ui.graphics.Color.Transparent,
            ) {
                Row(
                    Modifier.background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (dark) V.darkContinueStart else V.lightContinueStart,
                                    if (dark) V.darkContinueEnd else V.lightContinueEnd,
                                )
                            )
                        )
                        .padding(D.cardInset),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(D.gutter),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            tr("Continue learning", "متابعة التعلم"),
                            style = MaterialTheme.typography.titleMedium,
                            color = V.continueText,
                        )
                        Text(
                            next?.title?.text().orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = V.continueText,
                        )
                    }
                    Surface(shape = CircleShape, color = V.continueText) {
                        Icon(
                            Icons.Outlined.PlayArrow,
                            null,
                            Modifier.padding(D.gutter).size(D.icon),
                            tint = V.continueIcon,
                        )
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(D.gutter),
            ) {
                ApprovedAction(
                    tr("Practice limits", "تدرّب على النهايات"),
                    Icons.Outlined.EditNote,
                    scheme.secondaryContainer,
                    scheme.onSecondaryContainer,
                    Modifier.weight(1f).fillMaxHeight().testTag("home-practice"),
                ) {
                    open("practice/${LimitsLessonSequence.practiceId}")
                }
                ApprovedAction(
                    tr("Open quick solver", "فتح الحل السريع"),
                    Icons.Outlined.Calculate,
                    scheme.tertiaryContainer,
                    scheme.onTertiaryContainer,
                    Modifier.weight(1f).fillMaxHeight().testTag("home-quick-solver"),
                ) {
                    open("solver")
                }
                ApprovedAction(
                    tr("View all progress", "عرض التقدم الكامل"),
                    Icons.Outlined.Insights,
                    scheme.primaryContainer,
                    scheme.onPrimaryContainer,
                    Modifier.weight(1f).fillMaxHeight().testTag("home-all-progress"),
                ) {
                    open("progress")
                }
            }
            Text(
                tr("Your understanding", "تقدم فهمك"),
                Modifier.padding(top = D.gutter),
                style = MaterialTheme.typography.titleMedium,
            )
            ApprovedPanel {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(D.gutter),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.MenuBook,
                        null,
                        Modifier.size(D.icon),
                        tint = scheme.primary,
                    )
                    Column(Modifier.weight(1f)) {
                        ProgressFeedback(
                            tr("Limits mastery", "إتقان النهايات"),
                            "${mastery.score}%",
                            mastery.score / 100f,
                            "student-mastery-limits",
                        )
                    }
                }
            }
            if (assignment != null) {
                Text(
                    tr("Upcoming assignment", "واجبك القادم"),
                    Modifier.padding(top = D.gutter),
                    style = MaterialTheme.typography.titleMedium,
                )
                Surface(
                    onClick = { open("assignments/${assignment.id}") },
                    modifier =
                        Modifier.fillMaxWidth()
                            .heightIn(min = D.touch)
                            .testTag("home-upcoming-assignment"),
                    shape = RoundedCornerShape(D.radius),
                    color = scheme.surface,
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                ) {
                    Column(
                        Modifier.padding(D.cardInset),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(assignment.title.text(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            assignment.description.text(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                        Text(
                            tr("View assignment", "عرض الواجب"),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.primary,
                        )
                    }
                }
            }
            Text(
                tr("Recent learning", "تعلمك الأخير"),
                Modifier.padding(top = D.gutter),
                style = MaterialTheme.typography.titleMedium,
            )
            ApprovedPanel {
                if (state.product.activity.isEmpty())
                    Text(
                        tr(
                            "Your completed lessons and practice will appear here.",
                            "ستظهر هنا الدروس والتدريبات التي تكملها.",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                state.product.activity.takeLast(3).reversed().forEachIndexed { index, activity ->
                    if (index > 0) HorizontalDivider(color = scheme.outlineVariant)
                    DetailRow(
                        activity.title.text(),
                        tr("${activity.minutes} min", "${activity.minutes} دقيقة"),
                    )
                }
            }
        }
    }
}

@Composable
private fun ApprovedPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(D.radius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier.padding(D.cardInset),
            verticalArrangement = Arrangement.spacedBy(D.gutter),
            content = content,
        )
    }
}

@Composable
private fun ApprovedAction(
    title: String,
    icon: ImageVector,
    background: androidx.compose.ui.graphics.Color,
    foreground: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    action: () -> Unit,
) {
    Surface(
        onClick = action,
        modifier = modifier.heightIn(min = D.touch),
        shape = RoundedCornerShape(D.buttonRadius),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Column(
            Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = RoundedCornerShape(D.buttonRadius), color = background) {
                Icon(icon, null, Modifier.padding(D.gutter).size(D.icon), tint = foreground)
            }
            Text(
                title,
                Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
