package com.ahmadmol.enmath.features.progress

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.ahmadmol.enmath.core.design.isDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun ProductProgressScreen(
    state: LearningState,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onNotification: (() -> Unit)? = null,
    onTopicClick: ((String) -> Unit)? = null,
) {
    val stats = LearningRules.stats(state)
    val topics = state.books.flatMap { it.topics }
    val isArabic = state.language == Language.Arabic

    ScreenPage(modifier = modifier.testTag("screen-progress")) {
        ProgressHeaderBar(
            state = state,
            onBack = onBack,
            onSearch = onSearch,
            onNotification = onNotification
        )

        ProgressHeroCard(stats = stats, isArabic = isArabic)

        OverallProgressCard(
            stats = stats,
            completedCount = state.completed.size,
            totalCount = topics.sumOf { it.lessons.size }.coerceAtLeast(1)
        )

        LearningStatsRow(stats = stats)

        WeeklyActivityCard(stats = stats, isArabic = isArabic)

        MasteryByTopicSection(
            state = state,
            topics = topics,
            isArabic = isArabic,
            onTopicClick = onTopicClick
        )
    }
}

@Composable
private fun ProgressHeaderBar(state: LearningState, onBack: (() -> Unit)?, onSearch: (() -> Unit)?, onNotification: (() -> Unit)?) {
    RefTopBar(tr("Your progress", "تقدمك"), onBack, onSearch, onNotification,
        state.product.notifications.count { !it.teacher && it.id !in state.product.readNotifications })
}
@Composable
private fun ProgressHeroCard(stats: ProductStats, isArabic: Boolean) {
    RefHeroCard(
        title = label(TextKey.t_7512420977),
        subtitle = tr("Track your curriculum progress, streak, and topic mastery.", "تابع تقدمك في المنهاج وسلسلة أيامك وإتقان الموضوعات."),
        badgeText = label(TextKey.t_4cf108fea1, stats.streak),
        badgeColor = RefColors.LightBlueHeroBgStart,
        badgeTextColor = RefColors.PrimaryBlue,
        artwork = { ProgressHeroArtwork(Modifier.size(90.dp)) }
    )
}

@Composable
private fun ProgressHeroArtwork(modifier: Modifier = Modifier) {
    val primaryColor = Color(0xFF4F46E5)
    val secondaryColor = Color(0xFF3B82F6)
    val lightTint = Color(0xFF93C5FD)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = lightTint.copy(alpha = 0.2f),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        val barWidth = w * 0.18f
        val gap = w * 0.05f
        val startX = w * 0.12f
        val heights = listOf(h * 0.35f, h * 0.50f, h * 0.65f)

        heights.forEachIndexed { index, barHeight ->
            val bx = startX + index * (barWidth + gap)
            val by = h * 0.85f - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(secondaryColor, primaryColor)
                ),
                topLeft = Offset(bx, by),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )

            val topPath = Path().apply {
                moveTo(bx, by + 4.dp.toPx())
                lineTo(bx + barWidth * 0.5f, by - 6.dp.toPx())
                lineTo(bx + barWidth, by + 4.dp.toPx())
                close()
            }
            drawPath(topPath, color = lightTint.copy(alpha = 0.6f))
        }

        val lastBx = startX + 2 * (barWidth + gap) + barWidth * 0.5f
        val lastBy = h * 0.85f - heights[2]

        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(lastBx, lastBy),
            end = Offset(lastBx, lastBy - 20.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        val flagPath = Path().apply {
            moveTo(lastBx, lastBy - 20.dp.toPx())
            lineTo(lastBx - 14.dp.toPx(), lastBy - 15.dp.toPx())
            lineTo(lastBx, lastBy - 10.dp.toPx())
            close()
        }
        drawPath(flagPath, color = Color(0xFF3B82F6))
    }
}

@Composable
private fun OverallProgressCard(
    stats: ProductStats,
    completedCount: Int,
    totalCount: Int
) {
    val isDark = isDarkTheme()
    val progress = stats.lessonFraction.coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = label(TextKey.t_465bb41873),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$completedCount/$totalCount",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                val progressColor = Color(0xFF00BFA5)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 8.dp.toPx()
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun LearningStatsRow(stats: ProductStats) {
    RefStatsRow(listOf(
        RefStatItem(Icons.Outlined.Schedule, learningNumber(stats.minutes), tr("Learning minutes", "دقائق التعلم")),
        RefStatItem(Icons.AutoMirrored.Outlined.Assignment, learningNumber(stats.exercises), tr("Exercises", "تمارين")),
        RefStatItem(Icons.Outlined.CheckCircle, learningPercent((stats.accuracy * 100).toInt()), tr("Accuracy", "الدقة")),
    ))
}
@Composable
private fun WeeklyActivityCard(stats: ProductStats, isArabic: Boolean) {
    val max = stats.week.maxOrNull()?.coerceAtLeast(1) ?: 1
    val primary = MaterialTheme.colorScheme.primary
    val formatter = java.text.SimpleDateFormat("dd/MM", if (isArabic) java.util.Locale.forLanguageTag("ar") else java.util.Locale.ENGLISH).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
    val today = System.currentTimeMillis() / 86_400_000L
    PremiumHeroCard {
        Text(label(TextKey.t_f9676588a1), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            stats.week.forEachIndexed { index, minutes ->
                Column(Modifier.widthIn(min = 56.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(learningNumber(minutes), style = MaterialTheme.typography.labelLarge)
                    Box(Modifier.height(96.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.width(28.dp).height((96f * minutes / max).coerceAtLeast(4f).dp)
                            .clip(RoundedCornerShape(8.dp)).background(primary.copy(alpha = if (minutes > 0) 1f else .25f)))
                    }
                    Text(formatter.format(java.util.Date((today - (stats.week.lastIndex - index)) * 86_400_000L)),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(tr("Daily learning minutes · last 7 days", "دقائق التعلم اليومية · آخر ٧ أيام"),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun MasteryByTopicSection(
    state: LearningState,
    topics: List<Topic>,
    isArabic: Boolean,
    onTopicClick: ((String) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label(TextKey.t_c5e1bee1ed),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Icon(
                imageVector = Icons.Outlined.Layers,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        topics.forEach { topic ->
            val mastery = LearningRules.topic(state, topic.id)
            TopicMasteryRow(
                topic = topic,
                mastery = mastery,
                isArabic = isArabic,
                onClick = onTopicClick?.let { { it(topic.id) } }
            )
        }
    }
}

@Composable
private fun TopicMasteryRow(
    topic: Topic,
    mastery: TopicMastery,
    isArabic: Boolean,
    onClick: (() -> Unit)?
) {
    val isDark = isDarkTheme()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                TopicMathIcon(
                    symbol = topic.symbol,
                    topicId = topic.id
                )

                Text(
                    text = topic.title.text(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MasteryStageBadge(mastery = mastery, isArabic = isArabic)

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TopicMathIcon(
    symbol: String,
    topicId: String,
    modifier: Modifier = Modifier
) {
    val isDark = isDarkTheme()
    val bgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF0F9FF)
    val textColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when (topicId) {
            "limits" -> {
                Canvas(Modifier.size(24.dp)) {
                    val path = Path().apply {
                        moveTo(2.dp.toPx(), size.height * 0.8f)
                        cubicTo(
                            size.width * 0.3f, size.height * 0.8f,
                            size.width * 0.4f, size.height * 0.2f,
                            size.width * 0.8f, size.height * 0.2f
                        )
                        lineTo(size.width * 0.95f, size.height * 0.2f)
                    }
                    drawPath(
                        path = path,
                        color = textColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            "derivatives" -> {
                Text(
                    text = "f′(x)",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textColor
                    )
                )
            }
            "integrals" -> {
                Text(
                    text = "∫",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp,
                        color = textColor
                    )
                )
            }
            "matrices" -> {
                Text(
                    text = "[ : ]",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textColor
                    )
                )
            }
            "probability" -> {
                Icon(
                    imageVector = Icons.Outlined.Casino,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            "sequences" -> {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            "geometry" -> {
                Text(
                    text = "△",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
            }
            "logarithms" -> {
                Text(
                    text = "ln",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                )
            }
            else -> {
                Text(
                    text = symbol.ifEmpty { "∑" },
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )
                )
            }
        }
    }
}

@Composable
private fun MasteryStageBadge(mastery: TopicMastery, isArabic: Boolean) {
    val isDark = isDarkTheme()
    val (bgColor, contentColor, icon, stageLabel) = when (mastery.stage) {
        Mastery.NotStarted -> Quadruple(
            if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            Icons.Outlined.RadioButtonUnchecked,
            if (isArabic) "لم يبدأ" else "Not Started"
        )
        Mastery.Started -> Quadruple(
            if (isDark) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFFE0F2FE),
            Color(0xFF0284C7),
            Icons.Outlined.Flag,
            if (isArabic) "بدأ" else "Started"
        )
        Mastery.Learning -> Quadruple(
            if (isDark) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFFE0F2FE),
            Color(0xFF0284C7),
            Icons.Outlined.School,
            if (isArabic) "يتعلّم" else "Learning"
        )
        Mastery.Practicing -> Quadruple(
            if (isDark) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFFE0F2FE),
            Color(0xFF0284C7),
            Icons.Outlined.EditNote,
            if (isArabic) "يتدرّب" else "Practicing"
        )
        Mastery.Proficient -> Quadruple(
            if (isDark) Color(0xFF059669).copy(alpha = 0.2f) else Color(0xFFDCFCE7),
            Color(0xFF16A34A),
            Icons.Outlined.Verified,
            if (isArabic) "متمكّن" else "Proficient"
        )
        Mastery.Mastered -> Quadruple(
            if (isDark) Color(0xFF059669).copy(alpha = 0.2f) else Color(0xFFDCFCE7),
            Color(0xFF16A34A),
            Icons.Outlined.EmojiEvents,
            if (isArabic) "متقن" else "Mastered"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            val displayText = if (isArabic) {
                "$stageLabel · %${mastery.score}"
            } else {
                "$stageLabel · ${mastery.score}%"
            }
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
