package com.ahmadmol.enmath.features.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

@Composable
fun SearchScreen(state: LearningState, onSearch: (String) -> Unit, onRoute: (String) -> Unit) {
    var query by rememberSaveable(state.activeStudentId) { mutableStateOf("") }
    val term = LearningRules.search(query)
    val ar = state.language == com.ahmadmol.enmath.core.model.Language.Arabic

    ScreenPage(Modifier.imePadding().testTag("screen-search")) {
        // Hero Card matching Image 3
        RefHeroCard(
            title = tr("Search smart and learn more.", "ابحث بذكاء وتعلم أكثر."),
            subtitle = tr("Search through lessons, summaries, problems, and everything you need in mathematics.", "ابحث في الدروس والملخصات والمسائل وكل ما تحتاجه في مادة الرياضيات."),
            badgeText = tr("Discover fast", "اكتشف بسرعة"),
            badgeColor = RefColors.LightBlueHeroBgStart,
            badgeTextColor = RefColors.PrimaryBlue,
            artwork = { SearchHeroArtwork() }
        )

        // Search Input Bar matching Image 3
        RefSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = tr("Search", "بحث")
        )

        // Save Search Action Button matching Image 3
        RefSecondaryButton(
            text = label(TextKey.t_00965319c2),
            onClick = { onSearch(query) },
            icon = Icons.Outlined.BookmarkBorder,
            enabled = term.isNotBlank(),
            modifier = Modifier.testTag("save-search"),
        )

        if (term.isBlank()) {
            // Recent Searches Section matching Image 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = RefColors.TextPrimary
                )
                Text(
                    text = label(TextKey.t_b67d86605d),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RefColors.TextPrimary
                )
            }

            state.product.searches.forEach { savedQuery ->
                RefSecondaryButton(text = savedQuery,
                    onClick = { query = savedQuery; onSearch(savedQuery) },
                    icon = Icons.Outlined.Schedule,
                    modifier = Modifier.testTag("recent-search-$savedQuery"))
            }
            if (state.product.searches.isEmpty()) {
                Text(tr("Your saved searches will appear here.", "ستظهر عمليات البحث المحفوظة هنا."),
                    color = RefColors.TextSecondary)
            }

            // Info / Tip Banner matching Image 3
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, RefColors.CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Space.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Text(
                            text = tr("Continue from where you left off.", "تابع من حيث توقفت."),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RefColors.TextPrimary
                        )
                        Text(
                            text = tr("Recent searches are shown here to help you quickly access what you care about.", "تعرض هنا عمليات البحث الأخيرة لتساعدك على الوصول السريع إلى ما تهتم به."),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = RefColors.TextSecondary
                        )
                    }

                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = RefColors.LightBlueHeroBgStart
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = RefColors.PrimaryBlue
                            )
                        }
                    }
                }
            }

            return@ScreenPage
        }

        fun match(en: String, ar: String) = LearningRules.search(en + " " + ar).contains(term)
        val topics = state.books.flatMap { it.topics }.filter { match(it.title.en, it.title.ar) }
        val lessons = state.books.flatMap { it.lessons }.filter { match(it.title.en, it.title.ar) }
        val concepts = state.books.flatMap { it.lessons }.filter { match(it.rule.en, it.rule.ar) }
        val questions = state.questions.filter { StudentCatalog.practiceForQuestion(state, it.id) != null }.filter { match(it.prompt.en + it.expression, it.prompt.ar + it.topicId) }

        if (topics.isEmpty() && lessons.isEmpty() && concepts.isEmpty() && questions.isEmpty()) {
            RefEmptyState(
                title = label(TextKey.t_b8ee1e0f8c),
                subtitle = label(TextKey.t_95644465fb),
                artwork = { SearchHeroArtwork() }
            )
        }

        if (topics.isNotEmpty()) {
            SectionTitle(label(TextKey.t_519b0bc526))
            topics.forEach { t ->
                TopicCard(t, StudentCatalog.completed(state)) {
                    onSearch(query)
                    onRoute("topic/${t.id}")
                }
            }
        }
        if (lessons.isNotEmpty()) {
            SectionTitle(label(TextKey.t_6c48dfb4c3))
            lessons.forEach { l ->
                LessonCard(l, l.id in StudentCatalog.completed(state)) {
                    onSearch(query)
                    onRoute("lesson/${l.id}")
                }
            }
        }
        if (concepts.isNotEmpty()) {
            SectionTitle(label(TextKey.t_bdb85c717d))
            concepts.take(8).forEach { l ->
                Card(
                    onClick = { onSearch(query); onRoute("lesson/${l.id}") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, RefColors.CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(Space.lg),
                        verticalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Text(l.title.text(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(l.rule.text(), style = MaterialTheme.typography.bodyMedium, color = RefColors.TextSecondary)
                    }
                }
            }
        }
        if (questions.isNotEmpty()) {
            SectionTitle(label(TextKey.t_353becf694))
            questions.take(8).forEach { q ->
                Card(
                    onClick = { onSearch(query); onRoute("practice/${StudentCatalog.practiceForQuestion(state, q.id)?.id}") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, RefColors.CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(Space.lg),
                        verticalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        MathText(q.expression)
                        RefSecondaryButton(
                            text = label(TextKey.t_0991118449),
                            onClick = { onSearch(query); onRoute("practice/${StudentCatalog.practiceForQuestion(state, q.id)?.id}") }
                        )
                    }
                }
            }
        }
    }
}
