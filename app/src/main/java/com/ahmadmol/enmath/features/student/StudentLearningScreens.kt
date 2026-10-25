package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.ahmadmol.enmath.core.design.isDarkTheme
import com.ahmadmol.enmath.core.data.activeStudentId
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.core.design.components.student.*
import com.ahmadmol.enmath.core.model.Copy
import com.ahmadmol.enmath.core.model.Lesson
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence

@Composable
fun LegacyStudentLearningHome(state: LearningState, open: (String) -> Unit) {
    val next = state.lesson(state.lastLessonId) ?: state.lesson(LimitsLessonSequence.lessonId)
    val assignment =
        state.product.assignments
            .filter { a ->
                state.product.classes.any { it.id == a.classId && state.activeStudentId in it.studentIds } &&
                    state.product.submissions.none {
                        it.assignmentId == a.id && it.studentId == state.activeStudentId
                    } &&
                    a.due >= System.currentTimeMillis()
            }
            .minByOrNull { it.due }
    val progress = state.progress
    ExperiencePage("screen-home") {
        PageHeading(
            tr("A little understanding, every day", "فهم أعمق، كل يوم"),
            tr("Hello, \u2068${state.user?.name.orEmpty()}\u2069", "أهلاً، \u2068${state.user?.name.orEmpty()}\u2069"),
        )
        ExperiencePanel(emphasized = true) {
            Text(tr("Your next idea", "فكرتك التالية"), style = MaterialTheme.typography.labelLarge)
            Text(next?.title?.text().orEmpty(), style = MaterialTheme.typography.headlineMedium)
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
                    )
            )
            PrimaryButton(
                tr("Continue learning", "متابعة التعلم"),
                { open("lesson/${next?.id ?: LimitsLessonSequence.lessonId}") },
                Modifier.testTag("student-continue"),
            )
        }
        ProgressFeedback(
            tr("Today's learning goal", "هدف التعلم اليوم"),
            tr(
                "${progress.todayCompleted} of ${state.dailyGoal} learning activities",
                "${progress.todayCompleted} من ${state.dailyGoal} أنشطة تعلم",
            ),
            progress.todayCompleted.toFloat() / state.dailyGoal.coerceAtLeast(1),
            "daily-learning-goal",
        )
        ExperiencePanel {
            ExperienceSection(
                tr("Make the idea stick", "ثبّت الفكرة"),
                tr(
                    "Six short questions on limits, with feedback after each answer.",
                    "ستة أسئلة قصيرة عن النهايات، مع تفسير بعد كل إجابة.",
                ),
            )
            SecondaryButton(
                tr("Practice limits", "تدرّب على النهايات"),
                { open("practice/${LimitsLessonSequence.practiceId}") },
                Modifier.testTag("home-practice"),
            )
        }
        DetailRow(tr("Need a quick solution?", "تحتاج حلاً سريعاً؟"), tr("Solver", "المحلّل"))
        SecondaryButton(
            tr("Open quick solver", "فتح الحل السريع"),
            { open("solver") },
            Modifier.testTag("home-quick-solver"),
        )
        if (assignment != null) {
            ExperienceSection(tr("Upcoming assignment", "واجبك القادم"))
            AppCard(
                Modifier.testTag("home-upcoming-assignment"),
                onClick = { open("assignments/${assignment.id}") },
            ) {
                Text(assignment.title.text(), style = MaterialTheme.typography.titleMedium)
                Text(assignment.description.text(), style = MaterialTheme.typography.bodyMedium)
            }
        }
        ExperienceSection(tr("Your understanding", "تقدم فهمك"))
        ExperiencePanel {
            val mastery = LearningRules.topic(state, "limits")
            ProgressFeedback(
                tr("Limits mastery", "إتقان النهايات"),
                "${mastery.score}%",
                mastery.score / 100f,
                "student-mastery-limits",
            )
            TextButton({ open("progress") }) { Text(tr("View all progress", "عرض التقدم الكامل")) }
        }
        ExperienceSection(tr("Recent learning", "تعلمك الأخير"))
        if (state.product.activity.isEmpty())
            Text(
                tr(
                    "Your completed lessons and practice will appear here.",
                    "ستظهر هنا الدروس والتدريبات التي تكملها.",
                )
            )
        state.product.activity.takeLast(3).reversed().forEach {
            DetailRow(it.title.text(), tr("${it.minutes} min", "${it.minutes} دقيقة"))
        }
    }
}

@Composable
fun StudentStudy(state: LearningState, open: (String) -> Unit) {
    CurriculumScreen(state = state, open = open)
}

/**
 * High-fidelity Lesson Overview (نظرة عامة على الدرس) implementation for EN.MATH.
 * Pixel-close matching to reference design with Light/Dark mode, RTL support,
 * custom math canvas artwork, interactive lesson entry, and dynamic curriculum progress integration.
 */
@Composable
fun StudentLimitsTopic(
    state: LearningState,
    lesson: (String) -> Unit,
    practice: () -> Unit,
    onBack: () -> Unit = {},
    onNotification: () -> Unit = {},
    onSearch: () -> Unit = {},
    topicId: String = "limits",
) {
    val isDark = isDarkTheme()
    val topic = state.topic(topicId) ?: state.topic("limits")
    val mastery = LearningRules.topic(state, topicId.ifEmpty { "limits" })

    // Theme tokens
    val bgColor = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val borderStrokeColor = MaterialTheme.colorScheme.outlineVariant
    val primaryBlue = MaterialTheme.colorScheme.primary
    val electricBlueDark = Color(0xFF0052CC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("screen-topic")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RefTopBar(tr("Lessons", "الدروس"), onBack, onSearch, onNotification,
                state.product.notifications.count { !it.teacher && it.id !in state.product.readNotifications },
                backTag = "nav-back", searchTag = "header-search", notificationsTag = "header-notifications")
            // 2. Hero Topic Title Section with Limits Math Artwork
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Subtitle Column (RTL: Right side)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tr("Learn · Interact · Understand", "تعلّم · تفاعل · افهم"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = textSecondary
                    )

                    Text(
                        text = topic?.title?.text() ?: tr("Limits", "النهايات"),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = textPrimary
                    )
                }

                // Mathematical Illustration for Limits (RTL: Left side)
                LimitsHeaderArtwork(
                    isDark = isDark,
                    modifier = Modifier
                        .width(170.dp)
                        .height(100.dp)
                )
            }

            // 3. Lesson Progress Card ("تقدّم فهمي")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 0.dp else 2.dp,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderStrokeColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("My understanding progress", "تقدّم فهمي"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = textSecondary
                        )

                        Text(
                            text = "${mastery.score}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = primaryBlue
                        )
                    }

                    // Progress Bar Track & Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (mastery.score / 100f).coerceIn(0f, 1f))
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(electricBlueDark, primaryBlue)
                                    )
                                )
                        )
                    }
                }
            }

            // 4. Interactive Lesson Card ("ماذا تخبرنا النهاية؟")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (isDark) Color(0xFF0D1D33) else Color(0xFFF0F6FF),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A5F) else Color(0xFFD0E2FF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Title & Subtitle with Sunburst/Spark accents
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Spark rays accent above title
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    drawLine(
                                        color = primaryBlue,
                                        start = Offset(size.width * 0.3f, size.height * 0.8f),
                                        end = Offset(size.width * 0.1f, size.height * 0.3f),
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = primaryBlue,
                                        start = Offset(size.width * 0.7f, size.height * 0.8f),
                                        end = Offset(size.width * 0.9f, size.height * 0.3f),
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            Text(
                                text = tr("What is a limit telling us?", "ماذا تخبرنا النهاية؟"),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = textPrimary
                            )

                            Text(
                                text = tr("Move a point, predict a value, then explain your answer.", "حرّك نقطة، توقّع قيمة، ثم فسّر إجابتك."),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp
                                ),
                                color = textSecondary
                            )
                        }

                        // Curve Graphic Canvas inside Interactive Card
                        LimitsCardArtwork(
                            isDark = isDark,
                            modifier = Modifier
                                .width(120.dp)
                                .height(85.dp)
                        )
                    }

                    // Hero Blue Gradient "ابدأ الدرس التفاعلي" Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(electricBlueDark, primaryBlue)
                                )
                            )
                            .clickable { lesson(LimitsLessonSequence.lessonId) }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .testTag("start-interactive-limits"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Start interactive lesson", "ابدأ الدرس التفاعلي"),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            // Circular forward arrow badge inside button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Section Header: "المزيد في هذا الموضوع" with vertical blue bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vertical blue indicator accent line
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(primaryBlue)
                )

                Text(
                    text = tr("More in this topic", "المزيد في هذا الموضوع"),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = textPrimary
                )
            }

            // 6. Lesson Cards List
            val lessonsList = topic?.lessons?.filterNot { it.id == LimitsLessonSequence.lessonId }
                ?: listOf(
                    Lesson(
                        id = "limits-basics",
                        title = Copy("Limits · Basics", "النهايات · الأساسيات"),
                        objectives = Copy("", ""),
                        introduction = Copy("", ""),
                        example = "",
                        explanation = Copy("", ""),
                        rule = Copy("", ""),
                        mistake = Copy("", ""),
                        checkpoint = "",
                        checkpointAnswer = "",
                        minutes = 8
                    ),
                    Lesson(
                        id = "limits-one-sided",
                        title = Copy("Continuity and One-Sided Limits", "الاستمرار والنهايات الجانبية"),
                        objectives = Copy("", ""),
                        introduction = Copy("", ""),
                        example = "",
                        explanation = Copy("", ""),
                        rule = Copy("", ""),
                        mistake = Copy("", ""),
                        checkpoint = "",
                        checkpointAnswer = "",
                        minutes = 15
                    )
                )

            lessonsList.forEach { l ->
                val isCompleted = l.id in state.completed

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 0.dp else 1.dp,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { lesson(l.id) }
                        .testTag("lesson-${l.id}"),
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, borderStrokeColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Right Side Icon: Green Check if completed, Blue Play if in-progress
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCompleted) {
                                        if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)
                                    } else {
                                        if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = if (isCompleted) {
                                    if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
                                } else {
                                    if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Center: Title and Duration
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = l.title.text(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = textPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = tr("${l.minutes} minutes", "${l.minutes} دقائق"),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp
                                    ),
                                    color = textSecondary
                                )
                            }
                        }

                        // Left Side: Completion Badge + Chevron Arrow
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF34D399) else Color(0xFF15803D),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = tr("Completed", "مكتمل"),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = if (isDark) Color(0xFF34D399) else Color(0xFF15803D)
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 7. Practice Button ("تدرّب على الموضوع")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { practice() }
                    .testTag("topic-practice"),
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF0D1D33) else Color(0xFFF0F6FF),
                border = BorderStroke(1.5.dp, primaryBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AdsClick,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = tr("Practice this topic", "تدرّب على الموضوع"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = primaryBlue
                    )
                }
            }
        }
    }
}

/**
 * Top Banner Canvas Illustration for Limits (Axes, Curve, Dashed $L$ level, Open Circle)
 */
@Composable
private fun LimitsHeaderArtwork(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Radial ambient glow behind graph
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (isDark) Color(0xFF1E3A5F).copy(alpha = 0.5f) else Color(0xFFDCE9F9).copy(alpha = 0.85f),
                    Color.Transparent
                ),
                center = Offset(w * 0.45f, h * 0.45f),
                radius = w * 0.55f
            )
        )

        val axisColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
        val strokeWidth = 1.8.dp.toPx()

        // X-Axis
        val xAxisY = h * 0.78f
        drawLine(
            color = axisColor,
            start = Offset(w * 0.08f, xAxisY),
            end = Offset(w * 0.92f, xAxisY),
            strokeWidth = strokeWidth
        )
        // X-Axis Arrowhead
        drawPath(
            Path().apply {
                moveTo(w * 0.92f, xAxisY)
                lineTo(w * 0.85f, xAxisY - 4.dp.toPx())
                lineTo(w * 0.85f, xAxisY + 4.dp.toPx())
                close()
            },
            axisColor
        )

        // Y-Axis
        val yAxisX = w * 0.28f
        drawLine(
            color = axisColor,
            start = Offset(yAxisX, h * 0.92f),
            end = Offset(yAxisX, h * 0.12f),
            strokeWidth = strokeWidth
        )
        // Y-Axis Arrowhead
        drawPath(
            Path().apply {
                moveTo(yAxisX, h * 0.12f)
                lineTo(yAxisX - 4.dp.toPx(), h * 0.20f)
                lineTo(yAxisX + 4.dp.toPx(), h * 0.20f)
                close()
            },
            axisColor
        )

        // Dashed horizontal line to Level L
        val yL = h * 0.32f
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()), 0f)

        drawLine(
            color = axisColor,
            start = Offset(yAxisX, yL),
            end = Offset(w * 0.82f, yL),
            strokeWidth = 1.2.dp.toPx(),
            pathEffect = dashEffect
        )

        // Label 'L'
        val lTextResult = textMeasurer.measure("L", textStyle)
        drawText(
            textLayoutResult = lTextResult,
            topLeft = Offset(yAxisX - lTextResult.size.width - 8.dp.toPx(), yL - lTextResult.size.height / 2f)
        )

        // Curve Line
        val curveColor = Color(0xFF168BFF)
        val endPoint = Offset(w * 0.72f, yL)

        val curvePath = Path().apply {
            moveTo(w * 0.10f, h * 0.76f)
            cubicTo(
                w * 0.25f, h * 0.75f,
                w * 0.45f, h * 0.55f,
                endPoint.x, endPoint.y
            )
        }

        drawPath(
            path = curvePath,
            color = curveColor,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Open Circle at (x0, L)
        val circleRadius = 5.dp.toPx()
        val bgCardColor = if (isDark) Color(0xFF09121F) else Color(0xFFF4F7FC)
        drawCircle(
            color = bgCardColor,
            radius = circleRadius,
            center = endPoint
        )
        drawCircle(
            color = curveColor,
            radius = circleRadius,
            center = endPoint,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Interactive Lesson Card Canvas Illustration for Limits
 */
@Composable
private fun LimitsCardArtwork(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val axisColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
        val strokeWidth = 1.5.dp.toPx()

        // X-Axis
        val xAxisY = h * 0.78f
        drawLine(
            color = axisColor,
            start = Offset(w * 0.10f, xAxisY),
            end = Offset(w * 0.90f, xAxisY),
            strokeWidth = strokeWidth
        )
        drawPath(
            Path().apply {
                moveTo(w * 0.90f, xAxisY)
                lineTo(w * 0.83f, xAxisY - 3.5.dp.toPx())
                lineTo(w * 0.83f, xAxisY + 3.5.dp.toPx())
                close()
            },
            axisColor
        )

        // Y-Axis
        val yAxisX = w * 0.30f
        drawLine(
            color = axisColor,
            start = Offset(yAxisX, h * 0.90f),
            end = Offset(yAxisX, h * 0.12f),
            strokeWidth = strokeWidth
        )
        drawPath(
            Path().apply {
                moveTo(yAxisX, h * 0.12f)
                lineTo(yAxisX - 3.5.dp.toPx(), h * 0.22f)
                lineTo(yAxisX + 3.5.dp.toPx(), h * 0.22f)
                close()
            },
            axisColor
        )

        // Dashed Level L Line
        val yL = h * 0.35f
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()), 0f)

        drawLine(
            color = axisColor,
            start = Offset(yAxisX, yL),
            end = Offset(w * 0.82f, yL),
            strokeWidth = 1f.dp.toPx(),
            pathEffect = dashEffect
        )

        // Label 'L'
        val lTextResult = textMeasurer.measure("L", textStyle)
        drawText(
            textLayoutResult = lTextResult,
            topLeft = Offset(yAxisX - lTextResult.size.width - 6.dp.toPx(), yL - lTextResult.size.height / 2f)
        )

        // Curve Line
        val curveColor = Color(0xFF168BFF)
        val endPoint = Offset(w * 0.72f, yL)

        val curvePath = Path().apply {
            moveTo(w * 0.12f, h * 0.75f)
            cubicTo(
                w * 0.28f, h * 0.74f,
                w * 0.48f, h * 0.55f,
                endPoint.x, endPoint.y
            )
        }

        drawPath(
            path = curvePath,
            color = curveColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Open Circle
        val circleRadius = 4.5.dp.toPx()
        val bgCardColor = if (isDark) Color(0xFF0D1D33) else Color(0xFFF0F6FF)
        drawCircle(
            color = bgCardColor,
            radius = circleRadius,
            center = endPoint
        )
        drawCircle(
            color = curveColor,
            radius = circleRadius,
            center = endPoint,
            style = Stroke(width = 1.8.dp.toPx())
        )
    }
}
