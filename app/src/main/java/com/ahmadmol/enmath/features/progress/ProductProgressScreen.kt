package com.ahmadmol.enmath.features.progress

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun ProductProgressScreen(state: LearningState) {
    val stats = LearningRules.stats(state)
    val topics = state.books.flatMap { it.topics }
    ScreenPage(Modifier.testTag("screen-progress")) {
        PageHeading(label(TextKey.t_cde1f4644a), label(TextKey.t_7512420977))
        StreakBadge(stats.streak)
        ProgressCard(
            label(TextKey.t_465bb41873),
            "${state.completed.size}/${topics.sumOf {it.lessons.size}}",
            stats.lessonFraction,
        )
        AdaptivePair(
            first = {
                AppCard {
                    Text(label(TextKey.t_d61fdf036d))
                    Text("${stats.exercises}", style = MaterialTheme.typography.headlineMedium)
                }
            },
            second = {
                AppCard {
                    Text(label(TextKey.t_a9a723c334))
                    Text(label(TextKey.t_516f1fe3b4, stats.minutes))
                    Text(label(TextKey.t_e317f22089, (stats.accuracy * 100).toInt()))
                }
            },
        )
        SectionTitle(label(TextKey.t_f9676588a1))
        val max = stats.week.maxOrNull()?.coerceAtLeast(1) ?: 1
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
            verticalAlignment = androidx.compose.ui.Alignment.Bottom,
        ) {
            stats.week.forEachIndexed { i, n ->
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(Space.chart * (n.toFloat() / max).coerceAtLeast(.03f)),
                    ) {}
                    Text("${i+1}")
                    Text("$n")
                }
            }
        }
        DemoNotice(label(TextKey.t_8559a2cbe7))
        SectionTitle(label(TextKey.t_c5e1bee1ed))
        topics.forEach { t ->
            AppCard {
                Text(t.title.text())
                MasteryBadge(LearningRules.topic(state, t.id))
            }
        }
        SectionTitle(label(TextKey.t_f0ccbc38a5))
        val strengths = topics.filter { LearningRules.topic(state, it.id).score >= 70 }
        if (strengths.isEmpty()) Text(label(TextKey.t_66ca730c92))
        else strengths.forEach { Text(it.title.text()) }
        SectionTitle(label(TextKey.t_6f03a69ee9))
        topics
            .filter { LearningRules.topic(state, it.id).score < 45 }
            .take(3)
            .forEach { Text(it.title.text()) }
        SectionTitle(label(TextKey.t_e950f5cb20))
        Text(
            if (state.completed.isNotEmpty()) label(TextKey.t_ad7db392b8)
            else label(TextKey.t_b48bb99c87)
        )
        if (stats.streak >= 3) Text(label(TextKey.t_52ae618c18))
        if (state.product.attempts.any { it.accuracy >= .8f }) Text(label(TextKey.t_bac100df7d))
    }
}
