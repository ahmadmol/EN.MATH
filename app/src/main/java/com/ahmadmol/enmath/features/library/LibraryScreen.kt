package com.ahmadmol.enmath.features.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

import androidx.compose.material.icons.automirrored.outlined.MenuBook

private data class FilterTabItem(val index: Int, val title: String, val icon: ImageVector)

@Composable
fun LibraryScreen(state: LearningState, onLesson: (String) -> Unit, onProblem: (String) -> Unit) {
    var tab by rememberSaveable(state.user?.localId, state.generation) { mutableIntStateOf(0) }
    var query by rememberSaveable(state.user?.localId, state.generation) { mutableStateOf("") }
    var topic by rememberSaveable(state.user?.localId, state.generation) { mutableIntStateOf(0) }
    var resourceType by rememberSaveable(state.user?.localId, state.generation) { mutableIntStateOf(0) }
    val topics = state.books.flatMap { it.topics }
    val normalized = LearningRules.search(query)

    ScreenPage(Modifier.testTag("screen-library")) {
        // Hero Section matching Image 1
        RefHeroCard(
            title = tr("Keep useful ideas saved.", "احتفظ بالأفكار المفيدة."),
            subtitle = tr("Return anytime and continue learning at your pace.", "ارجع إليها في أي وقت وواصل تعلمك بأسلوبك."),
            badgeText = tr("Your Library", "مكتبتك"),
            badgeColor = RefColors.LightBlueHeroBgStart,
            badgeTextColor = RefColors.PrimaryBlue,
            artwork = { LibraryHeroArtwork() }
        )

        // Search Input matching Image 1
        RefSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = tr("Search", "بحث")
        )

        // Main Category Tabs (Lessons, Problems, Teacher Resources) matching Image 1
        val categoryTabs = listOf(
            RefTabItem("lessons", label(TextKey.t_6c48dfb4c3), Icons.AutoMirrored.Outlined.MenuBook),
            RefTabItem("problems", label(TextKey.t_10881e833c), Icons.Outlined.Calculate),
            RefTabItem("resources", label(TextKey.t_4ae85809f8), Icons.Outlined.School)
        )
        val selectedCategory = when (tab) {
            1 -> "problems"
            2 -> "resources"
            else -> "lessons"
        }

        RefCategoryTabs(
            items = categoryTabs,
            selectedId = selectedCategory,
            onSelect = { selectedId ->
                tab = when (selectedId) {
                    "problems" -> 1
                    "resources" -> 2
                    else -> 0
                }
            }
        )

        // Topic Chips Row matching Image 1
        val topicChips = listOf(
            RefTabItem("all", label(TextKey.t_dd0a8d9c19), Icons.Outlined.GridView)
        ) + topics.map { t ->
            val icon = when {
                t.symbol.contains("∞") || t.id.contains("limits") -> Icons.Outlined.AllInclusive
                t.symbol.contains("∫") || t.id.contains("integration") -> Icons.Outlined.Functions
                else -> Icons.Outlined.Functions
            }
            RefTabItem(t.id, t.title.text(), icon)
        }
        val selectedTopicId = if (topic == 0) "all" else topics.getOrNull(topic - 1)?.id ?: "all"

        RefFilterChipRow(
            items = topicChips,
            selectedId = selectedTopicId,
            onSelect = { selectedId ->
                topic = if (selectedId == "all") 0 else (topics.indexOfFirst { it.id == selectedId } + 1).coerceAtLeast(0)
            },
            useMintGreen = true
        )

        var count = 0

        // Lessons Tab
        if (tab == 0) {
            val lessons = state.books
                .flatMap { it.lessons }
                .filter {
                    it.id in state.product.savedLessons &&
                        LearningRules.search(it.title.en + it.title.ar).contains(normalized) &&
                        (topic == 0 || state.topicOf(it.id)?.id == topics.getOrNull(topic - 1)?.id)
                }

            lessons.forEach { l ->
                count++
                val isDone = l.id in StudentCatalog.completed(state)
                Card(
                    onClick = { onLesson(l.id) },
                    modifier = Modifier.fillMaxWidth().testTag("lesson-${l.id}"),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Space.lg),
                        verticalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            LibraryLessonThumbnail()

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(Space.xs)
                            ) {
                                Text(
                                    text = l.title.text(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = Space.sm, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = label(TextKey.t_516f1fe3b4, l.minutes),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = l.rule.text(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = Int.MAX_VALUE
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Space.sm)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isDone) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = label(TextKey.t_8891e609f6),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                onClick = { onLesson(l.id) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = l.title.text(),
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Problems Tab
        if (tab == 1) {
            state.product.history
                .filter {
                    it.id in state.product.savedProblems &&
                        LearningRules.search(it.expression).contains(normalized) &&
                        (topic == 0 || it.topicId == topics.getOrNull(topic - 1)?.id.orEmpty())
                }
                .forEach { h ->
                    count++
                    Card(onClick = { onProblem(h.id) }, modifier = Modifier.fillMaxWidth().testTag("library-problem-${h.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, RefColors.CardBorder)) {
                        Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
                            Text(state.topic(h.topicId)?.title?.text() ?: tr("Math", "رياضيات"),
                                style = MaterialTheme.typography.titleMedium, color = RefColors.TextPrimary)
                            EquationCard(h.expression)
                            RefSecondaryButton(label(TextKey.t_0bbe6c9c93), { onProblem(h.id) })
                        }
                    }
                }
        }

        // Teacher Resources Tab matching Image 1
        if (tab == 2) {
            val resourceKinds = listOf(
                RefTabItem("all_kinds", label(TextKey.t_d9742902f4), Icons.Outlined.GridView)
            ) + com.ahmadmol.enmath.core.model.ResourceKind.entries.mapIndexed { idx, k ->
                RefTabItem("kind_$idx", resourceKindLabel(k), Icons.Outlined.Description)
            }

            RefFilterChipRow(
                items = resourceKinds,
                selectedId = if (resourceType == 0) "all_kinds" else "kind_${resourceType - 1}",
                onSelect = { selectedId ->
                    resourceType = if (selectedId == "all_kinds") 0
                    else (selectedId.removePrefix("kind_").toIntOrNull() ?: 0) + 1
                },
                useMintGreen = true
            )

            state.product.resources
                .filter { r ->
                    r.classIds.any { c ->
                        state.product.classes.any { it.id == c && state.activeStudentId in it.studentIds }
                    } &&
                        LearningRules.search(r.title.en + r.title.ar).contains(normalized) &&
                        (topic == 0 || r.topicId == topics.getOrNull(topic - 1)?.id.orEmpty()) &&
                        (resourceType == 0 ||
                            r.kind ==
                                com.ahmadmol.enmath.core.model.ResourceKind.entries[
                                        resourceType - 1])
                }
                .forEach { r ->
                    count++
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("resource-item-${r.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, RefColors.CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Space.lg),
                            verticalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = RefColors.LightBlueHeroBgStart
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp),
                                            tint = RefColors.PrimaryBlue
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = Space.md),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = r.title.text(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RefColors.TextPrimary
                                    )
                                    Text(
                                        text = resourceKindLabel(r.kind),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RefColors.TextSecondary
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = RefColors.FormulaBoxBg,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = RefColors.TextMuted
                                        )
                                    }
                                }
                            }

                            if (r.body.text().isNotBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = RefColors.FormulaBoxBg,
                                    border = BorderStroke(1.dp, RefColors.CardBorder.copy(alpha = 0.5f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Space.md)
                                    ) {
                                        MathText(r.body.text())
                                    }
                                }
                            }
                        }
                    }
                }
        }

        // Empty State matching Image 3
        if (count == 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.sm),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.xxl, horizontal = Space.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    AssignmentsEmptyArtwork(Modifier.size(130.dp))

                    Text(
                        text = tr("No saved items here", "لا توجد عناصر محفوظة هنا"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = tr("Save lessons or problems from study pages to review later.", "قم بحفظ الدروس أو المسائل من صفحات الشرح والممارسة للرجوع إليها لاحقًا."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(Space.xs))

                    Surface(
                        onClick = {
                            query = ""
                            topic = 0
                            resourceType = 0
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = tr("Reset search", "إعادة المحاولة"),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun resourceKindLabel(kind: com.ahmadmol.enmath.core.model.ResourceKind) =
    when (kind) {
        com.ahmadmol.enmath.core.model.ResourceKind.LessonNote -> label(TextKey.t_d9613968c1)
        com.ahmadmol.enmath.core.model.ResourceKind.FormulaSheet -> label(TextKey.t_7722c61c7e)
        com.ahmadmol.enmath.core.model.ResourceKind.PracticeSet -> label(TextKey.t_3b45300a1e)
        else -> label(TextKey.t_7a5643ef0a)
    }
