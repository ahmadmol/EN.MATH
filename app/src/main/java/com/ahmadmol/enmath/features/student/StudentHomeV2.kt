package com.ahmadmol.enmath.features.student

import com.ahmadmol.enmath.core.data.activeStudentId
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.DetailRow
import com.ahmadmol.enmath.core.design.components.student.ProgressFeedback
import com.ahmadmol.enmath.core.design.tokens.ApprovedHomeVisuals as V
import com.ahmadmol.enmath.core.design.tokens.ResourceDimensions as D
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence
import com.ahmadmol.enmath.features.localgems.*

/** Node status on the vertical winding learning journey path */
enum class JourneyNodeStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}

/** Definition of a node on the learning journey path */
data class JourneyNodeData(
    val id: String,
    val lessonId: String?,
    val chapterNumber: Int,
    val chapterTitle: Copy,
    val chapterSubtitle: Copy,
    val title: Copy,
    val subtitle: Copy?,
    val symbolText: String,
    val isHeaderNode: Boolean = false,
    val xOffsetDp: Int = 0 // Offset from center to create the winding path effect
)

@Composable
fun StudentLearningHome(
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState()
) {
    if (state.user?.studyStage == StudyStage.G9) {
        Grade9RoadmapHomeScreen(state, open, gems)
    } else {
        BaccalaureateRoadmapHomeScreen(state, open, gems)
    }
}

// =============================================================================
// SUB-COMPONENTS
// =============================================================================

/**
 * Top Header matching reference visual:
 * Deep Navy pill, Greeting with student name, Gems badge and Streak
 */
@Composable
private fun JourneyTopHeader(
    studentName: String,
    gemCountText: String,
    streak: Int,
    dark: Boolean,
    isAr: Boolean,
    onOpenWallet: () -> Unit
) {
    val bgNavy = if (dark) Color(0xFF101732) else Color(0xFFFFFFFF)
    val borderColor = if (dark) Color(0xFF1E2850) else Color(0xFFE0E5F5)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = bgNavy,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (dark) 4.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Student Welcome & Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0084FF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isAr) "أهلاً، \u2068$studentName\u2069" else "Hello, \u2068$studentName\u2069",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDirection = TextDirection.ContentOrRtl
                            ),
                            color = if (dark) Color.White else Color(0xFF15192C)
                        )
                        Text(
                            text = if (isAr) "تابع مسارك في الرياضيات يوماً بعد يوم" else "Follow your math journey day by day",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (dark) Color(0xFF9AA2C3) else Color(0xFF646E8D)
                        )
                    }
                }
            }

            // Row 2: Ledger balance and recorded streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gems Badge
                Surface(
                    onClick = onOpenWallet,
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF162348) else Color(0xFFE8F1FF),
                    border = BorderStroke(1.dp, Color(0xFF0084FF).copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("home-gems-badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Diamond,
                            contentDescription = null,
                            tint = Color(0xFF00A2FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = gemCountText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF00A2FF)
                        )
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF00A2FF)
                        )
                    }
                }

                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF2A1C16) else Color(0xFFFFECE5),
                    border = BorderStroke(1.dp, Color(0xFFFF6B00).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF6B00)
                        )
                    }
                }


            }
        }
    }
}

/**
 * Daily Learning Goal panel matching screenshot:
 * Circular progress indicator (3/5), title, subtitle, and 5 activity type icons
 */
@Composable
private fun JourneyDailyGoalCard(
    todayCompleted: Int,
    dailyGoal: Int,
    dark: Boolean,
    isAr: Boolean
) {
    val bgNavy = if (dark) Color(0xFF101732) else Color(0xFFFFFFFF)
    val borderColor = if (dark) Color(0xFF1E2850) else Color(0xFFE0E5F5)
    val activeGoal = dailyGoal.coerceAtLeast(1)
    val progressFraction = (todayCompleted.toFloat() / activeGoal).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily-learning-goal"),
        shape = RoundedCornerShape(20.dp),
        color = bgNavy,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (dark) 4.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Progress Box
                Box(
                    modifier = Modifier.size(58.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF0084FF),
                        trackColor = if (dark) Color(0xFF1C274A) else Color(0xFFE2E8F8),
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "$todayCompleted/$activeGoal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDirection = TextDirection.Ltr
                        ),
                        color = if (dark) Color.White else Color(0xFF15192C)
                    )
                }

                // Main Goal Texts
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isAr) "هدف اليوم" else "Today's Goal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (dark) Color.White else Color(0xFF15192C)
                    )
                    Text(
                        text = if (isAr)
                            "أكمل $activeGoal أنشطة لمواصلة التقدم"
                        else
                            "Complete $activeGoal activities to stay on track",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dark) Color(0xFF9AA2C3) else Color(0xFF646E8D)
                    )
                }
            }

            // 5 Activity Step Icons (تعلم, تدرب, حل, مراجعة, اختبار)
            val activities = if (isAr) listOf(
                "تعلم" to Icons.AutoMirrored.Outlined.MenuBook,
                "تدرب" to Icons.Outlined.EditNote,
                "حل" to Icons.Outlined.Calculate,
                "مراجعة" to Icons.Outlined.Autorenew,
                "اختبار" to Icons.AutoMirrored.Outlined.Assignment
            ) else listOf(
                "Learn" to Icons.AutoMirrored.Outlined.MenuBook,
                "Train" to Icons.Outlined.EditNote,
                "Solve" to Icons.Outlined.Calculate,
                "Review" to Icons.Outlined.Autorenew,
                "Exam" to Icons.AutoMirrored.Outlined.Assignment
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activities.forEachIndexed { index, (label, icon) ->
                    val isDone = index < todayCompleted
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDone) Color(0xFF0084FF) else if (dark) Color(0xFF182245) else Color(0xFFEBF0FA),
                            border = BorderStroke(
                                1.dp,
                                if (isDone) Color(0xFF00E5FF) else if (dark) Color(0xFF283660) else Color(0xFFCFD8EE)
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (dark) Color(0xFF65719A) else Color(0xFF8A95B6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isDone) Color(0xFF00A2FF) else if (dark) Color(0xFF7A85AC) else Color(0xFF6B7699)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Container that renders the vertical winding path with connecting Bezier curves and nodes
 */
@Composable
private fun JourneyVerticalPathContainer(
    nodes: List<JourneyNodeData>,
    completedSet: Set<String>,
    activeLessonId: String,
    dark: Boolean,
    isAr: Boolean,
    open: (String) -> Unit
) {
    var lastChapter = 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        nodes.forEachIndexed { index, node ->
            val status = when {
                node.lessonId in completedSet -> JourneyNodeStatus.COMPLETED
                node.lessonId == activeLessonId || (index == 2 && completedSet.size >= 2) -> JourneyNodeStatus.CURRENT
                else -> JourneyNodeStatus.LOCKED
            }

            // Insert Chapter Section Header when a new chapter starts
            if (node.isHeaderNode && node.chapterNumber != lastChapter) {
                lastChapter = node.chapterNumber
                JourneySectionHeader(
                    chapterNumber = node.chapterNumber,
                    title = node.chapterTitle.text(),
                    subtitle = node.chapterSubtitle.text(),
                    dark = dark
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Render Node Item with connecting curve to next node
            val nextNode = nodes.getOrNull(index + 1)
            JourneyNodeRow(
                node = node,
                nextNode = nextNode,
                status = status,
                dark = dark,
                isAr = isAr,
                open = open
            )
        }
    }
}

/**
 * Section Header for each chapter along the path
 */
@Composable
private fun JourneySectionHeader(
    chapterNumber: Int,
    title: String,
    subtitle: String,
    dark: Boolean
) {
    val bgNavy = if (dark) Color(0xFF121B3B) else Color(0xFFFFFFFF)
    val borderColor = if (dark) Color(0xFF1E2D5E) else Color(0xFFD4E0FC)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = bgNavy,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (dark) Color.White else Color(0xFF15192C)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (dark) Color(0xFF9AA2C3) else Color(0xFF646E8D)
                )
            }
            Surface(
                shape = CircleShape,
                color = if (dark) Color(0xFF1A2652) else Color(0xFFEEF3FE),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF0084FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renders a single node on the path with an S-curve connecting line to the next node
 */
@Composable
private fun JourneyNodeRow(
    node: JourneyNodeData,
    nextNode: JourneyNodeData?,
    status: JourneyNodeStatus,
    dark: Boolean,
    isAr: Boolean,
    open: (String) -> Unit
) {
    val xOffsetDp = node.xOffsetDp.dp
    val nextXOffsetDp = nextNode?.xOffsetDp?.dp ?: 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Draw Connecting Curved Bezier Line behind the nodes
        if (nextNode != null) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val startX = (size.width / 2) + xOffsetDp.toPx()
                val startY = 32.dp.toPx()
                val endX = (size.width / 2) + nextXOffsetDp.toPx()
                val endY = size.height + 10.dp.toPx()

                val controlY1 = startY + (endY - startY) * 0.5f
                val controlY2 = startY + (endY - startY) * 0.5f

                val path = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(startX, controlY1, endX, controlY2, endX, endY)
                }

                val isActiveSegment = status == JourneyNodeStatus.COMPLETED || status == JourneyNodeStatus.CURRENT

                drawPath(
                    path = path,
                    color = if (isActiveSegment) Color(0xFF0084FF) else if (dark) Color(0xFF232D52) else Color(0xFFCFD8F0),
                    style = Stroke(
                        width = if (isActiveSegment) 5.dp.toPx() else 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = if (!isActiveSegment) PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f) else null
                    )
                )
            }
        }

        // Render the Node
        Box(
            modifier = Modifier
                .offset(x = xOffsetDp, y = 0.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                when (status) {
                    JourneyNodeStatus.COMPLETED -> CompletedJourneyNode(
                        node = node,
                        dark = dark,
                        onClick = { node.lessonId?.let { open("lesson/$it") } }
                    )

                    JourneyNodeStatus.CURRENT -> CurrentJourneyNode(
                        node = node,
                        dark = dark,
                        isAr = isAr,
                        onContinue = { node.lessonId?.let { open("lesson/$it") } }
                    )

                    JourneyNodeStatus.LOCKED -> LockedJourneyNode(
                        node = node,
                        dark = dark
                    )


                }
            }
        }
    }
}

/**
 * Completed Node: Blue circle with checkmark badge & side title label
 */
@Composable
private fun CompletedJourneyNode(
    node: JourneyNodeData,
    dark: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Side Title Label
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (dark) Color(0xFF131A35) else Color(0xFFEAF1FF),
            border = BorderStroke(1.dp, Color(0xFF0084FF).copy(alpha = 0.4f))
        ) {
            Text(
                text = node.title.text(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (dark) Color.White else Color(0xFF15192C),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // Node Circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, CircleShape, ambientColor = Color(0xFF0084FF), spotColor = Color(0xFF0084FF))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00A2FF), Color(0xFF0066FF))
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = node.symbolText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            // Top-right checkmark badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp)
                    .background(Color(0xFF00E5FF), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF004080),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Current Node: Glowing cyan/blue node with sparkling animation & attached Current Lesson Card
 */
@Composable
private fun CurrentJourneyNode(
    node: JourneyNodeData,
    dark: Boolean,
    isAr: Boolean,
    onContinue: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Glowing Current Node Circle
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Outer Glowing Ring
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(glowScale)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.25f), CircleShape)
            )

            // Main Circle Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(10.dp, CircleShape, ambientColor = Color(0xFF00A2FF), spotColor = Color(0xFF00E5FF))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                        ),
                        shape = CircleShape
                    )
                    .border(2.5.dp, Color(0xFF00E5FF), CircleShape)
                    .clip(CircleShape)
                    .clickable { onContinue() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = node.symbolText,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        // Current Lesson Card Attached on the Side
        Surface(
            onClick = onContinue,
            modifier = Modifier.testTag("student-continue"),
            shape = RoundedCornerShape(18.dp),
            color = if (dark) Color(0xFF101C3D) else Color(0xFFEAF2FF),
            border = BorderStroke(1.5.dp, Color(0xFF0084FF)),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isAr) "الدرس الحالي" else "Current Lesson",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF00A2FF)
                    )
                    Text(
                        text = node.title.text(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (dark) Color.White else Color(0xFF10162B)
                    )
                    Text(
                        text = node.subtitle?.text() ?: if (isAr) "شرح · أمثلة · تدريبات" else "Explanation · Examples · Exercises",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dark) Color(0xFF9AA5CC) else Color(0xFF5A668A)
                    )
                }

                // Action Arrow Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0084FF),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Locked Node: Dark grey circle with lock icon
 */
@Composable
private fun LockedJourneyNode(
    node: JourneyNodeData,
    dark: Boolean
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(if (dark) Color(0xFF1A213B) else Color(0xFFE2E7F5), CircleShape)
            .border(1.5.dp, if (dark) Color(0xFF2D385E) else Color(0xFFC0CADF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = node.symbolText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (dark) Color(0xFF58648E) else Color(0xFF7A86AC)
        )

        // Lock Badge Top-Right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 2.dp, y = (-2).dp)
                .size(18.dp)
                .background(if (dark) Color(0xFF2A3459) else Color(0xFFC8D2E8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (dark) Color(0xFF8B98C7) else Color(0xFF5A668C),
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

// =============================================================================
// EXISTING REUSED COMPONENT PROXIES (DiscoverGemsCard, SuperPackageCard, ApprovedPanel, ApprovedAction)
// =============================================================================

@Composable
fun DiscoverGemsCard(
    language: Language,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isAr = language == Language.Arabic

    Surface(
        onClick = onOpenWallet,
        modifier = modifier
            .fillMaxWidth()
            .testTag("discover-gems-banner"),
        shape = RoundedCornerShape(18.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.3f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = scheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Diamond,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAr) "اكتشف نظام الجواهر" else "Discover Gems System",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = scheme.onSurface
                    )
                    Text(
                        text = if (isAr) "مكافأتك الذهبية على التعلم والمثابرة" else "Your reward for learning & dedication",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                text = if (isAr) "كيف تكسب الجواهر؟" else "How to Earn Gems?",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = scheme.primary
            )

            val earnItems = if (isAr) listOf(
                "حل التمارين والمسائل الرياضية بثقة",
                "إكمال الأنشطة والتدريبات التعليمية المؤهلة",
                "التفاعل المباشر مع الدروس والاختبارات",
                "تحقيق تقدم تعليمي حقيقي داخل التطبيق"
            ) else listOf(
                "Solving math exercises and problems confidently",
                "Completing eligible learning activities & practice",
                "Interacting directly with lessons and quizzes",
                "Achieving genuine educational progress in the app"
            )

            earnItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SuperPackageCard(
    language: Language,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isAr = language == Language.Arabic

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("super-package-teaser"),
        shape = RoundedCornerShape(18.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, scheme.tertiary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scheme.tertiaryContainer,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = scheme.tertiary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "EN.MATH Super",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = scheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = scheme.tertiary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, scheme.tertiary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (isAr) "قريباً" else "Coming Soon",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = scheme.tertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isAr) "تجربة تعلم متطورة ومحتوى حصري متكامل" else "Advanced learning experience & exclusive content",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(D.radius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(D.cardInset),
            verticalArrangement = Arrangement.spacedBy(D.gutter),
            content = content,
        )
    }
}

@Composable
private fun ApprovedAction(
    title: String,
    icon: ImageVector,
    background: Color,
    foreground: Color,
    modifier: Modifier,
    action: () -> Unit,
) {
    Surface(
        onClick = action,
        modifier = modifier.heightIn(min = D.touch),
        shape = RoundedCornerShape(D.buttonRadius),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = RoundedCornerShape(D.buttonRadius), color = background) {
                Icon(icon, null, Modifier.padding(D.gutter).size(D.icon), tint = foreground)
            }
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
