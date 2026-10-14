package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.DetailRow
import com.ahmadmol.enmath.core.design.components.student.ProgressFeedback
import com.ahmadmol.enmath.core.design.tokens.ResourceDimensions as D
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence

/** S01 presentation only. Route actions and data selection match the retained legacy Home. */
@Composable
fun StudentHomeBeforeApproved(state: LearningState, open: (String) -> Unit) {
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
    BoxWithConstraints(
        Modifier.fillMaxSize().testTag("screen-home"),
        contentAlignment = Alignment.TopCenter,
    ) {
        val inset = if (maxWidth < 600.dp) D.phoneInset else D.tabletInset
        Column(
            Modifier.widthIn(max = D.contentMax)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = inset, vertical = D.cardInset),
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
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        tr("A little understanding, every day", "فهم أعمق، كل يوم"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(D.heroRadius),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        Icons.Outlined.PersonOutline,
                        null,
                        Modifier.padding(D.gutter).size(D.icon),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            HomePanel(tinted = true, modifier = Modifier.testTag("home-next-lesson")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.MenuBook, null, Modifier.size(D.icon))
                    Text(
                        tr("Your next idea", "فكرتك التالية"),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Text(next?.title?.text().orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(
                    if (next?.id == LimitsLessonSequence.lessonId)
                        tr(
                            "Explore a limit, make a prediction, then test your reasoning.",
                            "استكشف نهاية، توقّع النتيجة، ثم اختبر فهمك.",
                        )
                    else
                        tr(
                            "Continue your last lesson and apply its rule.",
                            "تابع آخر درس وطبّق قاعدته.",
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    { open("lesson/${next?.id ?: LimitsLessonSequence.lessonId}") },
                    Modifier.fillMaxWidth()
                        .heightIn(min = D.prominentButton)
                        .testTag("student-continue"),
                    shape = RoundedCornerShape(D.buttonRadius),
                    contentPadding = PaddingValues(D.cardInset),
                ) {
                    Text(
                        tr("Continue learning", "متابعة التعلم"),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(D.icon))
                }
            }
            HomePanel(modifier = Modifier.testTag("daily-learning-goal")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(D.cardInset),
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box(Modifier.size(D.goalRing), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = {
                                (progress.todayCompleted.toFloat() /
                                        state.dailyGoal.coerceAtLeast(1))
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(
                            "${progress.todayCompleted}/${state.dailyGoal}",
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    textDirection = TextDirection.Ltr
                                ),
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(D.gutter)) {
                HomeAction(
                    tr("Practice limits", "تدرّب على النهايات"),
                    tr("Six questions with feedback", "ستة أسئلة مع تفسير"),
                    Icons.Outlined.EditNote,
                    Modifier.weight(1f).fillMaxHeight().testTag("home-practice"),
                ) {
                    open("practice/${LimitsLessonSequence.practiceId}")
                }
                HomeAction(
                    tr("Open quick solver", "فتح الحل السريع"),
                    tr("Need a quick solution?", "تحتاج حلاً سريعاً؟"),
                    Icons.Outlined.Calculate,
                    Modifier.weight(1f).fillMaxHeight().testTag("home-quick-solver"),
                ) {
                    open("solver")
                }
            }
            if (assignment != null) {
                HomeSection(tr("Upcoming assignment", "واجبك القادم"), Icons.Outlined.EventNote)
                Surface(
                    onClick = { open("assignments/${assignment.id}") },
                    modifier =
                        Modifier.fillMaxWidth()
                            .heightIn(min = D.touch)
                            .testTag("home-upcoming-assignment"),
                    shape = RoundedCornerShape(D.radius),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        Modifier.padding(D.cardInset),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(assignment.title.text(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            assignment.description.text(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                tr("View assignment", "عرض الواجب"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowForward,
                                null,
                                Modifier.size(D.icon),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            HomeSection(tr("Your understanding", "تقدم فهمك"), Icons.Outlined.Insights)
            HomePanel {
                ProgressFeedback(
                    tr("Limits mastery", "إتقان النهايات"),
                    "${mastery.score}%",
                    mastery.score / 100f,
                    "student-mastery-limits",
                )
                TextButton(
                    { open("progress") },
                    Modifier.heightIn(min = D.touch).testTag("home-all-progress"),
                ) {
                    Text(tr("View all progress", "عرض التقدم الكامل"))
                }
            }
            HomeSection(tr("Recent learning", "تعلمك الأخير"), Icons.Outlined.History)
            HomePanel {
                if (state.product.activity.isEmpty())
                    Text(
                        tr(
                            "Your completed lessons and practice will appear here.",
                            "ستظهر هنا الدروس والتدريبات التي تكملها.",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                state.product.activity.takeLast(3).reversed().forEachIndexed { index, activity ->
                    if (index > 0)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
private fun HomePanel(
    modifier: Modifier = Modifier,
    tinted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (tinted) D.heroRadius else D.radius),
        color = if (tinted) scheme.primaryContainer else scheme.surface,
        contentColor = if (tinted) scheme.onPrimaryContainer else scheme.onSurface,
    ) {
        Column(
            Modifier.padding(D.cardInset),
            verticalArrangement = Arrangement.spacedBy(D.gutter),
            content = content,
        )
    }
}

@Composable
private fun HomeAction(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier,
    action: () -> Unit,
) {
    Surface(
        onClick = action,
        modifier = modifier.heightIn(min = D.touch),
        shape = RoundedCornerShape(D.radius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.padding(D.cardInset), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, Modifier.size(D.icon), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeSection(title: String, icon: ImageVector) {
    Row(
        Modifier.padding(top = D.gutter),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, Modifier.size(D.icon), tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}
