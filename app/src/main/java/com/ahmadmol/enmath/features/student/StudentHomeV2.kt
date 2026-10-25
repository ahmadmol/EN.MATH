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
    LOCKED,
    AVAILABLE_REWARD,
    CLAIMED_REWARD
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
    val isRewardNode: Boolean = false,
    val xOffsetDp: Int = 0 // Offset from center to create the winding path effect
)

/**
 * Duolingo-inspired Student Learning Journey Home Screen for EN.MATH.
 * Displays Syrian Scientific Baccalaureate Mathematics curriculum in an interactive
 * vertical winding path with gamification, gems integration, daily goals, and RTL support.
 */
@Composable
fun StudentLearningHome(
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState()
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val isAr = state.language == Language.Arabic

    val studentName = state.user?.name?.takeIf { it.isNotBlank() } ?: if (isAr) "الطالب" else "Student"
    val owner = LocalGemsPolicy.owner(state.user)
    val gemCountText = gemBalanceText(gems, owner, state.language)
    val stats = LearningRules.stats(state)
    val progress = state.progress

    // Find current active lesson
    val currentLesson = state.lesson(state.lastLessonId)
        ?: state.lesson(LimitsLessonSequence.lessonId)
        ?: state.lessons.firstOrNull()

    val assignment = state.product.assignments
        .filter { a ->
            state.product.classes.any { it.id == a.classId && state.activeStudentId in it.studentIds } &&
                    state.product.submissions.none { it.assignmentId == a.id && it.studentId == state.activeStudentId } &&
                    a.due >= System.currentTimeMillis()
        }
        .minByOrNull { it.due }

    val mastery = LearningRules.topic(state, "limits")

    // State for claimed reward chests
    var claimedRewards by remember { mutableStateOf(setOf<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Build curriculum journey nodes for Syrian Scientific Baccalaureate
    val journeyNodes = remember {
        listOf(
            // Chapter 1: Limits
            JourneyNodeData(
                id = "node-1",
                lessonId = "limits-foundations",
                chapterNumber = 1,
                chapterTitle = Copy("Chapter 1: Limits", "القسم 1: النهايات"),
                chapterSubtitle = Copy("Limits concept and calculation", "مفهوم النهاية وحساب النهايات"),
                title = Copy("Introduction to Limits", "مقدمة في النهايات"),
                subtitle = Copy("Explanation · Examples", "شرح · أمثلة"),
                symbolText = "∿",
                isHeaderNode = true,
                xOffsetDp = -55
            ),
            JourneyNodeData(
                id = "node-2",
                lessonId = "limits-practice",
                chapterNumber = 1,
                chapterTitle = Copy("Chapter 1: Limits", "القسم 1: النهايات"),
                chapterSubtitle = Copy("Limits concept and calculation", "مفهوم النهاية وحساب النهايات"),
                title = Copy("Algebraic Limits Calculation", "حساب النهايات الجبرية"),
                subtitle = Copy("Guided Practice", "تدريب موجّه"),
                symbolText = "lim",
                xOffsetDp = -15
            ),
            JourneyNodeData(
                id = "node-3",
                lessonId = "limits-applications",
                chapterNumber = 1,
                chapterTitle = Copy("Chapter 1: Limits", "القسم 1: النهايات"),
                chapterSubtitle = Copy("Limits concept and calculation", "مفهوم النهاية وحساب النهايات"),
                title = Copy("Infinite Limits & Continuity", "النهايات غير المنتهية والاستمرار"),
                subtitle = Copy("Explanation · Examples · Practice", "شرح · أمثلة · تدريبات"),
                symbolText = "f(x)",
                xOffsetDp = 30
            ),

            // Section Reward Chest
            JourneyNodeData(
                id = "reward-1",
                lessonId = null,
                chapterNumber = 1,
                chapterTitle = Copy("Gems Reward", "مكافأة الجواهر"),
                chapterSubtitle = Copy("Complete section to claim", "أكمل هذا القسم للحصول على الجواهر"),
                title = Copy("Gems Chest Reward", "مكافأة الجواهر"),
                subtitle = Copy("Complete this chapter", "أكمل هذا القسم"),
                symbolText = "🎁",
                isRewardNode = true,
                xOffsetDp = 65
            ),

            // Chapter 2: Derivatives
            JourneyNodeData(
                id = "node-4",
                lessonId = "derivatives-foundations",
                chapterNumber = 2,
                chapterTitle = Copy("Chapter 2: Derivatives", "القسم 2: الاشتقاق"),
                chapterSubtitle = Copy("Derivative concept & applications", "مفهوم المشتقة وتطبيقاتها"),
                title = Copy("Derivative & Tangent Concept", "مفهوم المشتقة والمماس"),
                subtitle = Copy("Foundations & Rules", "الأساسيات والقواعد"),
                symbolText = "f′",
                isHeaderNode = true,
                xOffsetDp = 20
            ),
            JourneyNodeData(
                id = "node-5",
                lessonId = "derivatives-practice",
                chapterNumber = 2,
                chapterTitle = Copy("Chapter 2: Derivatives", "القسم 2: الاشتقاق"),
                chapterSubtitle = Copy("Derivative concept & applications", "مفهوم المشتقة وتطبيقاتها"),
                title = Copy("Algebraic Derivative Rules", "قواعد الاشتقاق الجبرية"),
                subtitle = Copy("Practice & Problems", "تدريبات ومسائل"),
                symbolText = "dy/dx",
                xOffsetDp = -25
            ),
            JourneyNodeData(
                id = "node-6",
                lessonId = "derivatives-applications",
                chapterNumber = 2,
                chapterTitle = Copy("Chapter 2: Derivatives", "القسم 2: الاشتقاق"),
                chapterSubtitle = Copy("Derivative concept & applications", "مفهوم المشتقة وتطبيقاتها"),
                title = Copy("Derivative Applications & Variations", "تطبيقات المشتقة ودراسة التغيرات"),
                subtitle = Copy("Advanced Practice", "تدريبات متقدمة"),
                symbolText = "🧮",
                xOffsetDp = -60
            ),

            // Chapter 3: Integrals
            JourneyNodeData(
                id = "node-7",
                lessonId = "integrals-foundations",
                chapterNumber = 3,
                chapterTitle = Copy("Chapter 3: Integrals", "القسم 3: التكامل"),
                chapterSubtitle = Copy("Integrals & applications", "التكامل وتطبيقاته"),
                title = Copy("Antiderivatives & Integral Concept", "مفهوم الدالة الأصلية والتكامل"),
                subtitle = Copy("Foundations", "الأساسيات"),
                symbolText = "∫",
                isHeaderNode = true,
                xOffsetDp = -15
            ),
            JourneyNodeData(
                id = "node-8",
                lessonId = "integrals-practice",
                chapterNumber = 3,
                chapterTitle = Copy("Chapter 3: Integrals", "القسم 3: التكامل"),
                chapterSubtitle = Copy("Integrals & applications", "التكامل وتطبيقاته"),
                title = Copy("Integration Methods & Substitution", "طرق التكامل والتعويض"),
                subtitle = Copy("Guided Exercises", "تدريبات موجّهة"),
                symbolText = "∫dx",
                xOffsetDp = 35
            ),

            // Chapter 4: Algebra & Sequences
            JourneyNodeData(
                id = "node-9",
                lessonId = "sequences-foundations",
                chapterNumber = 4,
                chapterTitle = Copy("Chapter 4: Algebra", "القسم 4: الجبر"),
                chapterSubtitle = Copy("Sequences & Complex Numbers", "المتتاليات والأعداد المركبة"),
                title = Copy("Arithmetic & Geometric Sequences", "المتتاليات الحسابية والهندسية"),
                subtitle = Copy("Rules & Examples", "القواعد والأمثلة"),
                symbolText = "uₙ",
                isHeaderNode = true,
                xOffsetDp = 60
            ),
            JourneyNodeData(
                id = "node-10",
                lessonId = "matrices-foundations",
                chapterNumber = 4,
                chapterTitle = Copy("Chapter 4: Algebra", "القسم 4: الجبر"),
                chapterSubtitle = Copy("Sequences & Complex Numbers", "المتتاليات والأعداد المركبة"),
                title = Copy("Matrices & Linear Algebra", "المصفوفات والجبر الخطي"),
                subtitle = Copy("Operations", "العمليات الأساسية"),
                symbolText = "[A]",
                xOffsetDp = 15
            ),

            // Chapter 5: Probability
            JourneyNodeData(
                id = "node-11",
                lessonId = "probability-foundations",
                chapterNumber = 5,
                chapterTitle = Copy("Chapter 5: Probability", "القسم 5: الاحتمالات"),
                chapterSubtitle = Copy("Probability & Distributions", "الاحتمالات والتوزيعات"),
                title = Copy("Conditional Probability & Events", "الاحتمال الشرطي والأحداث"),
                subtitle = Copy("Foundations & Laws", "الأساسيات والقوانين"),
                symbolText = "🎲",
                isHeaderNode = true,
                xOffsetDp = -40
            )
        )
    }

    // Determine status of each node dynamically based on user completion state
    val completedSet = state.completed
    val activeLessonId = currentLesson?.id ?: "limits-foundations"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-home")
            .background(
                Brush.verticalGradient(
                    if (dark) listOf(Color(0xFF090D1E), Color(0xFF0E142B), Color(0xFF0B0F19))
                    else listOf(Color(0xFFEBF2FF), Color(0xFFF4F7FE), Color(0xFFF8F9FE))
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .widthIn(max = D.contentMax)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(
                        horizontal = if (maxWidth < 600.dp) 16.dp else D.tabletInset,
                        vertical = 12.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // -------------------------------------------------------------
                // 1. TOP HEADER
                // -------------------------------------------------------------
                JourneyTopHeader(
                    studentName = studentName,
                    gemCountText = gemCountText,
                    streak = stats.streak,
                    rankText = if (isAr) "المرتبة 25" else "Rank 25",
                    dark = dark,
                    isAr = isAr,
                    onOpenWallet = { open("gems/wallet") }
                )

                // -------------------------------------------------------------
                // 2. DAILY LEARNING GOAL CARD
                // -------------------------------------------------------------
                JourneyDailyGoalCard(
                    todayCompleted = progress.todayCompleted,
                    dailyGoal = state.dailyGoal,
                    dark = dark,
                    isAr = isAr
                )

                // -------------------------------------------------------------
                // 3. VERTICAL WINDING LEARNING PATH
                // -------------------------------------------------------------
                Text(
                    text = if (isAr) "مسار تعلم رياضيات البكالوريا" else "Baccalaureate Math Learning Path",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (dark) Color.White else Color(0xFF1E2438),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )

                JourneyVerticalPathContainer(
                    nodes = journeyNodes,
                    completedSet = completedSet,
                    activeLessonId = activeLessonId,
                    claimedRewards = claimedRewards,
                    dark = dark,
                    isAr = isAr,
                    open = open,
                    // No approved durable grant exists for this chest. Keep it unclaimed
                    // and open the existing wallet/status instead of reporting a fake success.
                    onClaimReward = { open("gems/wallet") }
                )

                // -------------------------------------------------------------
                // 4. QUICK ACTIONS & SHORTCUTS (Testing Compatibility)
                // -------------------------------------------------------------
                Text(
                    text = if (isAr) "أدوات وتدريبات سريعة" else "Quick Tools & Practice",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (dark) Color.White else Color(0xFF1E2438),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(D.gutter)
                ) {
                    ApprovedAction(
                        title = tr("Practice limits", "تدرّب على النهايات"),
                        icon = Icons.Outlined.EditNote,
                        background = scheme.secondaryContainer,
                        foreground = scheme.onSecondaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("home-practice")
                    ) {
                        open("practice/${LimitsLessonSequence.practiceId}")
                    }
                    ApprovedAction(
                        title = tr("Open quick solver", "فتح الحل السريع"),
                        icon = Icons.Outlined.Calculate,
                        background = scheme.tertiaryContainer,
                        foreground = scheme.onTertiaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("home-quick-solver")
                    ) {
                        open("solver")
                    }
                    ApprovedAction(
                        title = tr("View all progress", "عرض التقدم الكامل"),
                        icon = Icons.Outlined.Insights,
                        background = scheme.primaryContainer,
                        foreground = scheme.onPrimaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("home-all-progress")
                    ) {
                        open("progress")
                    }
                }

                // Mastery feedback
                ApprovedPanel {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(D.gutter)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(D.icon),
                            tint = scheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            ProgressFeedback(
                                title = tr("Limits mastery", "إتقان النهايات"),
                                detail = "${mastery.score}%",
                                fraction = mastery.score / 100f,
                                tag = "student-mastery-limits"
                            )
                        }
                    }
                }

                // Upcoming assignment if available
                if (assignment != null) {
                    Text(
                        text = tr("Upcoming assignment", "واجبك القادم"),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Surface(
                        onClick = { open("assignments/${assignment.id}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = D.touch)
                            .testTag("home-upcoming-assignment"),
                        shape = RoundedCornerShape(D.radius),
                        color = scheme.surface,
                        border = BorderStroke(1.dp, scheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(D.cardInset),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(assignment.title.text(), style = MaterialTheme.typography.titleMedium)
                            Text(
                                assignment.description.text(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant
                            )
                            Text(
                                tr("View assignment", "عرض الواجب"),
                                style = MaterialTheme.typography.labelLarge,
                                color = scheme.primary
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // 5. DISCOVER GEMS BANNER & SUPER TEASER
                // -------------------------------------------------------------
                DiscoverGemsCard(
                    language = state.language,
                    onOpenWallet = { open("gems/wallet") }
                )

                SuperPackageCard(
                    language = state.language
                )

                // 6. Weekly Gem Review
                if (owner != null) {
                    WeeklyGemCard(gems, state.language, state) { open("gems/review") }
                }

                // Safe bottom padding for navigation bar
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENTS
// =============================================================================

/**
 * Top Header matching reference visual:
 * Deep Navy pill, Greeting with student name, Gems badge, Streak, Rank
 */
@Composable
private fun JourneyTopHeader(
    studentName: String,
    gemCountText: String,
    streak: Int,
    rankText: String,
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

            // Row 2: Status Badges (Gems, Streak, Rank)
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

                // Rank Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF292415) else Color(0xFFFFF8E5),
                    border = BorderStroke(1.dp, Color(0xFFFFB800).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 14.sp
                        )
                        Text(
                            text = rankText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFB800)
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
    claimedRewards: Set<String>,
    dark: Boolean,
    isAr: Boolean,
    open: (String) -> Unit,
    onClaimReward: (String) -> Unit
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
                node.isRewardNode -> {
                    if (node.id in claimedRewards) JourneyNodeStatus.CLAIMED_REWARD
                    else if (completedSet.contains("limits-foundations") && completedSet.contains("limits-practice"))
                        JourneyNodeStatus.AVAILABLE_REWARD
                    else JourneyNodeStatus.LOCKED
                }
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
                open = open,
                onClaimReward = { onClaimReward(node.id) }
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
    open: (String) -> Unit,
    onClaimReward: () -> Unit
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

                    JourneyNodeStatus.AVAILABLE_REWARD, JourneyNodeStatus.CLAIMED_REWARD -> RewardChestJourneyNode(
                        node = node,
                        isClaimed = status == JourneyNodeStatus.CLAIMED_REWARD,
                        dark = dark,
                        onClaim = onClaimReward
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

/**
 * Reward Chest Node: Golden Chest with claimable Gems
 */
@Composable
private fun RewardChestJourneyNode(
    node: JourneyNodeData,
    isClaimed: Boolean,
    dark: Boolean,
    onClaim: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tooltip Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isClaimed) if (dark) Color(0xFF1E241A) else Color(0xFFEDF7EB)
            else if (dark) Color(0xFF2C2412) else Color(0xFFFFF8E5),
            border = BorderStroke(
                1.dp,
                if (isClaimed) Color(0xFF4CAF50) else Color(0xFFFFB800)
            )
        ) {
            Text(
                text = if (isClaimed) "تم استلام المكافأة" else node.title.text(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isClaimed) Color(0xFF4CAF50) else Color(0xFFFFB800),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // Golden Chest Circle
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = if (isClaimed) 2.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFFFFB800),
                    spotColor = Color(0xFFFFB800)
                )
                .background(
                    brush = if (isClaimed) Brush.linearGradient(listOf(Color(0xFF388E3C), Color(0xFF2E7D32)))
                    else Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))),
                    shape = CircleShape
                )
                .border(2.dp, if (isClaimed) Color(0xFF81C784) else Color(0xFFFFE082), CircleShape)
                .clip(CircleShape)
                .clickable(enabled = !isClaimed) { onClaim() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isClaimed) "✅" else "🎁",
                fontSize = 26.sp
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
