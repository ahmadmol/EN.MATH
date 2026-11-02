package com.ahmadmol.enmath.features.student

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.StudyStage
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.courses.CatalogUnavailable
import com.ahmadmol.enmath.features.localgems.gemBalanceText
import com.ahmadmol.enmath.features.planner.PlanSummary
import com.ahmadmol.enmath.features.reference.CleanBottomBar
import com.ahmadmol.enmath.features.reference.CleanNavItem
import kotlinx.coroutines.launch

/** Grade 9 consumes source references; browsing is never evidence of learning. */
fun grade9Resume(state: LearningState, visitedId: String?): SourceResearch? =
    SourceCurricula.forState(state)?.takeIf { it.stage == StudyStage.G9 }
        ?.research(visitedId.orEmpty())?.takeIf { StudentCatalog.acceptsReference(state, it.ref) }

fun grade9SubjectUnits(geometry: Boolean, query: String = ""): List<SourceUnit> {
    val catalog = SourceCurricula.forStage(StudyStage.G9)
    val book = catalog.books.single { it.id == if (geometry) "G9-GEO" else "G9-ALG" }
    if (query.isBlank()) return book.units
    val routes = catalog.search(query).map { it.second }.toSet()
    return book.units.filter { it.route in routes || it.researches.any { r -> r.route in routes } || book.route in routes }
}

@Composable
fun Grade9BottomNav(selected: String?, open: (String) -> Unit) {
    // Compose mirrors this logical order in Arabic: Algebra starts at the right; Home stays central.
    CleanBottomBar(listOf(
        CleanNavItem(Routes.Grade9Algebra, tr("Algebra", "الجبر"), Icons.Outlined.Calculate),
        CleanNavItem(Routes.Grade9Geometry, tr("Geometry", "الهندسة"), Icons.Outlined.SquareFoot),
        CleanNavItem(Routes.Home, tr("Home", "الرئيسية"), Icons.Outlined.Home),
        CleanNavItem(Routes.Grade9Exercises, tr("Exercises", "التمارين"), Icons.Outlined.EditNote),
        CleanNavItem(Routes.Profile, tr("Profile", "حسابي"), Icons.Outlined.PersonOutline),
    ), selected, "nav-", dark = false, accessibleCenter = true, open = open)
}

/** A saveable lazy list keeps long screens usable at large type sizes and on return from details. */
@Composable
fun Grade9Scaffold(
    tag: String,
    stateKey: Any? = null,
    initialIndex: Int = 0,
    listState: LazyListState = if (stateKey != null) {
        rememberSaveable(key = stateKey.toString(), saver = LazyListState.Saver) {
            LazyListState(firstVisibleItemIndex = initialIndex)
        }
    } else {
        rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    },
    content: LazyListScope.() -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize().testTag(tag),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun Grade9Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun Grade9Heading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Grade9Action(title: String, route: String, open: (String) -> Unit, tag: String = route) {
    OutlinedButton(
        onClick = { open(route) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(tag),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(title)
    }
}

/**
 * Connecting curved path segment drawn between two station positions on the zigzag learning journey.
 */
@Composable
private fun ZigzagPathSegment(prevBias: Float, currBias: Float, connectingColor: Color, modifier: Modifier = Modifier) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(modifier = modifier.fillMaxWidth().height(42.dp)) {
        val w = size.width
        val h = size.height
        val childWidthPx = 200.dp.toPx()
        val availableSpace = (w - childWidthPx).coerceAtLeast(0f)
        val pBias = (prevBias - 0.5f) * 1.5f
        val cBias = (currBias - 0.5f) * 1.5f
        val resolvedPrev = if (isRtl) -pBias else pBias
        val resolvedCurr = if (isRtl) -cBias else cBias

        val startX = w * 0.5f + resolvedPrev * (availableSpace * 0.5f)
        val endX = w * 0.5f + resolvedCurr * (availableSpace * 0.5f)
        val path = Path().apply {
            moveTo(startX, 0f)
            cubicTo(
                startX, h * 0.52f,
                endX, h * 0.48f,
                endX, h
            )
        }
        drawPath(
            path = path,
            color = connectingColor,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
        // Midpoint decorative indicator
        val midX = (startX + endX) * 0.5f
        drawCircle(
            color = connectingColor.copy(alpha = 0.9f),
            radius = 3.5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(midX, h * 0.5f)
        )
    }
}

/**
 * An interactive station node on the zigzag learning path with smooth touch response.
 */
@Composable
fun PathStationNode(
    research: SourceResearch,
    stationIndex: Int,
    lastVisited: Boolean,
    open: (String) -> Unit,
    bias: Float,
    prevBias: Float = bias,
    progressStatus: LessonProgressStatus = LessonProgressStatus.NOT_STARTED,
    isSuggested: Boolean = false
) {
    val isPilot = research.id == Grade9Pilot.lessonId
    val inInspection = LocalInspectionMode.current
    val naturePilotEnabled = inInspection || ((LocalContext.current.applicationInfo.flags and
        android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    val isAlgebra = research.id.startsWith("G9-ALG")
    val defaultAccent = if (isAlgebra) EntryTeal else EntryIndigo
    val connectingColor = defaultAccent.copy(alpha = 0.35f)
    val isNaturePilot = naturePilotEnabled && NaturePracticeBank.matches(research)
    val accessibilityLabel = tr("${research.order}. ${research.title}", "${research.order}. ${research.title}") +
        when {
            isNaturePilot -> when (progressStatus) {
                LessonProgressStatus.PRACTICE_COMPLETED -> tr(" · Practice completed", " · اكتمل التدريب")
                LessonProgressStatus.NEEDS_REVIEW -> tr(" · Review recommended", " · يُنصح بالمراجعة")
                LessonProgressStatus.STARTED -> tr(" · Practice in progress", " · التدريب قيد التقدم")
                else -> tr(" · Interactive practice available", " · تدريب تفاعلي متاح")
            }
            isPilot -> tr(" · Original pilot lesson available", " · درس أصلي تجريبي متاح")
            lastVisited -> tr(" · Last visited", " · آخر زيارة")
            else -> tr(" · Source reference under review", " · مرجع مصدر قيد المراجعة")
        }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "station-press-scale"
    )

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (stationIndex > 0) {
            ZigzagPathSegment(prevBias = prevBias, currBias = bias, connectingColor = connectingColor)
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = BiasAlignment(horizontalBias = (bias - 0.5f) * 1.5f, verticalBias = 0f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 200.dp)
            ) {
                // Interactive Station Node Button
                Surface(
                    onClick = {
                        open(
                            if (isNaturePilot) NaturePracticeGrader.ROUTE
                            else research.route
                        )
                    },
                    interactionSource = interactionSource,
                    shape = CircleShape,
                    color = when {
                        isNaturePilot -> when (progressStatus) {
                            LessonProgressStatus.PRACTICE_COMPLETED -> Color(0xFF0D9488)
                            LessonProgressStatus.NEEDS_REVIEW -> Color(0xFF4F46E5)
                            LessonProgressStatus.STARTED -> defaultAccent
                            else -> defaultAccent
                        }
                        isPilot -> defaultAccent
                        lastVisited -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (isNaturePilot || isPilot || isSuggested) 3.dp else 2.dp,
                        color = when {
                            isNaturePilot -> when (progressStatus) {
                                LessonProgressStatus.PRACTICE_COMPLETED -> Color(0xFFF59E0B)
                                LessonProgressStatus.NEEDS_REVIEW -> Color(0xFF818CF8)
                                else -> Color.White.copy(alpha = 0.95f)
                            }
                            isPilot -> Color.White.copy(alpha = 0.95f)
                            isSuggested -> defaultAccent
                            lastVisited -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    shadowElevation = if (isNaturePilot || isPilot) 8.dp else 3.dp,
                    modifier = Modifier
                        .size(68.dp)
                        .scale(pressScale)
                        .testTag("station-${research.id}")
                        .semantics {
                            this.contentDescription = accessibilityLabel
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isNaturePilot) {
                            when (progressStatus) {
                                LessonProgressStatus.PRACTICE_COMPLETED -> Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                LessonProgressStatus.NEEDS_REVIEW -> Icon(
                                    imageVector = Icons.Outlined.Replay,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                LessonProgressStatus.STARTED -> Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                else -> Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        } else if (isPilot) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (lastVisited) Icons.Outlined.BookmarkAdded else Icons.AutoMirrored.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = if (lastVisited) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "${research.order}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (lastVisited) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Station Label Card
                Surface(
                    onClick = {
                        open(
                            if (isNaturePilot) "lesson/${research.id}"
                            else research.route
                        )
                    },
                    modifier = Modifier.testTag("station-label-${research.id}"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = research.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = when {
                                isNaturePilot -> when (progressStatus) {
                                    LessonProgressStatus.PRACTICE_COMPLETED -> tr("Practice completed", "اكتمل التدريب")
                                    LessonProgressStatus.NEEDS_REVIEW -> tr("Review recommended", "يُنصح بالمراجعة")
                                    LessonProgressStatus.STARTED -> tr("In progress", "قيد التقدم")
                                    else -> tr("Interactive practice · Available", "تدريب تفاعلي · متاح")
                                }
                                isPilot -> tr("Original pilot · Available", "درس أصلي · متاح")
                                lastVisited -> tr("Last visited · Under review", "آخر زيارة · قيد المراجعة")
                                else -> tr("Source reference · Under review", "مرجع مصدر · قيد المراجعة")
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = when {
                                isNaturePilot -> when (progressStatus) {
                                    LessonProgressStatus.PRACTICE_COMPLETED -> Color(0xFF0D9488)
                                    LessonProgressStatus.NEEDS_REVIEW -> Color(0xFF4F46E5)
                                    else -> defaultAccent
                                }
                                isPilot -> defaultAccent
                                lastVisited -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean, motivating, and child-friendly student learning progress card for Grade 9.
 * Strictly displays only:
 * 1. Current section and unit
 * 2. Truthful available practice progress
 * 3. Short encouraging phrase
 * 4. One clear primary action button (min 48dp)
 * Keeps detailed statistical numbers on the dedicated progress screen to prevent clutter.
 */
@Composable
fun Grade9LearningProgressCard(
    progress: Grade9LearningProgress,
    open: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val teal = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("g9-learning-progress-card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, teal.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Current Section and Unit Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = teal.copy(alpha = 0.14f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AutoGraph,
                            contentDescription = null,
                            tint = teal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tr("Your learning progress", "تقدمك في التعلم"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${tr(progress.activeSubject.en, progress.activeSubject.ar)} — ${tr(progress.activeUnit.en, progress.activeUnit.ar)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = teal
                    )
                }
            }

            // 2. Truthful Available Practice Progress (constant layout height prevents shift)
            LinearProgressIndicator(
                progress = { progress.availablePracticeFraction },
                color = teal,
                trackColor = teal.copy(alpha = 0.18f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .testTag("g9-home-progress-bar")
            )

            Text(
                text = tr(progress.availablePracticeProgressText.en, progress.availablePracticeProgressText.ar),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2
            )

            // 3. Short Encouraging Phrase
            Text(
                text = tr(progress.encouragementMessage.en, progress.encouragementMessage.ar),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2
            )

            // 4. One Clear Primary Action Button
            Button(
                onClick = { open(progress.recommendedRoute) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("g9-progress-action"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = teal)
            ) {
                Text(
                    text = tr(progress.recommendedNextStep.en, progress.recommendedNextStep.ar),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/** Preserved for backward compatibility and test contracts. */
@Suppress("unused")
@Composable
fun PathStation(research: SourceResearch, lastVisited: Boolean, open: (String) -> Unit) {
    val bias = if (research.order % 2 == 0) 0.3f else 0.7f
    PathStationNode(research = research, stationIndex = research.order, lastVisited = lastVisited, open = open, bias = bias)
}

@Composable
fun CurriculumUnitCard(unit: SourceUnit, open: (String) -> Unit) {
    val isAlgebra = unit.bookId == "G9-ALG"
    val accent = if (isAlgebra) EntryTeal else EntryIndigo
    val hasPilot = unit.researches.any { it.id == Grade9Pilot.lessonId }

    OutlinedCard(
        onClick = { open(unit.route) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag("g9-unit-${unit.id}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.14f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${unit.order}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = unit.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (hasPilot)
                        tr("${unit.researches.size} indexed topics · one original pilot available",
                            "${unit.researches.size} أبحاث مفهرسة · درس أصلي تجريبي متاح")
                    else
                        tr("${unit.researches.size} indexed topics · content under review",
                            "${unit.researches.size} أبحاث مفهرسة · المحتوى قيد المراجعة"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * First-time learning subject selection screen for Grade 9 students.
 * Presents a clear, inspiring, and simple choice between Algebra and Geometry.
 */
@Composable
fun Grade9SubjectPicker(
    onSelectSubject: (Grade9Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("g9-subject-picker"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, EntryTeal.copy(alpha = 0.35f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = EntryTeal.copy(alpha = 0.14f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Explore,
                        contentDescription = null,
                        tint = EntryTeal,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = tr("Welcome to Grade 9 Math!", "أهلًا بك في رياضيات التاسع!"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = tr("Choose your learning path to begin your journey. You can easily switch anytime.",
                    "اختر مسار التعلم لتبدأ رحلتك. يمكنك التبديل بينهما بسهولة في أي وقت."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

        // Algebra Option Card
        Card(
            onClick = { onSelectSubject(Grade9Subject.ALGEBRA) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("select-algebra"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, EntryTeal.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EntryTeal.copy(alpha = 0.15f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Calculate,
                            contentDescription = null,
                            tint = EntryTeal,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr("Algebra", "الجبر"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = EntryTeal
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tr("6 units · 19 topics", "6 وحدات · 19 بحثًا"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = EntryTeal
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Geometry Option Card
        Card(
            onClick = { onSelectSubject(Grade9Subject.GEOMETRY) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("select-geometry"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, EntryIndigo.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EntryIndigo.copy(alpha = 0.15f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.SquareFoot,
                            contentDescription = null,
                            tint = EntryIndigo,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr("Geometry", "الهندسة"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = EntryIndigo
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tr("4 units · 13 topics", "4 وحدات · 13 بحثًا"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = EntryIndigo
                )
            }
        }
    }
    }
}

/**
 * Top Segmented Switcher between Algebra and Geometry with smooth transitions.
 */
@Composable
fun Grade9SubjectSwitcher(
    currentSubject: Grade9Subject,
    onSelectSubject: (Grade9Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("g9-subject-switcher"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isAlgebra = currentSubject == Grade9Subject.ALGEBRA
            val isGeometry = currentSubject == Grade9Subject.GEOMETRY

            // Algebra Tab
            Surface(
                onClick = { onSelectSubject(Grade9Subject.ALGEBRA) },
                shape = RoundedCornerShape(14.dp),
                color = if (isAlgebra) EntryTeal else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Space.touch)
                    .testTag("switch-algebra")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Calculate,
                        contentDescription = null,
                        tint = if (isAlgebra) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tr("Algebra (6 Units)", "الجبر (6 وحدات)"),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isAlgebra) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Geometry Tab
            Surface(
                onClick = { onSelectSubject(Grade9Subject.GEOMETRY) },
                shape = RoundedCornerShape(14.dp),
                color = if (isGeometry) EntryIndigo else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Space.touch)
                    .testTag("switch-geometry")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SquareFoot,
                        contentDescription = null,
                        tint = if (isGeometry) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tr("Geometry (4 Units)", "الهندسة (4 وحدات)"),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isGeometry) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun Grade9LearningPath(
    state: LearningState,
    open: (String) -> Unit,
    gems: LocalGemsUiState,
    planner: PlannerRepository?,
    workspace: SourceWorkspaceRepository?,
    grade9Study: Grade9StudyRepository? = null,
    initialPathIndex: Int = 0,
    naturePractice: NaturePracticeRepository? = null
) {
    val catalog = SourceCurricula.forState(state)?.takeIf { it.stage == StudyStage.G9 }
    if (catalog == null) { CatalogUnavailable(); return }
    val scope = StudentCatalog.scope(state)
    val change = workspace?.changes?.collectAsState()?.value
    val read = remember(scope, change) { scope?.let { workspace?.read(it) } }
    val resumed = grade9Resume(state, (read as? SourceWorkspaceRead.Ready)?.workspace?.visitedId)
    val studyRead = rememberG9Study(state, grade9Study)
    val pilotData = (studyRead as? G9StudyRead.Ready)?.data
    if (studyRead == G9StudyRead.StorageError || studyRead == G9StudyRead.OwnerChanged) {
        Grade9Scaffold("screen-home") { item { G9StudyError(studyRead) } }
        return
    }
    val dailyPlan = (rememberG9Plan(state, planner) as? PlanRead.Ready)?.plan ?: StudyPlan()
    val nextStudy = grade9NextStudy(pilotData ?: G9StudyData(), dailyPlan, (read as? SourceWorkspaceRead.Ready)?.workspace?.visitedId, System.currentTimeMillis())
    val natureChange = naturePractice?.changes?.collectAsState()?.value
    val natureRead = remember(scope, natureChange) { scope?.let { naturePractice?.read(it) } }
    val natureData = (natureRead as? NatureRead.Ready)?.data ?: NaturePracticeData()
    val learningProgress = remember(natureData) {
        StudentLearningProgressEngine.computeGrade9Progress(natureData)
    }
    val natureEnabled = LocalInspectionMode.current || (LocalContext.current.applicationInfo.flags and
        android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0)
    val unread = state.product.notifications.count { !it.teacher && it.id !in state.product.readNotifications }
    val tealAccent = MaterialTheme.colorScheme.primary
    val coroutineScope = rememberCoroutineScope()

    // Active Subject selection: default to ALGEBRA for existing/seamless workflows, but allow first-time picker if not chosen
    val activeSubject = pilotData?.activeSubject ?: Grade9Subject.ALGEBRA
    val hasChosen = pilotData?.subjectChosen == true
    val showPicker = !hasChosen && pilotData?.activeSubject == null

    val selectedBookId = if (activeSubject == Grade9Subject.ALGEBRA) "G9-ALG" else "G9-GEO"
    val displayedBook = catalog.books.firstOrNull { it.id == selectedBookId } ?: catalog.books.first()
    val bookAccent = if (displayedBook.id == "G9-ALG") EntryTeal else EntryIndigo

    // Independent scroll positions preserved across Algebra and Geometry switches
    val algebraListState = rememberSaveable(
        key = "g9_home_algebra_scroll_${scope?.ownerId.orEmpty()}",
        saver = LazyListState.Saver
    ) {
        LazyListState(firstVisibleItemIndex = if (activeSubject == Grade9Subject.ALGEBRA) initialPathIndex else 0)
    }
    val geometryListState = rememberSaveable(
        key = "g9_home_geometry_scroll_${scope?.ownerId.orEmpty()}",
        saver = LazyListState.Saver
    ) {
        LazyListState(firstVisibleItemIndex = if (activeSubject == Grade9Subject.GEOMETRY) initialPathIndex else 0)
    }
    val activeListState = if (activeSubject == Grade9Subject.ALGEBRA) algebraListState else geometryListState

    Grade9Scaffold("screen-home", listState = activeListState) {
        if (showPicker) {
            // First-time Subject Choice: Clean, focused onboarding between Algebra and Geometry
            item("greeting") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, tealAccent.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(tealAccent.copy(alpha = 0.14f), MaterialTheme.colorScheme.surface)))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tealAccent.copy(alpha = 0.16f),
                                border = BorderStroke(2.dp, tealAccent.copy(alpha = 0.4f)),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.student_g9_transparent),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(3.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = tr("Hello, ${state.user?.name.orEmpty().ifBlank { tr("Student", "يا بطل") }}", "أهلًا، ${state.user?.name.orEmpty().ifBlank { "يا بطل" }}"),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(shape = RoundedCornerShape(8.dp), color = tealAccent.copy(alpha = 0.18f)) {
                                        Text(
                                            text = tr("Grade 9", "التاسع"),
                                            color = tealAccent,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tr("My gems: ", "جواهري: ") + gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { open("notifications") },
                                modifier = Modifier.size(48.dp).testTag("g9-notifications")
                            ) {
                                BadgedBox(badge = { if (unread > 0) Badge { Text("$unread") } }) {
                                    Icon(Icons.Outlined.Notifications, tr("Notifications, $unread unread", "الإشعارات، $unread غير مقروءة"), tint = tealAccent)
                                }
                            }
                        }
                    }
                }
            }

            item("subject-picker") {
                Grade9SubjectPicker(
                    onSelectSubject = { chosen ->
                        if (scope != null && grade9Study != null) {
                            coroutineScope.launch {
                                grade9Study.selectSubject(scope, chosen)
                            }
                        }
                    }
                )
            }
        } else {
            // 1. Top Section Switcher (Algebra / Geometry)
            item("subject-switcher") {
                Grade9SubjectSwitcher(
                    currentSubject = activeSubject,
                    onSelectSubject = { nextSubject ->
                        if (scope != null && grade9Study != null && nextSubject != activeSubject) {
                            coroutineScope.launch {
                                grade9Study.selectSubject(scope, nextSubject)
                            }
                        }
                    }
                )
            }

            // 2. Compact Student Greeting Card
            item("greeting") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, tealAccent.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(tealAccent.copy(alpha = 0.14f), MaterialTheme.colorScheme.surface)))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tealAccent.copy(alpha = 0.16f),
                                border = BorderStroke(2.dp, tealAccent.copy(alpha = 0.4f)),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.student_g9_transparent),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(3.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = tr("Hello, ${state.user?.name.orEmpty().ifBlank { tr("Student", "يا بطل") }}", "أهلًا، ${state.user?.name.orEmpty().ifBlank { "يا بطل" }}"),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(shape = RoundedCornerShape(8.dp), color = tealAccent.copy(alpha = 0.18f)) {
                                        Text(
                                            text = tr("Grade 9", "التاسع"),
                                            color = tealAccent,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tr("My gems: ", "جواهري: ") + gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { open("notifications") },
                                modifier = Modifier.size(48.dp).testTag("g9-notifications")
                            ) {
                                BadgedBox(badge = { if (unread > 0) Badge { Text("$unread") } }) {
                                    Icon(Icons.Outlined.Notifications, tr("Notifications, $unread unread", "الإشعارات، $unread غير مقروءة"), tint = tealAccent)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Learning Progress Card (Single Clear Follow-up Action)
            item("learning-progress") {
                if (activeSubject == Grade9Subject.ALGEBRA && natureEnabled) {
                    if (natureRead is NatureRead.Ready || LocalInspectionMode.current) {
                        Grade9LearningProgressCard(progress = learningProgress, open = open)
                    } else {
                        NatureProgressReadError()
                    }
                } else {
                    G9LearningCard(tr("Your next step", "خطوتك التالية")) {
                        Text(displayedBook.title)
                        Text(tr("These topics are indexed references. Browsing does not count as completed learning.",
                            "هذه الموضوعات مراجع مفهرسة. تصفّحها لا يُحتسب إنجازًا تعليميًا."))
                        val next = resumed?.takeIf { it in displayedBook.researches } ?: displayedBook.researches.firstOrNull()
                        if (next != null) OutlinedButton({ open("g9/reference/${next.id}") },
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-reference-action")) {
                            Text(tr("Browse reference: ${next.title}", "تصفح المرجع: ${next.title}"))
                        }
                    }
                }
            }

            // Storage warning if workspace read fails
            if (read == SourceWorkspaceRead.StorageError) {
                item("workspace-storage-error") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            tr("Your browsing history could not be read. Existing data is preserved.", "تعذّر قراءة سجل التصفح. بياناتك الحالية محفوظة."),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

        // 4. Current Subject Header Card
        item("book-${displayedBook.id}") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bookAccent.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, bookAccent.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("book-${displayedBook.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (displayedBook.id == "G9-ALG") Icons.Outlined.Calculate else Icons.Outlined.SquareFoot,
                        contentDescription = null,
                        tint = bookAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = displayedBook.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = bookAccent
                        )
                        Text(
                            text = tr("${displayedBook.units.size} units · ${displayedBook.researches.size} indexed topics",
                                "${displayedBook.units.size} وحدات · ${displayedBook.researches.size} بحثًا مفهرسًا"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 6. Zigzag Learning Journey across the selected subject units and researches
        var subjectStationIndex = 0
        displayedBook.units.forEach { unit ->
            item("unit-${unit.id}") {
                CurriculumUnitCard(unit, open)
            }

            unit.researches.forEach { research ->
                val index = subjectStationIndex++
                val prevBias = if (index == 0) 0.5f else when ((index - 1) % 4) {
                    0 -> 0.5f
                    1 -> 0.22f
                    2 -> 0.5f
                    3 -> 0.78f
                    else -> 0.5f
                }
                val currBias = when (index % 4) {
                    0 -> 0.5f
                    1 -> 0.22f
                    2 -> 0.5f
                    3 -> 0.78f
                    else -> 0.5f
                }

                val isNature = NaturePracticeBank.matches(research)
                val stationProgress = if (isNature) learningProgress.natureLessonStatus else LessonProgressStatus.UNAVAILABLE

                item("station-${research.id}") {
                    PathStationNode(
                        research = research,
                        stationIndex = index,
                        lastVisited = resumed?.id == research.id,
                        open = open,
                        bias = currBias,
                        prevBias = prevBias,
                        progressStatus = stationProgress,
                        isSuggested = nextStudy.route == research.route || (isNature && nextStudy.route == NaturePracticeGrader.ROUTE)
                    )
                }
            }
        }

        // 7. Study Plan Summary
        item("plan") {
            PlanSummary(state, planner, open)
        }

        // 8. Tools Shortcuts
        item("tools") {
            Grade9Card {
                Grade9Heading(tr("Your study tools", "أدوات دراستك"), tr("Available locally", "متاحة محليًا"))
                Grade9Action(tr("Library and saved references", "المكتبة والمراجع المحفوظة"), "library", open)
                Grade9Action(tr("Math solver", "الحل الرياضي"), Routes.Solver, open)
                Grade9Action(tr("Graph lab", "مختبر الرسم"), "explore", open)
                Grade9Action(tr("Exercises and availability", "التمارين وحالة التوفر"), Routes.Grade9Exercises, open)
                Grade9Action(tr("My progress", "تقدّمي"), Routes.Progress, open)
                Grade9Action(tr("My achievements and activity", "إنجازاتي ونشاطي"), "g9/activity", open)
                Grade9Action(tr("My gems: ", "جواهري: ") + gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language), "gems/wallet", open)
            }
        }
        }
    }
}

@Composable
fun Grade9SubjectScreen(state: LearningState, geometry: Boolean, open: (String) -> Unit) {
    if (SourceCurricula.forState(state)?.stage != StudyStage.G9) { CatalogUnavailable(); return }
    var query by rememberSaveable { mutableStateOf("") }
    val book = SourceCurricula.forStage(StudyStage.G9).books.single { it.id == if (geometry) "G9-GEO" else "G9-ALG" }
    val units = grade9SubjectUnits(geometry, query)
    val accent = if (geometry) EntryIndigo else EntryTeal

    Grade9Scaffold(if (geometry) "screen-g9-geometry" else "screen-g9-algebra") {
        item("heading") {
            StageHero(
                stage = StudyStage.G9,
                title = book.title,
                subtitle = tr("${book.units.size} units · ${book.researches.size} indexed topics",
                    "${book.units.size} وحدات · ${book.researches.size} بحثًا مفهرسًا"),
                badgeIcon = if (geometry) Icons.Outlined.SquareFoot else Icons.Outlined.Calculate
            )
        }

        item("search") {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                label = { Text(if (geometry) tr("Search geometry", "ابحث في الهندسة") else tr("Search algebra", "ابحث في الجبر")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = accent) },
                singleLine = true
            )
        }

        item("status") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (geometry)
                            tr("Geometry content is under preparation. Browsing the index is not completed learning.", "محتوى الهندسة قيد الإعداد. تصفح الفهرس ليس تعلّمًا مكتملًا.")
                        else
                            tr("An original reduced-fractions pilot is available. The other Algebra topics remain under preparation.", "يتوفر درس أصلي تجريبي للكسور المختزلة. بقية أبحاث الجبر قيد الإعداد."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!geometry) {
                        Button(
                            onClick = { open("lesson/${Grade9Pilot.lessonId}") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Text(tr("Learn reduced fractions", "تعلّم الكسور المختزلة"))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { open(book.route) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(tr("Book details", "تفاصيل الكتاب"), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = { open("progress/book/${book.id}") },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(tr("Book progress", "تقدم الكتاب"), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        if (units.isEmpty()) {
            item("empty") {
                Text(
                    tr("No matching units or topics. Try another title.", "لا وحدات أو أبحاث مطابقة. جرّب عنوانًا آخر."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            units.forEach { unit ->
                item("unit-${unit.id}") {
                    CurriculumUnitCard(unit, open)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReferenceFilter(label: String, selected: String, options: List<Pair<String, String>>, choose: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { (id, title) ->
                DropdownMenuItem(text = { Text(title) }, onClick = { choose(id); expanded = false })
            }
        }
    }
}

@Composable
fun Grade9ExercisesScreen(state: LearningState, open: (String) -> Unit, grade9Study: Grade9StudyRepository? = null) {
    val catalog = SourceCurricula.forState(state)?.takeIf { it.stage == StudyStage.G9 }
    if (catalog == null) { CatalogUnavailable(); return }
    var bookId by rememberSaveable { mutableStateOf(catalog.books.first().id) }
    var unitId by rememberSaveable { mutableStateOf("") }
    var researchId by rememberSaveable { mutableStateOf("") }
    val book = catalog.book(bookId) ?: catalog.books.first()
    val unit = book.units.find { it.id == unitId }
    val research = unit?.researches?.find { it.id == researchId }
    val tealAccent = MaterialTheme.colorScheme.primary

    Grade9Scaffold("screen-g9-exercises") {
        item("title") {
            Grade9Heading(tr("Exercises", "التمارين"), tr("Choose a source topic to check availability", "اختر موضوعًا من المصدر لمعرفة حالة التوفر"))
        }

        // Active practice hero card for Grade 9 student
        item("active-practice") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, tealAccent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(tealAccent.copy(alpha = 0.12f), MaterialTheme.colorScheme.surface)))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = tealAccent) {
                                Text(
                                    tr("Active training", "تدريب نشط"),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                tr("Reduced fractions", "الكسور المختزلة"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            tr("4 interactive practice questions with step-by-step guidance and hints", "4 أسئلة تدريب أصلية مع خطوات الحل وتلميحات ذكية"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { open("g9/practice") },
                                modifier = Modifier.weight(1.3f).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tealAccent)
                            ) {
                                Text(tr("Start practice", "ابدأ التدريب"))
                            }
                            OutlinedButton(
                                onClick = { open("lesson/${Grade9Pilot.lessonId}") },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(tr("Lesson review", "مراجعة الدرس"))
                            }
                        }
                    }
                }
            }
        }

        // Compact Filter Section
        item("filters") {
            Grade9Card {
                Text(tr("Filter by topic", "تصفية حسب الموضوع"), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                // Quick category chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    catalog.books.forEach { b ->
                        FilterChip(
                            selected = bookId == b.id,
                            onClick = { bookId = b.id; unitId = ""; researchId = "" },
                            label = { Text(b.title) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                ReferenceFilter(tr("Unit", "الوحدة"), unit?.title ?: tr("All units", "كل الوحدات"),
                    listOf("" to tr("All units", "كل الوحدات")) + book.units.map { it.id to it.title }) { unitId = it; researchId = "" }
                if (unit != null) {
                    ReferenceFilter(tr("Topic", "البحث"), research?.title ?: tr("All topics", "كل الأبحاث"),
                        listOf("" to tr("All topics", "كل الأبحاث")) + unit.researches.map { it.id to it.title }) { researchId = it }
                }
            }
        }

        // Availability status card
        item("availability") {
            Grade9Card {
                val pilotMatches = book.id == "G9-ALG" && (unit == null || unit.id == "G9-ALG-U01") && (research == null || research.id == Grade9Pilot.lessonId)
                if (pilotMatches) {
                    Text(tr("Reduced fractions · 4 original practice questions", "كسور مختزلة · 4 أسئلة تدريب أصلية"), style = MaterialTheme.typography.titleMedium)
                    Grade9Action(tr("Start or resume practice", "ابدأ التدريب أو استكمله"), "g9/practice", open)
                    Grade9Action(tr("Review the lesson", "راجع الدرس"), "lesson/${Grade9Pilot.lessonId}", open)
                }
                Text(tr("Other exercises are under preparation", "بقية التمارين قيد الإعداد"), style = MaterialTheme.typography.titleMedium)
                Text(
                    tr("Only the bounded EN.MATH pilot is available. Textbook exercise pages and earlier drafts are not a released question bank.",
                        "المتاح هو درس EN.MATH التجريبي المحدد فقط. صفحات تمارين الكتاب والمسودات السابقة ليست بنك أسئلة منشورًا."),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    tr("Quick tests, unit tests and comprehensive exams will become available after content review.",
                        "تتوفر الاختبارات السريعة واختبارات الوحدات والنماذج الشاملة بعد مراجعة المحتوى."),
                    style = MaterialTheme.typography.bodySmall
                )
                Grade9Action(tr("Browse selected reference", "تصفح المرجع المحدد"), research?.route ?: unit?.route ?: book.route, open)
                Grade9Action(tr("Source and review status", "المصدر وحالة المراجعة"), Routes.Grade9Source, open)
            }
        }

        // Mistakes Review Card
        item("mistakes") {
            Grade9Card {
                Text(tr("My mistakes", "دفتر أخطائي"), style = MaterialTheme.typography.titleMedium)
                Text(tr("Review answers from your actual saved pilot attempts.", "راجع إجابات محاولاتك التجريبية المحفوظة فعليًا."), style = MaterialTheme.typography.bodySmall)
                Grade9Action(tr("Open mistake review", "فتح مراجعة الأخطاء"), "review", open)
                Grade9Action(tr("Plan my next step", "خطّط لخطوتي التالية"), "planner", open)
            }
        }

        // Evidence section
        item("pilot-evidence") {
            Grade9EvidenceSection(state, grade9Study, open)
        }
    }
}

@Composable
fun Grade9SourceScreen() {
    val catalog = SourceCurricula.forStage(StudyStage.G9)
    Grade9Scaffold("screen-g9-source") {
        item {
            Grade9Heading(tr("Source and availability", "المصدر والتوفر"), tr("An index is a guide, not a published lesson", "الفهرس دليل وليس درسًا منشورًا"))
        }
        items(catalog.books, key = { it.id }) { book ->
            Grade9Card {
                Text(book.title, style = MaterialTheme.typography.titleLarge)
                Text(tr("Source edition ${book.edition} · ${book.pages} PDF pages", "طبعة المصدر ${book.edition} · ${book.pages} صفحة PDF"))
                Text(tr("${book.units.size} units · ${book.researches.size} indexed topics", "${book.units.size} وحدات · ${book.researches.size} بحثًا مفهرسًا"))
            }
        }
        item {
            Grade9Card {
                Text(tr("One original EN.MATH pilot: reduced fractions, with source and automated mathematical checks. Independent educator review is still recommended. This is not official year-alignment certification.",
                    "درس EN.MATH أصلي تجريبي واحد: الكسور المختزلة، مع تحقق المصدر واختبارات رياضية آلية. ما زالت مراجعة تربوية مستقلة مستحسنة. هذا ليس اعتمادًا رسميًا لمطابقة السنة الدراسية."))
                Text(tr("Under review", "قيد المراجعة"), style = MaterialTheme.typography.titleLarge)
                Text(tr("The source analyzed is 2025–2026. Alignment with 2026–2027 has not been verified.", "المصدر المحلل هو 2025–2026. لم تُثبت المطابقة مع 2026–2027."))
                Text(tr("Explanations, worked examples, diagrams, questions and solutions need mathematical and educational review. Source indexing does not grant publishing rights; permissions must be checked before distributing files.",
                    "تحتاج الشروحات والأمثلة والرسوم والأسئلة والحلول مراجعة رياضية وتربوية. فهرسة المصدر لا تمنح حق النشر؛ يجب فحص الإذن قبل توزيع الملفات."))
                Text(tr("Browsing, saving references and completing manual plan goals do not award mastery or gems.", "التصفح وحفظ المراجع وإنجاز أهداف الخطة يدويًا لا تمنح إتقانًا أو جواهر."))
            }
        }
    }
}

@Composable
fun Grade9HelpScreen(open: (String) -> Unit) {
    Grade9Scaffold("screen-g9-help") {
        item {
            Grade9Heading(tr("Your Grade 9 guide", "دليلك في التاسع"), tr("One step at a time", "خطوة بخطوة"))
        }
        item {
            Grade9Card {
                Text(tr("Where do I start?", "من أين أبدأ؟"), style = MaterialTheme.typography.titleLarge)
                Text(tr("Explore the path on Home or choose Algebra or Geometry. Open a topic to see its source pages, save it to your library or add it to your plan.",
                    "تصفح المسار في الرئيسية أو اختر الجبر أو الهندسة. افتح البحث لمعرفة صفحات المصدر وحفظه في مكتبتك أو إضافته إلى خطتك."))
                Text(tr("What does under review mean?", "ماذا تعني قيد المراجعة؟"), style = MaterialTheme.typography.titleMedium)
                Text(tr("The title is indexed. A checked explanation and exercise set are not yet available. It is not counted as completed learning.",
                    "العنوان مفهرس، لكن الشرح والتمارين المعتمدة غير متاحة بعد. لا يُحسب هذا تعلّمًا مكتملًا."))
                Text(tr("Where are my data?", "أين تُحفظ بياناتي؟"), style = MaterialTheme.typography.titleMedium)
                Text(tr("Your profile, plan and saved work stay on this device in a separate local file. There is no cloud sync. Switch files from Profile; keep app data to retain your work.",
                    "ملفك وخطتك وأعمالك المحفوظة تبقى على هذا الجهاز في ملف محلي مستقل. لا توجد مزامنة سحابية. بدّل الملف من حسابي، واحتفظ ببيانات التطبيق لحفظ عملك."))
                Grade9Action(tr("My study plan", "خطتي الدراسية"), "planner", open)
                Grade9Action(tr("Source and availability", "المصدر والتوفر"), Routes.Grade9Source, open)
            }
        }
    }
}
