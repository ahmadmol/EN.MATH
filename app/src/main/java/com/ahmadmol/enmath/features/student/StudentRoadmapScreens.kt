package com.ahmadmol.enmath.features.student

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.tokens.ResourceDimensions as D
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.localgems.gemBalanceText
import kotlin.math.sin

/** Station Node Status for the S-shaped learning roadmap */
enum class RoadmapNodeStatus {
    COMPLETED,
    IN_PROGRESS,
    AVAILABLE,
    LOCKED,
    CONTENT_PENDING
}

/** Data representation of a single research node on the winding roadmap */
data class RoadmapResearchNode(
    val id: String,
    val unitNumber: Int,
    val lessonNumber: Int,
    val labelText: String,
    val title: String,
    val subtitle: String?,
    val status: RoadmapNodeStatus,
    val route: String,
    val isAlgebra: Boolean = true,
    val bookVolume: Int = 1
)

/** Data representation of a unit banner along the roadmap */
data class RoadmapUnitSection(
    val unitOrder: Int,
    val title: String,
    val subtitle: String,
    val researches: List<RoadmapResearchNode>,
    val isAlgebra: Boolean = true,
    val bookVolume: Int = 1
)

// =============================================================================
// GRADE 9 ROADMAP HOME SCREEN
// =============================================================================

@Composable
fun Grade9RoadmapHomeScreen(
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState(),
    planner: PlannerRepository? = null
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val isAr = state.language == Language.Arabic

    val studentName = state.user?.name?.takeIf { it.isNotBlank() } ?: if (isAr) "طالب الصف التاسع" else "Grade 9 Student"
    val owner = LocalGemsPolicy.owner(state.user)
    val gemCountText = gemBalanceText(gems, owner, state.language)
    val stats = LearningRules.stats(state)
    val progress = state.progress
    val level = (stats.streak / 3 + 1).coerceAtLeast(1)

    val activeLesson = state.lesson(state.lastLessonId)
        ?: state.lessons.firstOrNull()

    // Build Grade 9 Unit Sections (10 Units, 32 Researches)
    val completedSet = StudentCatalog.completed(state)
    val unitSections = remember(completedSet, state.lastLessonId) {
        G9CatalogData.units.map { unit ->
            val isAlg = unit.ref.sourceBookId == "G9-ALG"
            val researches = unit.lessons.map { lesson ->
                val lessonId = lesson.ref.lessonId.orEmpty()
                val isCompleted = lessonId in completedSet
                val isCurrent = lessonId == state.lastLessonId
                val isAvailable = state.lesson(lessonId) != null

                val status = when {
                    isCompleted -> RoadmapNodeStatus.COMPLETED
                    isCurrent -> RoadmapNodeStatus.IN_PROGRESS
                    isAvailable -> RoadmapNodeStatus.AVAILABLE
                    else -> RoadmapNodeStatus.CONTENT_PENDING
                }

                RoadmapResearchNode(
                    id = lessonId,
                    unitNumber = unit.order,
                    lessonNumber = lesson.order,
                    labelText = "${unit.order}.${lesson.order}",
                    title = lesson.title,
                    subtitle = lesson.draftObjective.ifBlank { "بحث صفحة ${lesson.pages.from} - ${lesson.pages.to}" },
                    status = status,
                    route = if (isAvailable) "lesson/$lessonId" else "book/${unit.ref.sourceBookId}",
                    isAlgebra = isAlg
                )
            }

            RoadmapUnitSection(
                unitOrder = unit.order,
                title = if (isAr) "الوحدة ${unit.order}: ${unit.title}" else "Unit ${unit.order}: ${unit.title}",
                subtitle = if (isAr) "الأبحاث والصفحات ${unit.pages.from}-${unit.pages.to}" else "Pages ${unit.pages.from}-${unit.pages.to}",
                researches = researches,
                isAlgebra = isAlg
            )
        }
    }

    val totalResearches = remember(unitSections) { unitSections.sumOf { it.researches.size } }
    val allNodesList = remember(unitSections) { unitSections.flatMap { it.researches } }

    val lazyListState = rememberLazyListState()

    // Background Gradient for Grade 9 (Turquoise / Light Teal theme)
    val g9BackgroundBrush = Brush.verticalGradient(
        if (dark) listOf(Color(0xFF041820), Color(0xFF0A2B35), Color(0xFF051D24))
        else listOf(Color(0xFFE0F7FA), Color(0xFFE8F8FA), Color(0xFFF0FBFD))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-home")
            .background(g9BackgroundBrush),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .widthIn(max = D.contentMax)
                .fillMaxWidth()
                .padding(horizontal = if (LocalConfiguration.current.screenWidthDp > 600) 24.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // 1. TOP HEADER BAR
            item(key = "g9_header") {
                G9TopHeader(
                    studentName = studentName,
                    gemCountText = gemCountText,
                    streak = stats.streak,
                    level = level,
                    dark = dark,
                    isAr = isAr,
                    onOpenWallet = { open("gems/wallet") }
                )
            }

            // 2. HERO CONTINUATION CARD ("تابع دراستك")
            item(key = "g9_hero") {
                G9HeroContinuationCard(
                    activeLessonTitle = activeLesson?.title?.let { if (isAr) it.ar else it.en } ?: tr("طبيعة الأعداد والكسور", "Number Nature & Fractions"),
                    unitTitle = tr("الوحدة 1: الأعداد الجبرية", "Unit 1: Algebraic Numbers"),
                    progressText = "2 / 4",
                    dark = dark,
                    isAr = isAr,
                    onContinue = {
                        if (activeLesson != null) open("lesson/${activeLesson.id}")
                        else open("book/G9-ALG")
                    }
                )
            }

            // 3. DAILY GOAL CARD
            item(key = "g9_daily_goal") {
                RoadmapDailyGoalCard(
                    todayCompleted = progress.todayCompleted,
                    dailyGoal = state.dailyGoal,
                    dark = dark,
                    isAr = isAr
                )
            }

            // 4. QUICK TOOLS & ACTIONS ROW (Testing & Quick Navigation)
            item(key = "g9_quick_tools") {
                RoadmapQuickToolsRow(
                    dark = dark,
                    isAr = isAr,
                    open = open
                )
            }

            // 5. ALGEBRA SECTION HEADER (Units 1 - 6)
            item(key = "g9_algebra_banner") {
                RoadmapSubjectBanner(
                    title = if (isAr) "كتاب الجبر" else "Algebra Textbook",
                    subtitle = if (isAr) "6 وحدات · 19 بحثاً مفهرساً" else "6 Units · 19 Researches",
                    iconRes = R.drawable.icon_solver,
                    accentColor = Color(0xFF00ACC1),
                    dark = dark
                )
            }

            // 6. ROADMAP PATH & STATIONS FOR ALGEBRA (Units 1 to 6)
            var globalNodeIndex = 0
            unitSections.filter { it.isAlgebra }.forEach { unitSection ->
                item(key = "g9_unit_header_${unitSection.unitOrder}") {
                    RoadmapUnitHeaderBadge(
                        title = unitSection.title,
                        subtitle = unitSection.subtitle,
                        badgeColor = Color(0xFF00ACC1),
                        dark = dark
                    )
                }

                itemsIndexed(
                    items = unitSection.researches,
                    key = { _, node -> "g9_node_${node.id}" }
                ) { idx, node ->
                    val currentIndex = globalNodeIndex++
                    val nextNode = allNodesList.getOrNull(currentIndex + 1)

                    RoadmapStationRow(
                        node = node,
                        nodeIndex = currentIndex,
                        nextNode = nextNode,
                        accentColor = Color(0xFF00ACC1),
                        dark = dark,
                        isAr = isAr,
                        open = open
                    )
                }

                item(key = "g9_unit_chest_${unitSection.unitOrder}") {
                    RoadmapUnitChestNode(
                        unitOrder = unitSection.unitOrder,
                        accentColor = Color(0xFF00ACC1),
                        dark = dark,
                        isAr = isAr
                    )
                }
            }

            // 7. GEOMETRY SECTION HEADER (Units 7 - 10)
            item(key = "g9_geometry_banner") {
                Spacer(modifier = Modifier.height(12.dp))
                RoadmapSubjectBanner(
                    title = if (isAr) "كتاب الهندسة" else "Geometry Textbook",
                    subtitle = if (isAr) "4 وحدات · 13 بحثاً مفهرساً" else "4 Units · 13 Researches",
                    iconRes = R.drawable.icon_geometry,
                    accentColor = Color(0xFF00838F),
                    dark = dark
                )
            }

            // 8. ROADMAP PATH & STATIONS FOR GEOMETRY (Units 7 to 10)
            unitSections.filter { !it.isAlgebra }.forEach { unitSection ->
                item(key = "g9_unit_header_${unitSection.unitOrder}") {
                    RoadmapUnitHeaderBadge(
                        title = unitSection.title,
                        subtitle = unitSection.subtitle,
                        badgeColor = Color(0xFF00838F),
                        dark = dark
                    )
                }

                itemsIndexed(
                    items = unitSection.researches,
                    key = { _, node -> "g9_node_${node.id}" }
                ) { idx, node ->
                    val currentIndex = globalNodeIndex++
                    val nextNode = allNodesList.getOrNull(currentIndex + 1)

                    RoadmapStationRow(
                        node = node,
                        nodeIndex = currentIndex,
                        nextNode = nextNode,
                        accentColor = Color(0xFF00838F),
                        dark = dark,
                        isAr = isAr,
                        open = open
                    )
                }

                item(key = "g9_unit_chest_${unitSection.unitOrder}") {
                    RoadmapUnitChestNode(
                        unitOrder = unitSection.unitOrder,
                        accentColor = Color(0xFF00838F),
                        dark = dark,
                        isAr = isAr
                    )
                }
            }

            // 9. FINAL EXAMINATION & COMPREHENSIVE REVIEW SECTION
            item(key = "g9_final_exam") {
                RoadmapFinalExamSection(
                    title = if (isAr) "الامتحان الشامل — التاسع" else "Grade 9 Comprehensive Exam",
                    subtitle = if (isAr) "مراجعة عامة وااختبارات شاملة على جميع وحدات الجبر والهندسة" else "Full review and comprehensive practice across all units",
                    accentColor = Color(0xFF00ACC1),
                    dark = dark,
                    isAr = isAr,
                    open = open
                )
            }
        }
    }
}

// =============================================================================
// BACCALAUREATE ROADMAP HOME SCREEN
// =============================================================================

@Composable
fun BaccalaureateRoadmapHomeScreen(
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState(),
    planner: PlannerRepository? = null
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val isAr = state.language == Language.Arabic

    val studentName = state.user?.name?.takeIf { it.isNotBlank() } ?: if (isAr) "طالب البكالوريا العلمي" else "Baccalaureate Student"
    val owner = LocalGemsPolicy.owner(state.user)
    val gemCountText = gemBalanceText(gems, owner, state.language)
    val stats = LearningRules.stats(state)
    val progress = state.progress
    val level = (stats.streak / 3 + 1).coerceAtLeast(1)

    val activeLesson = state.lesson(state.lastLessonId)
        ?: state.lessons.firstOrNull()

    // Build Baccalaureate Unit Sections (14 Units across Book 1 & Book 2, 63 Researches)
    val completedSet = StudentCatalog.completed(state)
    val unitSections = remember(completedSet, state.lastLessonId) {
        BacSourceData.units.map { unit ->
            val bookVol = if (unit.sourceId == "bac_sci_math_part1") 1 else 2
            val researches = unit.lessonIds.mapIndexed { idx, lessonId ->
                val lessonObj = BacSourceCatalog.lesson(lessonId)
                val isCompleted = lessonId in completedSet
                val isCurrent = lessonId == state.lastLessonId
                val isAvailable = state.lesson(lessonId) != null

                val status = when {
                    isCompleted -> RoadmapNodeStatus.COMPLETED
                    isCurrent -> RoadmapNodeStatus.IN_PROGRESS
                    isAvailable -> RoadmapNodeStatus.AVAILABLE
                    else -> RoadmapNodeStatus.CONTENT_PENDING
                }

                RoadmapResearchNode(
                    id = lessonId,
                    unitNumber = unit.order,
                    lessonNumber = idx + 1,
                    labelText = "${unit.order}.${idx + 1}",
                    title = lessonObj?.title ?: "البحث ${idx + 1}",
                    subtitle = lessonObj?.edition ?: "فهرس المصدر",
                    status = status,
                    route = if (isAvailable) "lesson/$lessonId" else "book/${unit.sourceId}",
                    bookVolume = bookVol
                )
            }

            RoadmapUnitSection(
                unitOrder = unit.order,
                title = if (isAr) "الوحدة ${unit.order}: ${unit.title}" else "Unit ${unit.order}: ${unit.title}",
                subtitle = if (isAr) "الكتاب ${bookVol} · الأبحاث ${researches.size}" else "Book $bookVol · ${researches.size} Researches",
                researches = researches,
                bookVolume = bookVol
            )
        }
    }

    val allNodesList = remember(unitSections) { unitSections.flatMap { it.researches } }
    val lazyListState = rememberLazyListState()

    // Background Gradient for Baccalaureate (Indigo / Purple theme)
    val bacBackgroundBrush = Brush.verticalGradient(
        if (dark) listOf(Color(0xFF090D1E), Color(0xFF121833), Color(0xFF0B0F24))
        else listOf(Color(0xFFEDE7F6), Color(0xFFF3EFEF), Color(0xFFF8F5FF))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-home")
            .background(bacBackgroundBrush),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .widthIn(max = D.contentMax)
                .fillMaxWidth()
                .padding(horizontal = if (LocalConfiguration.current.screenWidthDp > 600) 24.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // 1. TOP HEADER BAR
            item(key = "bac_header") {
                BacTopHeader(
                    studentName = studentName,
                    gemCountText = gemCountText,
                    streak = stats.streak,
                    level = level,
                    dark = dark,
                    isAr = isAr,
                    onOpenWallet = { open("gems/wallet") }
                )
            }

            // 2. HERO CONTINUATION CARD ("تابع دراستك")
            item(key = "bac_hero") {
                BacHeroContinuationCard(
                    activeLessonTitle = activeLesson?.title?.let { if (isAr) it.ar else it.en } ?: tr("نهاية دالة عند نقطة", "Limit of a Function at a Point"),
                    unitTitle = tr("الكتاب الأول — الوحدة 1: النهايات", "Book 1 — Unit 1: Limits"),
                    progressText = "3 / 5",
                    dark = dark,
                    isAr = isAr,
                    onContinue = {
                        if (activeLesson != null) open("lesson/${activeLesson.id}")
                        else open("study/demo")
                    }
                )
            }

            // 3. DAILY GOAL CARD
            item(key = "bac_daily_goal") {
                RoadmapDailyGoalCard(
                    todayCompleted = progress.todayCompleted,
                    dailyGoal = state.dailyGoal,
                    dark = dark,
                    isAr = isAr
                )
            }

            // 4. QUICK TOOLS & ACTIONS ROW
            item(key = "bac_quick_tools") {
                RoadmapQuickToolsRow(
                    dark = dark,
                    isAr = isAr,
                    open = open
                )
            }

            // 5. BOOK 1 SECTION BANNER (Units 1 - 7)
            item(key = "bac_book1_banner") {
                RoadmapSubjectBanner(
                    title = if (isAr) "الكتاب الأول — رياضيات البكالوريا" else "Textbook 1 — Baccalaureate Math",
                    subtitle = if (isAr) "7 وحدات · 34 بحثاً مفهرساً" else "7 Units · 34 Researches",
                    iconRes = R.drawable.icon_book,
                    accentColor = Color(0xFF3F51B5),
                    dark = dark
                )
            }

            // 6. ROADMAP PATH & STATIONS FOR BOOK 1 (Units 1 to 7)
            var globalNodeIndex = 0
            unitSections.filter { it.bookVolume == 1 }.forEach { unitSection ->
                item(key = "bac_unit_header_${unitSection.unitOrder}") {
                    RoadmapUnitHeaderBadge(
                        title = unitSection.title,
                        subtitle = unitSection.subtitle,
                        badgeColor = Color(0xFF3F51B5),
                        dark = dark
                    )
                }

                itemsIndexed(
                    items = unitSection.researches,
                    key = { _, node -> "bac_node_${node.id}" }
                ) { idx, node ->
                    val currentIndex = globalNodeIndex++
                    val nextNode = allNodesList.getOrNull(currentIndex + 1)

                    RoadmapStationRow(
                        node = node,
                        nodeIndex = currentIndex,
                        nextNode = nextNode,
                        accentColor = Color(0xFF3F51B5),
                        dark = dark,
                        isAr = isAr,
                        open = open
                    )
                }

                item(key = "bac_unit_chest_${unitSection.unitOrder}") {
                    RoadmapUnitChestNode(
                        unitOrder = unitSection.unitOrder,
                        accentColor = Color(0xFF3F51B5),
                        dark = dark,
                        isAr = isAr
                    )
                }
            }

            // 7. BOOK 2 SECTION BANNER (Units 8 - 14)
            item(key = "bac_book2_banner") {
                Spacer(modifier = Modifier.height(12.dp))
                RoadmapSubjectBanner(
                    title = if (isAr) "الكتاب الثاني — رياضيات البكالوريا" else "Textbook 2 — Baccalaureate Math",
                    subtitle = if (isAr) "7 وحدات · 29 بحثاً مفهرساً" else "7 Units · 29 Researches",
                    iconRes = R.drawable.icon_book,
                    accentColor = Color(0xFF7E57C2),
                    dark = dark
                )
            }

            // 8. ROADMAP PATH & STATIONS FOR BOOK 2 (Units 8 to 14)
            unitSections.filter { it.bookVolume == 2 }.forEach { unitSection ->
                item(key = "bac_unit_header_${unitSection.unitOrder}") {
                    RoadmapUnitHeaderBadge(
                        title = unitSection.title,
                        subtitle = unitSection.subtitle,
                        badgeColor = Color(0xFF7E57C2),
                        dark = dark
                    )
                }

                itemsIndexed(
                    items = unitSection.researches,
                    key = { _, node -> "bac_node_${node.id}" }
                ) { idx, node ->
                    val currentIndex = globalNodeIndex++
                    val nextNode = allNodesList.getOrNull(currentIndex + 1)

                    RoadmapStationRow(
                        node = node,
                        nodeIndex = currentIndex,
                        nextNode = nextNode,
                        accentColor = Color(0xFF7E57C2),
                        dark = dark,
                        isAr = isAr,
                        open = open
                    )
                }

                item(key = "bac_unit_chest_${unitSection.unitOrder}") {
                    RoadmapUnitChestNode(
                        unitOrder = unitSection.unitOrder,
                        accentColor = Color(0xFF7E57C2),
                        dark = dark,
                        isAr = isAr
                    )
                }
            }

            // 9. FINAL EXAMINATION & COMPREHENSIVE REVIEW SECTION
            item(key = "bac_final_exam") {
                RoadmapFinalExamSection(
                    title = if (isAr) "الامتحان الشامل — البكالوريا العلمي" else "Baccalaureate Comprehensive Exam",
                    subtitle = if (isAr) "مراجعة شاملة واختبارات معتمدة على كامل مفردات الكتابين" else "Comprehensive exam and practice across both textbooks",
                    accentColor = Color(0xFF7E57C2),
                    dark = dark,
                    isAr = isAr,
                    open = open
                )
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENTS & REUSABLE ROADMAP ELEMENTS
// =============================================================================

@Composable
private fun G9TopHeader(
    studentName: String,
    gemCountText: String,
    streak: Int,
    level: Int,
    dark: Boolean,
    isAr: Boolean,
    onOpenWallet: () -> Unit
) {
    val bgNavy = if (dark) Color(0xFF0D2530) else Color(0xFFFFFFFF)
    val borderColor = if (dark) Color(0xFF194452) else Color(0xFFB2EBF2)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = bgNavy,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (dark) 4.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar_g9),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF00ACC1), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = if (isAr) "مرحباً بك، $studentName" else "Welcome, $studentName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (dark) Color.White else Color(0xFF004D40)
                        )
                        Text(
                            text = if (isAr) "طالب الصف التاسع · مسارك الرسمي" else "Grade 9 Official Math Roadmap",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (dark) Color(0xFF80CBC4) else Color(0xFF00796B)
                        )
                    }
                }

                Image(
                    painter = painterResource(R.drawable.ic_enmath),
                    contentDescription = "EN.MATH",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Stats Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF332010) else Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_flame),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAr) "$streak أيام متتالية" else "$streak Days Streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Surface(
                    onClick = onOpenWallet,
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF0A2E38) else Color(0xFFE0F7FA),
                    border = BorderStroke(1.dp, Color(0xFF00ACC1).copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("home-gems-badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_gem),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = gemCountText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF00838F)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF262A10) else Color(0xFFF9FBE7),
                    border = BorderStroke(1.dp, Color(0xFFC0CA33).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_star),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAr) "مستوى $level" else "Lvl $level",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF827717)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BacTopHeader(
    studentName: String,
    gemCountText: String,
    streak: Int,
    level: Int,
    dark: Boolean,
    isAr: Boolean,
    onOpenWallet: () -> Unit
) {
    val bgNavy = if (dark) Color(0xFF101732) else Color(0xFFFFFFFF)
    val borderColor = if (dark) Color(0xFF1E2850) else Color(0xFFC5CAE9)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = bgNavy,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (dark) 4.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar_bac),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF3F51B5), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = if (isAr) "مرحباً بك، $studentName" else "Welcome, $studentName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (dark) Color.White else Color(0xFF1A237E)
                        )
                        Text(
                            text = if (isAr) "طالب البكالوريا العلمي · مسارك المعتمد" else "Scientific Baccalaureate Math Roadmap",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (dark) Color(0xFF9FA8DA) else Color(0xFF3F51B5)
                        )
                    }
                }

                Image(
                    painter = painterResource(R.drawable.ic_enmath),
                    contentDescription = "EN.MATH",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Stats Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF332010) else Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_flame),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAr) "$streak أيام متتالية" else "$streak Days Streak",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Surface(
                    onClick = onOpenWallet,
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF1A1C40) else Color(0xFFE8EAF6),
                    border = BorderStroke(1.dp, Color(0xFF3F51B5).copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("home-gems-badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_gem),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = gemCountText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF303F9F)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF2B1A38) else Color(0xFFF3E5F5),
                    border = BorderStroke(1.dp, Color(0xFF8E24AA).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_star),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAr) "مستوى $level" else "Lvl $level",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun G9HeroContinuationCard(
    activeLessonTitle: String,
    unitTitle: String,
    progressText: String,
    dark: Boolean,
    isAr: Boolean,
    onContinue: () -> Unit
) {
    Surface(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("continue-lesson"),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, Color(0xFF00ACC1).copy(alpha = 0.6f)),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (dark) listOf(Color(0xFF004D40), Color(0xFF006064))
                        else listOf(Color(0xFF00ACC1), Color(0xFF26C6DA))
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = if (isAr) "تابع دراستك" else "Continue Studying",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = unitTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Text(
                        text = activeLessonTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { 0.5f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.35f)
                        )
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(90.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.student_g9_transparent),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.icon_play),
                                contentDescription = "Play",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BacHeroContinuationCard(
    activeLessonTitle: String,
    unitTitle: String,
    progressText: String,
    dark: Boolean,
    isAr: Boolean,
    onContinue: () -> Unit
) {
    Surface(
        onClick = onContinue,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("continue-lesson"),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, Color(0xFF3F51B5).copy(alpha = 0.6f)),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (dark) listOf(Color(0xFF1A237E), Color(0xFF311B92))
                        else listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = if (isAr) "تابع دراستك" else "Continue Studying",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = unitTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Text(
                        text = activeLessonTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.35f)
                        )
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(90.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.student_bac_transparent),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.icon_play),
                                contentDescription = "Play",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapDailyGoalCard(
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF00ACC1),
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isAr) "هدف اليوم التعليمي" else "Daily Learning Goal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (dark) Color.White else Color(0xFF15192C)
                    )
                    Text(
                        text = if (isAr)
                            "أكمل $activeGoal أنشطة يومياً لتعزيز إتقان المفاهيم"
                        else
                            "Complete $activeGoal activities daily for mastery",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dark) Color(0xFF9AA2C3) else Color(0xFF646E8D)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoadmapQuickToolsRow(
    dark: Boolean,
    isAr: Boolean,
    open: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ApprovedAction(
            title = if (isAr) "تدريبات سريعة" else "Quick Practice",
            icon = Icons.Outlined.EditNote,
            background = scheme.secondaryContainer,
            foreground = scheme.onSecondaryContainer,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("home-practice")
        ) {
            open("practice")
        }

        ApprovedAction(
            title = if (isAr) "المحلّل الرياضي" else "Math Solver",
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
            title = if (isAr) "التقدم المحفوظ" else "All Progress",
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
}

@Composable
private fun RoadmapSubjectBanner(
    title: String,
    subtitle: String,
    iconRes: Int,
    accentColor: Color,
    dark: Boolean
) {
    val bgCard = if (dark) Color(0xFF121E36) else Color(0xFFF0F4FD)
    val borderColor = accentColor.copy(alpha = 0.4f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = bgCard,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (dark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun RoadmapUnitHeaderBadge(
    title: String,
    subtitle: String,
    badgeColor: Color,
    dark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (dark) Color(0xFF101B2B) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = badgeColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFF90A4AE) else Color(0xFF546E7A)
            )
        }
    }
}

@Composable
private fun RoadmapStationRow(
    node: RoadmapResearchNode,
    nodeIndex: Int,
    nextNode: RoadmapResearchNode?,
    accentColor: Color,
    dark: Boolean,
    isAr: Boolean,
    open: (String) -> Unit
) {
    val xOffsetPx = sin(nodeIndex * 0.75f) * 75f
    val xOffsetDp = xOffsetPx.dp
    val nextXOffsetDp = (if (nextNode != null) sin((nodeIndex + 1) * 0.75f) * 75f else 0f).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (nextNode != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = (size.width / 2) + xOffsetDp.toPx()
                val startY = 32.dp.toPx()
                val endX = (size.width / 2) + nextXOffsetDp.toPx()
                val endY = size.height + 8.dp.toPx()

                val controlY = startY + (endY - startY) * 0.5f

                val path = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(startX, controlY, endX, controlY, endX, endY)
                }

                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = if (dark) 0.5f else 0.4f),
                    style = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(x = xOffsetDp)
        ) {
            Surface(
                onClick = { open(node.route) },
                shape = CircleShape,
                color = when (node.status) {
                    RoadmapNodeStatus.COMPLETED -> Color(0xFF4CAF50)
                    RoadmapNodeStatus.IN_PROGRESS -> accentColor
                    RoadmapNodeStatus.AVAILABLE -> if (dark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                    RoadmapNodeStatus.LOCKED -> if (dark) Color(0xFF121824) else Color(0xFFECEFF1)
                    RoadmapNodeStatus.CONTENT_PENDING -> if (dark) Color(0xFF182234) else Color(0xFFF1F5F9)
                },
                border = BorderStroke(
                    width = if (node.status == RoadmapNodeStatus.IN_PROGRESS) 3.dp else 2.dp,
                    color = when (node.status) {
                        RoadmapNodeStatus.COMPLETED -> Color(0xFF81C784)
                        RoadmapNodeStatus.IN_PROGRESS -> Color.White
                        RoadmapNodeStatus.AVAILABLE -> accentColor
                        RoadmapNodeStatus.LOCKED -> Color(0xFF90A4AE)
                        RoadmapNodeStatus.CONTENT_PENDING -> accentColor.copy(alpha = 0.5f)
                    }
                ),
                shadowElevation = if (node.status == RoadmapNodeStatus.IN_PROGRESS) 8.dp else 3.dp,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when (node.status) {
                        RoadmapNodeStatus.COMPLETED -> {
                            Image(
                                painter = painterResource(R.drawable.completed),
                                contentDescription = "Completed",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        RoadmapNodeStatus.IN_PROGRESS -> {
                            Image(
                                painter = painterResource(R.drawable.icon_play),
                                contentDescription = "In Progress",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        RoadmapNodeStatus.AVAILABLE -> {
                            Text(
                                text = node.labelText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                        RoadmapNodeStatus.LOCKED -> {
                            Image(
                                painter = painterResource(R.drawable.locked),
                                contentDescription = "Locked",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        RoadmapNodeStatus.CONTENT_PENDING -> {
                            Text(
                                text = node.labelText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (dark) Color(0xFF90A4AE) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (dark) Color(0xFF0F172A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = 140.dp)
            ) {
                Text(
                    text = node.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = if (dark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun RoadmapUnitChestNode(
    unitOrder: Int,
    accentColor: Color,
    dark: Boolean,
    isAr: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (dark) Color(0xFF1E293B) else Color(0xFFFFF8E1),
            border = BorderStroke(2.dp, Color(0xFFFFB300)),
            shadowElevation = 4.dp,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.icon_chest),
                    contentDescription = "Unit Reward Chest",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun RoadmapFinalExamSection(
    title: String,
    subtitle: String,
    accentColor: Color,
    dark: Boolean,
    isAr: Boolean,
    open: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (dark) Color(0xFF101B2B) else Color(0xFFFFFFFF),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_trophy),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Image(
                    painter = painterResource(R.drawable.icon_graduation),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (dark) Color.White else Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { open("review") },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isAr) "مراجعة شاملة" else "Full Review")
                }

                Button(
                    onClick = { open("practice") },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(if (isAr) "اختبار تجريبي" else "Practice Test")
                }
            }
        }
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
