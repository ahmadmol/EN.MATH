package com.ahmadmol.enmath.features.student

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence

// Premium Design Tokens for Curriculum Screen
object CurriculumColors {
    val DarkMainBg = Color(0xFF081426)
    val DarkCardBg = Color(0xFF10243C)
    val DarkElevatedBg = Color(0xFF152D49)
    val DarkBorder = Color(0xFF1E3A5F)

    val LightMainBg = Color(0xFFF4F6FB)
    val LightCardBg = Color(0xFFFFFFFF)
    val LightElevatedBg = Color(0xFFEBF2FA)
    val LightBorder = Color(0xFFD0DCEB)

    val PrimaryBlue = Color(0xFF168BFF)
    val ElectricBlue = Color(0xFF0072FF)
    val CyanAccent = Color(0xFF23C6F5)
    val PurpleAccent = Color(0xFF9462F5)
    val GoldAccent = Color(0xFFFFB800)

    val DarkTextPrimary = Color(0xFFF5F8FF)
    val DarkTextSecondary = Color(0xFFA6B7D2)

    val LightTextPrimary = Color(0xFF0F172A)
    val LightTextSecondary = Color(0xFF475569)

    @Composable
    fun mainBg(isDark: Boolean) = if (isDark) DarkMainBg else LightMainBg

    @Composable
    fun cardBg(isDark: Boolean) = if (isDark) DarkCardBg else LightCardBg

    @Composable
    fun elevatedBg(isDark: Boolean) = if (isDark) DarkElevatedBg else LightElevatedBg

    @Composable
    fun border(isDark: Boolean) = if (isDark) DarkBorder else LightBorder

    @Composable
    fun textPrimary(isDark: Boolean) = if (isDark) DarkTextPrimary else LightTextPrimary

    @Composable
    fun textSecondary(isDark: Boolean) = if (isDark) DarkTextSecondary else LightTextSecondary
}

/**
 * Premium Curriculum Screen Implementation for EN.MATH
 */
@Composable
fun CurriculumScreen(
    state: LearningState,
    open: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Determine current active topic and lesson from real student state
    val activeLessonId = state.lastLessonId.ifEmpty { LimitsLessonSequence.lessonId }
    val activeTopic = state.topicOf(activeLessonId) ?: state.topic("limits") ?: state.topics.firstOrNull()
    val activeTopicId = activeTopic?.id ?: "limits"

    // Calculate real completion stats
    val topicLessons = activeTopic?.lessons.orEmpty()
    val totalTopicActivities = topicLessons.size.coerceAtLeast(1)
    val completedTopicActivities = topicLessons.count { it.id in state.completed }
    val topicMastery = LearningRules.topic(state, activeTopicId)
    val progressFraction = if (totalTopicActivities > 0) {
        completedTopicActivities.toFloat() / totalTopicActivities
    } else {
        topicMastery.score / 100f
    }
    val progressPercent = (progressFraction * 100).toInt().coerceIn(0, 100)

    // Books calculations from real data
    val books = state.books
    val book1 = books.find { it.id == "book-1" } ?: books.firstOrNull()
    val book2 = books.find { it.id == "book-2" } ?: books.getOrNull(1)

    val book1Total = book1?.lessons?.size ?: 12
    val book1Completed = book1?.lessons?.count { it.id in state.completed } ?: 0

    val book2Total = book2?.lessons?.size ?: 12
    val book2Completed = book2?.lessons?.count { it.id in state.completed } ?: 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CurriculumColors.mainBg(isDark))
            .testTag("screen-study")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Header with 3D math illustration
            CurriculumHeroHeader(isDark = isDark)

            // 2. Current Learning Card
            CurrentLearningCard(
                topicTitle = activeTopic?.title?.text() ?: tr("Exploring Limits", "استكشاف النهايات"),
                completedActivities = completedTopicActivities,
                totalActivities = totalTopicActivities,
                progressPercent = progressPercent,
                progressFraction = progressFraction,
                isDark = isDark,
                onContinue = { open("topic/$activeTopicId") }
            )

            // 3. Section Title: Books & Other Topics
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr("Books & Other Topics", "الكتب والمواضيع الأخرى"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = CurriculumColors.textPrimary(isDark)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tr("Choose a book and start your learning journey.", "اختر كتاباً وابدأ في رحلتك التعليمية."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CurriculumColors.textSecondary(isDark)
                    )
                }

                // Stacked Books Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDark) Color(0xFF162B48) else Color(0xFFE2EDF8)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoStories,
                        contentDescription = null,
                        tint = CurriculumColors.PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 4. Book 1 Card (Analysis & Sequences)
            book1?.let { b1 ->
                CurriculumBookCard(
                    bookId = b1.id,
                    title = b1.title.text(),
                    subtitle = b1.subtitle.text(),
                    topicsSummary = tr("Limits, Continuity, Derivatives, Integrals & Sequences", "النهايات، الاستمرار، الاشتقاق، التكامل والمتتاليات"),
                    completedLessons = book1Completed,
                    totalLessons = book1Total,
                    isBookTwo = false,
                    isDark = isDark,
                    onOpenBook = { open("book/${b1.id}") },
                    topics = listOf(
                        TopicChipData("limits", tr("Limits", "النهايات"), TopicIconType.Graph),
                        TopicChipData("derivatives", tr("Derivatives", "الاشتقاق"), TopicIconType.Derivative),
                        TopicChipData("integrals", tr("Integrals", "التكامل"), TopicIconType.Integral),
                        TopicChipData("sequences", tr("Sequences", "المتتاليات"), TopicIconType.Sequence)
                    ),
                    onTopicClick = { topicId -> open("topic/$topicId") }
                )
            }

            // 5. Book 2 Card (Algebra, Probability & Geometry)
            book2?.let { b2 ->
                CurriculumBookCard(
                    bookId = b2.id,
                    title = b2.title.text(),
                    subtitle = b2.subtitle.text(),
                    topicsSummary = tr("Complex Numbers, Linear Algebra, Spatial Geometry & Probability", "الأعداد المركبة، الجبر الخطي، الهندسة في الفراغ، والاحتمالات"),
                    completedLessons = book2Completed,
                    totalLessons = book2Total,
                    isBookTwo = true,
                    isDark = isDark,
                    onOpenBook = { open("book/${b2.id}") },
                    topics = listOf(
                        TopicChipData("logarithms", tr("Complex Numbers", "الأعداد المركبة"), TopicIconType.ComplexNumber),
                        TopicChipData("matrices", tr("Linear Algebra", "الجبر الخطي"), TopicIconType.Matrix),
                        TopicChipData("geometry", tr("Spatial Geometry", "الهندسة في الفراغ"), TopicIconType.Geometry3D),
                        TopicChipData("probability", tr("Probability", "الاحتمالات"), TopicIconType.Probability)
                    ),
                    onTopicClick = { topicId -> open("topic/$topicId") }
                )
            }

            // 6. Browse All Lessons Card
            BrowseAllLessonsCard(
                isDark = isDark,
                onClick = { open("study/catalog") }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Top Header section matching the reference design artwork and typography.
 */
@Composable
private fun CurriculumHeroHeader(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Text Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = tr("Learn through connected ideas", "تعلّم عبر أفكار مترابطة"),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = CurriculumColors.CyanAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tr("Your Curriculum", "منهاجك"),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = CurriculumColors.textPrimary(isDark)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tr(
                    "Choose a book and follow your learning path step by step.",
                    "اختر كتاباً واتّبع مسارك في التعلّم خطوة بخطوة."
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = CurriculumColors.textSecondary(isDark)
            )
        }

        // Math Illustration Box
        HeaderMathArtwork(
            modifier = Modifier.size(width = 110.dp, height = 95.dp)
        )
    }
}

/**
 * Native vector Canvas illustration for top header (books + pi + limit + curve).
 */
@Composable
private fun HeaderMathArtwork(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Glowing backdrop blur circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CurriculumColors.PrimaryBlue.copy(alpha = 0.25f),
                        CurriculumColors.PurpleAccent.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.4f, h * 0.5f),
                    radius = w * 0.6f
                ),
                center = Offset(w * 0.4f, h * 0.5f),
                radius = w * 0.55f
            )

            // Function curve path (y = f(x))
            val curvePath = Path().apply {
                moveTo(w * 0.2f, h * 0.35f)
                cubicTo(
                    w * 0.4f, h * 0.1f,
                    w * 0.6f, h * 0.5f,
                    w * 0.95f, h * 0.2f
                )
            }
            drawPath(
                path = curvePath,
                color = CurriculumColors.CyanAccent,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Book 1 (Bottom Dark Blue Book)
            val b1 = Path().apply {
                moveTo(w * 0.05f, h * 0.68f)
                lineTo(w * 0.45f, h * 0.55f)
                lineTo(w * 0.55f, h * 0.65f)
                lineTo(w * 0.15f, h * 0.78f)
                close()
            }
            drawPath(b1, color = Color(0xFF10284D))

            // Book 2 (Middle Red/Burgundy Book)
            val b2 = Path().apply {
                moveTo(w * 0.08f, h * 0.60f)
                lineTo(w * 0.48f, h * 0.47f)
                lineTo(w * 0.58f, h * 0.57f)
                lineTo(w * 0.18f, h * 0.70f)
                close()
            }
            drawPath(
                b2,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF8B1E3F), Color(0xFFD83A56))
                )
            )

            // Book 3 (Top Premium Blue 3D Book with Spine & Pages)
            val topCover = Path().apply {
                moveTo(w * 0.12f, h * 0.48f)
                lineTo(w * 0.52f, h * 0.35f)
                lineTo(w * 0.62f, h * 0.45f)
                lineTo(w * 0.22f, h * 0.58f)
                close()
            }
            drawPath(
                topCover,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF168BFF), Color(0xFF2E5BFF))
                )
            )

            // White Pages Edge of Top Book
            val pages = Path().apply {
                moveTo(w * 0.52f, h * 0.35f)
                lineTo(w * 0.55f, h * 0.38f)
                lineTo(w * 0.65f, h * 0.48f)
                lineTo(w * 0.62f, h * 0.45f)
                close()
            }
            drawPath(pages, color = Color(0xFFE8F0FE))
        }

        // Overlay Mathematical Symbols (pi & lim)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Pi symbol on top book
            Text(
                text = "π",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 28.dp, top = 36.dp)
            )

            // lim x->0 math symbol
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 4.dp, top = 6.dp)
            ) {
                Text(
                    text = "lim",
                    color = CurriculumColors.CyanAccent.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "x→0",
                    color = CurriculumColors.CyanAccent.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Serif
                )
            }
        }
    }
}

/**
 * Premium Current Learning Card (Goal, circular progress, node line, action button).
 */
@Composable
private fun CurrentLearningCard(
    topicTitle: String,
    completedActivities: Int,
    totalActivities: Int,
    progressPercent: Int,
    progressFraction: Float,
    isDark: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 8.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = CurriculumColors.PrimaryBlue.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        CurriculumColors.PrimaryBlue.copy(alpha = 0.6f),
                        CurriculumColors.CyanAccent.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CurriculumColors.cardBg(isDark)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Circular Progress on Left, Info on Right (RTL layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Header tag with Target Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(CurriculumColors.PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GpsFixed,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = tr("Current Goal", "الهدف الحالي"),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = CurriculumColors.textSecondary(isDark)
                        )
                    }

                    // Main Topic Title
                    Text(
                        text = topicTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = CurriculumColors.textPrimary(isDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Subtitle progress text
                    Text(
                        text = tr(
                            "$completedActivities of $totalActivities activities completed",
                            "$completedActivities من $totalActivities أنشطة مكتملة"
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
                        ),
                        color = CurriculumColors.textSecondary(isDark)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Step Progress Nodes Indicator
                    StepNodesProgressIndicator(
                        completed = completedActivities,
                        total = totalActivities
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Circular Progress Ring
                CircularLearningGauge(
                    percentage = progressPercent,
                    progress = progressFraction,
                    isDark = isDark
                )
            }

            // Main Action Button: Continue Learning
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("student-continue")
                    .testTag("study-limits"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Unspecified
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    CurriculumColors.ElectricBlue,
                                    CurriculumColors.CyanAccent
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = CurriculumColors.ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = tr("Continue Learning", "متابعة التعلم"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular progress ring gauge with glowing percentage text.
 */
@Composable
private fun CircularLearningGauge(
    percentage: Int,
    progress: Float,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        val trackColor = if (isDark) Color(0xFF17304D) else Color(0xFFE0ECF8)
        val arcBrush = Brush.sweepGradient(
            colors = listOf(
                CurriculumColors.PrimaryBlue,
                CurriculumColors.CyanAccent,
                CurriculumColors.PrimaryBlue
            )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track Ring
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            val sweep = (progress.coerceIn(0f, 1f) * 360f)
            if (sweep > 0f) {
                drawArc(
                    brush = arcBrush,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            color = CurriculumColors.textPrimary(isDark)
        )
    }
}

/**
 * Step nodes indicator showing completed steps with checkmarks.
 */
@Composable
private fun StepNodesProgressIndicator(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val steps = total.coerceAtLeast(3)
    Row(
        modifier = modifier.fillMaxWidth(0.85f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until steps) {
            val isDone = i < completed
            val isCurrent = i == completed

            if (i > 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            if (i <= completed) CurriculumColors.CyanAccent
                            else Color(0xFF233B58)
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> CurriculumColors.CyanAccent
                            isCurrent -> CurriculumColors.PrimaryBlue
                            else -> Color(0xFF172B45)
                        }
                    )
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = when {
                            isDone || isCurrent -> CurriculumColors.CyanAccent
                            else -> Color(0xFF2C496E)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color(0xFF081426),
                        modifier = Modifier.size(13.dp)
                    )
                } else if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

/**
 * Book Card for Book 1 & Book 2.
 */
@Composable
private fun CurriculumBookCard(
    bookId: String,
    title: String,
    subtitle: String,
    topicsSummary: String,
    completedLessons: Int,
    totalLessons: Int,
    isBookTwo: Boolean,
    isDark: Boolean,
    onOpenBook: () -> Unit,
    topics: List<TopicChipData>,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (completedLessons.toFloat() / totalLessons.coerceAtLeast(1)).coerceIn(0f, 1f)
    val accentColor = if (isBookTwo) CurriculumColors.PurpleAccent else CurriculumColors.PrimaryBlue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CurriculumColors.border(isDark),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("book-$bookId"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CurriculumColors.cardBg(isDark)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Chevron left/right button on end, Title & Badge on start (RTL friendly)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Chevron Open Button
                IconButton(
                    onClick = onOpenBook,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0xFF1C3250) else Color(0xFFE4EDF8)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = tr("Open book", "فتح الكتاب"),
                        tint = CurriculumColors.textPrimary(isDark)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title & Subtitle
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = CurriculumColors.textSecondary(isDark)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = CurriculumColors.textPrimary(isDark)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = topicsSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = CurriculumColors.textSecondary(isDark),
                        maxLines = Int.MAX_VALUE
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Book Badge artwork
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isBookTwo) {
                                    listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                                } else {
                                    listOf(Color(0xFF168BFF), Color(0xFF0052CC))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBookTwo) {
                        // 3D Cube Icon for Book 2
                        Icon(
                            imageVector = Icons.Outlined.ViewInAr,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        // Integral formula text for Book 1
                        Text(
                            text = "∫f(x)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }

            // Progress Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = tr(
                        "$completedLessons of $totalLessons lessons completed",
                        "$completedLessons من $totalLessons دروس مكتملة"
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = CurriculumColors.textSecondary(isDark)
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = if (isDark) Color(0xFF192C45) else Color(0xFFE2E8F0)
                )
            }

            // Topic Chips Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = if (LocalDensity.current.fontScale > 1.3f) 2 else 4,
            ) {
                topics.forEach { chipData ->
                    CurriculumTopicChip(
                        data = chipData,
                        isDark = isDark,
                        onClick = { onTopicClick(chipData.topicId) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

enum class TopicIconType {
    Graph,
    Derivative,
    Integral,
    Sequence,
    ComplexNumber,
    Matrix,
    Geometry3D,
    Probability
}

data class TopicChipData(
    val topicId: String,
    val title: String,
    val iconType: TopicIconType
)

/**
 * Interactive Topic Chip Component.
 */
@Composable
private fun CurriculumTopicChip(
    data: TopicChipData,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("topic-${data.topicId}"),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Color(0xFF162A45) else Color(0xFFEAF2FB),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF223F66) else Color(0xFFCBDDF2))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Mini Icon
            TopicChipIcon(data.iconType, isDark)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = data.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = CurriculumColors.textPrimary(isDark),
                maxLines = Int.MAX_VALUE,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TopicChipIcon(type: TopicIconType, isDark: Boolean) {
    val tint = CurriculumColors.CyanAccent
    when (type) {
        TopicIconType.Graph -> Icon(Icons.AutoMirrored.Outlined.ShowChart, null, tint = tint, modifier = Modifier.size(14.dp))
        TopicIconType.Derivative -> Text("d/dx", color = tint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        TopicIconType.Integral -> Text("∫", color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        TopicIconType.Sequence -> Text(":::", color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        TopicIconType.ComplexNumber -> Text("i", color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        TopicIconType.Matrix -> Text("[::]", color = tint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        TopicIconType.Geometry3D -> Icon(Icons.Outlined.ViewInAr, null, tint = tint, modifier = Modifier.size(14.dp))
        TopicIconType.Probability -> Icon(Icons.Outlined.Casino, null, tint = tint, modifier = Modifier.size(14.dp))
    }
}

/**
 * Card for browsing all lessons with 3D Golden Trophy artwork.
 */
@Composable
private fun BrowseAllLessonsCard(
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CurriculumColors.border(isDark),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("browse-all-lessons"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CurriculumColors.cardBg(isDark)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Trophy Artwork
            GoldenTrophyArtwork(
                modifier = Modifier.size(width = 80.dp, height = 70.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = tr("Browse All Lessons", "استعراض جميع الدروس"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = CurriculumColors.textPrimary(isDark)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tr("Browse all units and topics", "تصفح جميع الوحدات والموضوعات"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp
                    ),
                    color = CurriculumColors.textSecondary(isDark)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action Arrow Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CurriculumColors.PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Native Canvas drawing of a 3D Golden Trophy sitting on books with sparkles.
 */
@Composable
private fun GoldenTrophyArtwork(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Glow aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CurriculumColors.GoldAccent.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.4f),
                    radius = w * 0.5f
                ),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = w * 0.45f
            )

            // Books base at bottom
            val bookBase = Path().apply {
                moveTo(w * 0.1f, h * 0.82f)
                lineTo(w * 0.9f, h * 0.82f)
                lineTo(w * 0.85f, h * 0.95f)
                lineTo(w * 0.15f, h * 0.95f)
                close()
            }
            drawPath(bookBase, color = Color(0xFF1E3A60))

            // Trophy Cup Body
            val cup = Path().apply {
                moveTo(w * 0.28f, h * 0.18f)
                lineTo(w * 0.72f, h * 0.18f)
                lineTo(w * 0.64f, h * 0.52f)
                cubicTo(w * 0.6f, h * 0.65f, w * 0.4f, h * 0.65f, w * 0.36f, h * 0.52f)
                close()
            }
            drawPath(
                cup,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFDF00), Color(0xFFD4AF37))
                )
            )

            // Trophy Stem & Base
            val stem = Path().apply {
                moveTo(w * 0.44f, h * 0.62f)
                lineTo(w * 0.56f, h * 0.62f)
                lineTo(w * 0.60f, h * 0.78f)
                lineTo(w * 0.40f, h * 0.78f)
                close()
            }
            drawPath(
                stem,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD4AF37), Color(0xFFAA7C11))
                )
            )

            // Trophy Handles
            drawArc(
                color = Color(0xFFFFDF00),
                startAngle = 120f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.16f, h * 0.22f),
                size = Size(w * 0.25f, h * 0.32f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFFFDF00),
                startAngle = -80f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.59f, h * 0.22f),
                size = Size(w * 0.25f, h * 0.32f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Sparkle stars
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = CurriculumColors.GoldAccent,
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.TopEnd)
        )
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = CurriculumColors.GoldAccent,
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.TopStart)
        )
    }
}
