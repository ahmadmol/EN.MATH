package com.ahmadmol.enmath.features.student

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.localgems.gemBalanceText
import com.ahmadmol.enmath.features.planner.PlanSummary
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Interactive Learning Map Entry for Grade 9.
 */
@Composable
fun Grade9RoadmapHomeScreen(
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState(),
    planner: PlannerRepository? = null,
    workspace: SourceWorkspaceRepository? = null
) {
    Grade9LearningPath(state, open, gems, planner, workspace)
}

/**
 * Interactive Learning Map Entry for Baccalaureate.
 */
@Composable
fun BaccalaureateRoadmapHomeScreen(
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState(),
    planner: PlannerRepository? = null,
    workspace: SourceWorkspaceRepository? = null
) {
    InteractiveRoadmapHome(
        stage = StudyStage.BAC,
        state = state,
        open = open,
        gems = gems,
        planner = planner,
        workspace = workspace
    )
}

/**
 * Interactive Learning Map screen matching Image 11 (Grade 9) and Image 12 (Baccalaureate).
 * Features:
 * - Gamified header (Avatar, greeting, EN.MATH logo, streak 🔥, gems 💎, level ⭐, notification bell).
 * - "تابع دراستك" hero card with 3D character art, active progress bar, and large pulsing play button.
 * - Distinct Book 1 & Book 2 headers for Baccalaureate.
 * - Curving snake learning path with lesson stations, completed checkmarks, active play nodes, and milestone chests.
 * - Background math doodles (formulas, coordinate curves, 3D shapes).
 * - Final comprehensive exam section with Trophy, Graduation cap, and test milestones.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteractiveRoadmapHome(
    stage: StudyStage,
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState,
    planner: PlannerRepository?,
    workspace: SourceWorkspaceRepository?
) {
    val catalog = SourceCurricula.forStage(stage)
    if (catalog == null) {
        ScreenPage { com.ahmadmol.enmath.features.courses.CatalogUnavailable() }
        return
    }

    val dark = isDarkTheme()
    val scope = StudentCatalog.scope(state)
    val changed = workspace?.changes?.collectAsState()?.value
    val read = remember(scope, changed) { scope?.let { workspace?.read(it) } }
    val visited = (read as? SourceWorkspaceRead.Ready)?.workspace?.visitedId?.let(catalog::research)
    val savedIds = (read as? SourceWorkspaceRead.Ready)?.workspace?.savedIds.orEmpty()
    val completedLessons = StudentCatalog.completed(state)

    val listState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()
    var rewardDialogGems by remember { mutableStateOf<Int?>(null) }

    // First research as fallback for "continue learning"
    val firstResearch = catalog.researches.firstOrNull()
    val activeResearch = visited ?: firstResearch
    val activeUnit = activeResearch?.let { catalog.unit(it.unitId) } ?: catalog.units.firstOrNull()

    // Gamification values
    val streak = LearningRules.stats(state).streak
    val gemCount = gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language)
    val completedCount = completedLessons.size + savedIds.size
    val level = (completedCount / 3 + 1).coerceAtLeast(1)
    val unreadNotifications = state.product.notifications.count { !it.teacher && it.id !in state.product.readNotifications }

    if (rewardDialogGems != null) {
        AlertDialog(
            onDismissRequest = { rewardDialogGems = null },
            icon = {
                Image(
                    painter = painterResource(R.drawable.icon_chest),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
            },
            title = {
                Text(
                    text = tr("Milestone Reward!", "مكافأة الإنجاز!"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = tr(
                        "Great job progressing along your math journey! You have earned +${rewardDialogGems} gems.",
                        "أحسنت بمتابعة مسار تعلّمك في الرياضيات! حصلت على +${rewardDialogGems} جوهرة إضافية."
                    ),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { rewardDialogGems = null },
                    colors = ButtonDefaults.buttonColors(containerColor = if (stage == StudyStage.G9) Color(0xFF00A896) else Color(0xFF0066FF))
                ) {
                    Text(tr("Collect", "استلام"))
                }
            }
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-home"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Top Gamification Header Bar (Image 11 & 12 top)
        item(key = "roadmap_header") {
            GamifiedTopHeader(
                stage = stage,
                userName = state.user?.name.orEmpty(),
                streak = streak,
                gemCount = gemCount,
                level = level,
                unreadNotifications = unreadNotifications,
                dark = dark,
                onOpenNotifications = { open("notifications") },
                onOpenWallet = { open("gems/wallet") }
            )
            Spacer(Modifier.height(14.dp))
        }

        // 2. "تابع دراستك" Hero Card with 3D Character Illustration (Image 11 & 12 Hero)
        item(key = "roadmap_hero") {
            ContinueStudyHeroCard(
                stage = stage,
                research = activeResearch,
                unit = activeUnit,
                catalog = catalog,
                dark = dark,
                onContinue = { open(activeResearch?.route ?: "study") }
            )
            Spacer(Modifier.height(16.dp))
        }

        // 3. Quick Tools Navigation Bar
        item(key = "roadmap_tools") {
            RoadmapQuickTools(open = open)
            Spacer(Modifier.height(16.dp))
        }

        // 4. Study Plan Summary (Integrated)
        item(key = "roadmap_plan") {
            PlanSummary(state, planner, open)
            Spacer(Modifier.height(16.dp))
        }

        // 5. The Interactive Snake Map Path across all Units and Lessons
        renderInteractiveLearningPath(
            stage = stage,
            catalog = catalog,
            activeResearchId = activeResearch?.id,
            completedLessons = completedLessons,
            savedIds = savedIds,
            dark = dark,
            open = open,
            onRewardClick = { rewardDialogGems = 25 }
        )

        // 6. Final Comprehensive Exam Footer Section (Image 11 & 12 bottom)
        item(key = "roadmap_final_exam") {
            ComprehensiveExamFooter(
                stage = stage,
                dark = dark,
                open = open,
                onBackToTop = { coroutine.launch { listState.animateScrollToItem(0) } }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Top Gamified Bar with Avatar, Greeting, Logo, Streak, Gems, and Level.
 */
@Composable
private fun GamifiedTopHeader(
    stage: StudyStage,
    userName: String,
    streak: Int,
    gemCount: String,
    level: Int,
    unreadNotifications: Int,
    dark: Boolean,
    onOpenNotifications: () -> Unit,
    onOpenWallet: () -> Unit
) {
    val studentTitle = if (stage == StudyStage.G9) {
        tr("Grade 9 Student", "طالب الصف التاسع")
    } else {
        tr("Baccalaureate Student", "طالب البكالوريا العلمي")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Notification bell with red dot
        Box(
            modifier = Modifier
                .size(42.dp)
                .liquidGlass(shape = CircleShape, alpha = 0.82f, elevation = 2.dp)
                .clickable { onOpenNotifications() }
                .testTag("home-notifications"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = tr("Notifications", "الإشعارات"),
                tint = if (dark) Color(0xFF8AB4F8) else Color(0xFF0066FF),
                modifier = Modifier.size(22.dp)
            )
            if (unreadNotifications > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 3.dp)
                        .size(9.dp)
                        .background(Color(0xFFFF3B30), CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }

        // Student Avatar + Greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(
                    if (stage == StudyStage.G9) R.drawable.avatar_g9 else R.drawable.avatar_bac
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, if (stage == StudyStage.G9) Color(0xFF00A896) else Color(0xFF0066FF), CircleShape)
            )
            Column {
                Text(
                    text = if (userName.isNotBlank()) "مرحبًا، \u2068$userName\u2069" else tr("Welcome", "مرحبًا بك"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (dark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = studentTitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }

        // EN.MATH Logo in center
        Image(
            painter = painterResource(R.drawable.ic_enmath),
            contentDescription = "EN.MATH",
            modifier = Modifier.size(34.dp)
        )

        // Gamification Pills: Streak, Gems, Level
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Streak Pill
            GamificationPill(
                icon = { Text("🔥", fontSize = 13.sp) },
                value = "$streak",
                label = tr("days", "أيام"),
                tint = Color(0xFFFF6D00),
                bgTint = Color(0xFFFF6D00).copy(alpha = if (dark) 0.22f else 0.12f)
            )

            // Gems Pill
            GamificationPill(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Diamond,
                        contentDescription = null,
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(15.dp)
                    )
                },
                value = gemCount,
                label = tr("gems", "جوهرة"),
                tint = Color(0xFF9C27B0),
                bgTint = Color(0xFF9C27B0).copy(alpha = if (dark) 0.22f else 0.12f),
                tag = "home-gems-badge",
                onClick = onOpenWallet
            )

            // Level Pill
            GamificationPill(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(15.dp)
                    )
                },
                value = "$level",
                label = tr("level", "المستوى"),
                tint = Color(0xFFFFB300),
                bgTint = Color(0xFFFFB300).copy(alpha = if (dark) 0.22f else 0.12f)
            )
        }
    }
}

@Composable
private fun GamificationPill(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    tint: Color,
    bgTint: Color,
    tag: String? = null,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    val tagModifier = if (tag != null) Modifier.testTag(tag) else Modifier

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgTint,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        modifier = Modifier
            .then(tagModifier)
            .then(clickModifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            icon()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = tint
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                    color = tint.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * "تابع دراستك" Hero Card with 3D Character Illustration and Play Action.
 */
@Composable
private fun ContinueStudyHeroCard(
    stage: StudyStage,
    research: SourceResearch?,
    unit: SourceUnit?,
    catalog: SourceCurriculum,
    dark: Boolean,
    onContinue: () -> Unit
) {
    val isG9 = stage == StudyStage.G9
    val bgBrush = if (dark) {
        Brush.linearGradient(
            if (isG9) listOf(Color(0xFF0F2B28), Color(0xFF133B36))
            else listOf(Color(0xFF101D38), Color(0xFF172B54))
        )
    } else {
        Brush.linearGradient(
            if (isG9) listOf(Color(0xFFE0F7F4), Color(0xFFC7EFE8))
            else listOf(Color(0xFFE8F1FC), Color(0xFFD6E6F9))
        )
    }
    val borderColor = if (isG9) Color(0xFF00A896).copy(alpha = 0.4f) else Color(0xFF0066FF).copy(alpha = 0.35f)
    val accentColor = if (isG9) Color(0xFF00A896) else Color(0xFF0066FF)

    val bookTitle = unit?.let { catalog.book(it.bookId)?.title }.orEmpty()
    val unitSubtitle = if (unit != null) "الوحدة ${unit.order}: ${unit.title}" else tr("Mathematics Curriculum", "منهاج الرياضيات")
    val lessonTitle = research?.title ?: tr("Begin your first lesson", "ابدأ درسك الأول")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContinue() },
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left: 3D Student Character
                Image(
                    painter = painterResource(
                        if (isG9) R.drawable.entry_student_grade9_cutout else R.drawable.entry_student_baccalaureate_cutout
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .width(92.dp)
                        .heightIn(max = 120.dp)
                )

                // Middle: Text Info and Progress
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tr("Continue Studying", "تابع دراستك"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = if (bookTitle.isNotBlank()) "$bookTitle — $unitSubtitle" else unitSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = lessonTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    // Progress row: "2/4" + bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "2/4",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        LinearProgressIndicator(
                            progress = { 0.5f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentColor,
                            trackColor = accentColor.copy(alpha = 0.2f)
                        )
                    }
                }

                // Right: Pulsing Circular Play Button
                Surface(
                    onClick = onContinue,
                    shape = CircleShape,
                    color = accentColor,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("student-continue")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = tr("Continue", "متابعة"),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

data class SourcePathEntry(
    val key: String,
    val research: SourceResearch?,
    val station: Int = 0,
    val hasNext: Boolean = false,
)

fun sourcePathEntries(catalog: SourceCurriculum): List<SourcePathEntry> {
    return catalog.units.flatMap { unit ->
        unit.researches.mapIndexed { index, research ->
            SourcePathEntry(
                key = "station-${research.id}",
                research = research,
                station = index,
                hasNext = index < unit.researches.size - 1
            )
        }
    }
}


/**
 * Quick Tools Navigation Bar with Liquid Glass styling.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoadmapQuickTools(open: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LiquidGlassQuickButton(label = tr("Solver", "المحلل"), icon = Icons.Outlined.Calculate, tag = "home-quick-solver") { open("solver") }
        LiquidGlassQuickButton(label = tr("Practice", "التدريب"), icon = Icons.Outlined.Quiz, tag = "home-practice") { open("study/review/all") }
        LiquidGlassQuickButton(label = tr("My progress", "تقدمي"), icon = Icons.AutoMirrored.Outlined.TrendingUp, tag = "home-all-progress") { open("progress") }
        LiquidGlassQuickButton(label = tr("Graph lab", "مختبر الرسم"), icon = Icons.Outlined.AutoGraph) { open("explore") }
    }
}

@Composable
private fun LiquidGlassQuickButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String? = null,
    onClick: () -> Unit
) {
    val tagModifier = if (tag != null) Modifier.testTag(tag) else Modifier

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp,
        modifier = Modifier
            .then(tagModifier)
            .heightIn(min = 44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Render the full interactive snake path with units, math doodles, and lesson nodes.
 */
private fun LazyListScope.renderInteractiveLearningPath(
    stage: StudyStage,
    catalog: SourceCurriculum,
    activeResearchId: String?,
    completedLessons: Set<String>,
    savedIds: Set<String>,
    dark: Boolean,
    open: (String) -> Unit,
    onRewardClick: () -> Unit
) {
    var globalStationIndex = 0

    catalog.books.forEachIndexed { bookIndex, book ->
        // For Baccalaureate, render the Book Divider Banners (Image 12)
        if (stage == StudyStage.BAC) {
            item(key = "book_banner_${book.id}") {
                BaccalaureateBookDivider(book = book, bookIndex = bookIndex, dark = dark, onClick = { open(book.route) })
                Spacer(Modifier.height(14.dp))
            }
        }

        book.units.forEachIndexed { unitIndex, unit ->
            // Unit Header Card
            item(key = "unit_header_${unit.id}") {
                UnitHeaderCard(
                    unit = unit,
                    stage = stage,
                    bookIndex = bookIndex,
                    dark = dark,
                    onClick = { open(unit.route) }
                )
                Spacer(Modifier.height(10.dp))
            }

            // Lessons in this Unit
            itemsIndexed(
                items = unit.researches,
                key = { _, research -> "station_${research.id}" }
            ) { researchIndex, research ->
                val currentStation = globalStationIndex++
                val isCompleted = research.id in completedLessons || research.id in savedIds
                val isCurrent = research.id == activeResearchId
                val hasRewardChest = researchIndex == unit.researches.lastIndex || (researchIndex > 0 && researchIndex % 3 == 0)

                LearningMapStationRow(
                    stage = stage,
                    research = research,
                    unit = unit,
                    stationIndex = currentStation,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    hasRewardChest = hasRewardChest,
                    dark = dark,
                    open = open,
                    onRewardClick = onRewardClick
                )
            }
        }
    }
}

/**
 * Book Banner for Baccalaureate (Book 1 Blue / Book 2 Purple).
 */
@Composable
private fun BaccalaureateBookDivider(
    book: SourceBook,
    bookIndex: Int,
    dark: Boolean,
    onClick: () -> Unit
) {
    val isBook1 = bookIndex == 0
    val gradientColors = if (isBook1) {
        if (dark) listOf(Color(0xFF152A56), Color(0xFF1C3A7A))
        else listOf(Color(0xFF0066FF), Color(0xFF0052CC))
    } else {
        if (dark) listOf(Color(0xFF381A54), Color(0xFF4C2472))
        else listOf(Color(0xFF7C3AED), Color(0xFF6B21A8))
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home-book-${book.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isBook1) "الكتاب الأول" else "الكتاب الثاني",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Text(
                    text = "${book.units.size} وحدات • ${book.researches.size} بحثًا",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Unit Header Card along the learning path.
 */
@Composable
private fun UnitHeaderCard(
    unit: SourceUnit,
    stage: StudyStage,
    bookIndex: Int,
    dark: Boolean,
    onClick: () -> Unit
) {
    val isG9 = stage == StudyStage.G9
    val badgeColor = when {
        isG9 -> Color(0xFF00A896)
        bookIndex == 1 -> Color(0xFF7C3AED)
        else -> Color(0xFF0066FF)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (dark) Color(0xFF121B30) else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home-unit-${unit.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pill tag e.g. "الوحدة 1"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeColor
            ) {
                Text(
                    text = "الوحدة ${unit.order}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = unit.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (dark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${unit.researches.size} أبحاث من المصدر",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Single Station on the Winding S-Curved Track with math doodles and nodes.
 */
@Composable
private fun LearningMapStationRow(
    stage: StudyStage,
    research: SourceResearch,
    unit: SourceUnit,
    stationIndex: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    hasRewardChest: Boolean,
    dark: Boolean,
    open: (String) -> Unit,
    onRewardClick: () -> Unit
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val isG9 = stage == StudyStage.G9
    val accentColor = if (isG9) Color(0xFF00A896) else Color(0xFF0066FF)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        // Compute S-curve lateral offset
        val offsetFactor = 0.26f * sin(stationIndex * 1.35f)
        val nextOffsetFactor = 0.26f * sin((stationIndex + 1) * 1.35f)
        val xOffsetDp = this.maxWidth * offsetFactor
        val nextXOffsetDp = this.maxWidth * nextOffsetFactor

        // 1. Draw Connecting Bezier Snake Path
        Canvas(modifier = Modifier.fillMaxSize()) {
            val direction = if (rtl) -1f else 1f
            val startX = (size.width / 2) + xOffsetDp.toPx() * direction
            val startY = 36.dp.toPx()
            val endX = (size.width / 2) + nextXOffsetDp.toPx() * direction
            val endY = size.height + 36.dp.toPx()

            val controlY1 = startY + (endY - startY) * 0.5f
            val controlY2 = startY + (endY - startY) * 0.5f

            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(startX, controlY1, endX, controlY2, endX, endY)
            }

            // Path shadow/glow
            drawPath(
                path = path,
                color = if (isCompleted) accentColor.copy(alpha = 0.35f) else if (dark) Color(0xFF263354) else Color(0xFFCBD5E1),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Math Doodles on canvas sides
            drawMathDoodle(stationIndex, stage, size, dark)
        }

        // 2. Interactive Station Node
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = xOffsetDp * (if (rtl) -1f else 1f))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Node Circle
            StationNodeButton(
                stationNumber = "${unit.order}.${research.order}",
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                accentColor = accentColor,
                dark = dark,
                tag = "home-research-${research.id}",
                onClick = { open(research.route) }
            )

            // Optional Milestone Reward Chest beside station
            if (hasRewardChest) {
                RewardChestButton(
                    onClick = onRewardClick
                )
            }
        }
    }
}

/**
 * Circular Node Button (Completed with checkmark, Current with pulsing Play, or Available).
 */
@Composable
private fun StationNodeButton(
    stationNumber: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    accentColor: Color,
    dark: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.testTag(tag)
    ) {
        if (isCurrent) {
            // Glowing outer ring
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(pulseScale)
                    .background(accentColor.copy(alpha = 0.25f), CircleShape)
            )
        }

        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = when {
                isCompleted -> accentColor
                isCurrent -> accentColor
                else -> if (dark) Color(0xFF1E293B) else Color.White
            },
            border = BorderStroke(
                if (isCurrent) 2.5.dp else 1.5.dp,
                if (isCompleted || isCurrent) Color.White else accentColor.copy(alpha = 0.5f)
            ),
            shadowElevation = if (isCurrent) 8.dp else 3.dp,
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    isCompleted -> {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    isCurrent -> {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = stationNumber,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Milestone Reward Chest Button.
 */
@Composable
private fun RewardChestButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFFFFF3CD),
        border = BorderStroke(1.dp, Color(0xFFFFC107)),
        shadowElevation = 3.dp,
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.icon_chest),
                contentDescription = tr("Reward Chest", "صندوق المكافأة"),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Draw decorative math doodles on Canvas matching Image 11 and 12.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMathDoodle(
    index: Int,
    stage: StudyStage,
    canvasSize: androidx.compose.ui.geometry.Size,
    dark: Boolean
) {
    val doodleColor = if (dark) Color(0xFF475569).copy(alpha = 0.25f) else Color(0xFF94A3B8).copy(alpha = 0.35f)
    val rightSide = index % 2 == 0
    val x = if (rightSide) canvasSize.width * 0.85f else canvasSize.width * 0.12f
    val y = canvasSize.height * 0.45f

    when (index % 5) {
        0 -> {
            // Triangle
            val p = Path().apply {
                moveTo(x, y - 14.dp.toPx())
                lineTo(x + 16.dp.toPx(), y + 14.dp.toPx())
                lineTo(x - 16.dp.toPx(), y + 14.dp.toPx())
                close()
            }
            drawPath(p, doodleColor, style = Stroke(1.5.dp.toPx()))
        }
        1 -> {
            // Function curve / wave
            val p = Path().apply {
                moveTo(x - 16.dp.toPx(), y)
                cubicTo(x - 8.dp.toPx(), y - 12.dp.toPx(), x + 8.dp.toPx(), y + 12.dp.toPx(), x + 16.dp.toPx(), y)
            }
            drawPath(p, doodleColor, style = Stroke(1.5.dp.toPx()))
        }
        2 -> {
            // Circle with radius
            drawCircle(doodleColor, radius = 12.dp.toPx(), center = Offset(x, y), style = Stroke(1.5.dp.toPx()))
            drawLine(doodleColor, Offset(x, y), Offset(x + 12.dp.toPx(), y), strokeWidth = 1.2.dp.toPx())
        }
        3 -> {
            // Geometric rectangle / cube face
            drawRect(doodleColor, topLeft = Offset(x - 10.dp.toPx(), y - 10.dp.toPx()), size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 20.dp.toPx()), style = Stroke(1.5.dp.toPx()))
        }
        4 -> {
            // Coordinate axes
            drawLine(doodleColor, Offset(x - 12.dp.toPx(), y), Offset(x + 12.dp.toPx(), y), strokeWidth = 1.2.dp.toPx())
            drawLine(doodleColor, Offset(x, y - 12.dp.toPx()), Offset(x, y + 12.dp.toPx()), strokeWidth = 1.2.dp.toPx())
        }
    }
}

/**
 * Final Comprehensive Exam Footer Section (Image 11 & 12 bottom).
 */
@Composable
private fun ComprehensiveExamFooter(
    stage: StudyStage,
    dark: Boolean,
    open: (String) -> Unit,
    onBackToTop: () -> Unit
) {
    val isG9 = stage == StudyStage.G9
    val bgBrush = if (dark) {
        Brush.linearGradient(listOf(Color(0xFF131F3B), Color(0xFF1A2A4E)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFE8F1FC), Color(0xFFDCEAF9)))
    }
    val borderColor = if (dark) Color(0xFF253965) else Color(0xFFC8DEFA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trophy and Graduation Cap illustration row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_trophy),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                Image(
                    painter = painterResource(R.drawable.icon_graduation),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Title & Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tr("Comprehensive Final Exam", "الامتحان الشامل"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = if (dark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = if (isG9) tr("General review across all Grade 9 units", "مراجعة عامة على جميع وحدات التاسع")
                    else tr("General review and exam across all Baccalaureate units", "مراجعة واختبار على جميع وحدات البكالوريا"),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }

            // 3 Interactive Action Nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExamActionNode(
                    icon = Icons.AutoMirrored.Outlined.FactCheck,
                    label = tr("Review", "مراجعة"),
                    enabled = true,
                    dark = dark,
                    onClick = { open("study/review/all") }
                )
                ExamActionNode(
                    icon = Icons.Outlined.Diamond,
                    label = tr("Practice Exam", "اختبار تجريبي"),
                    enabled = true,
                    dark = dark,
                    onClick = { open(if (isG9) "study/review/all" else "study/demo") }
                )
                ExamActionNode(
                    icon = Icons.Filled.Lock,
                    label = tr("Final Exam", "الامتحان النهائي"),
                    enabled = false,
                    dark = dark,
                    onClick = {}
                )
            }

            HorizontalDivider(
                color = if (dark) Color(0xFF263765) else Color(0xFFCBD5E1),
                thickness = 0.8.dp,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Back to top button
            TextButton(
                onClick = onBackToTop,
                modifier = Modifier.heightIn(min = 40.dp)
            ) {
                Text(
                    text = tr("Back to top of map", "العودة إلى بداية المسار"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isG9) Color(0xFF00A896) else Color(0xFF0066FF)
                )
            }
        }
    }
}

@Composable
private fun ExamActionNode(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    dark: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(enabled = enabled) { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = when {
                !enabled -> if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                else -> if (dark) Color(0xFF223565) else Color(0xFFE6EFFF)
            },
            border = BorderStroke(
                1.dp,
                if (!enabled) Color.Transparent else if (dark) Color(0xFF3B5696) else Color(0xFFB8D3FE)
            ),
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = when {
                        !enabled -> if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        else -> if (dark) Color(0xFF7CA6FF) else Color(0xFF0066FF)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = when {
                !enabled -> if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                else -> if (dark) Color.White else Color(0xFF0F172A)
            }
        )
    }
}
