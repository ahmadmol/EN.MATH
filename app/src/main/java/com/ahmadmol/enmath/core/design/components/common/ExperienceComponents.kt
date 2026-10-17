package com.ahmadmol.enmath.core.design.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.tokens.ExperienceTokens

@Composable
fun ExperiencePage(
    tag: String,
    teacher: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize().testTag(tag), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(
                    max = if (teacher) ExperienceTokens.teacherMax else ExperienceTokens.studentMax
                )
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(ExperienceTokens.inset),
            verticalArrangement = Arrangement.spacedBy(ExperienceTokens.gap),
            content = content,
        )
    }
}

@Composable
fun ExperiencePanel(
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(modifier, tonal = emphasized, content = content)
}

@Composable
fun ExperienceSection(title: String, subtitle: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle.isNotEmpty())
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
}

/** Native short equations; bidi direction is isolated from surrounding Arabic. */
@Composable
fun MathLine(value: String, modifier: Modifier = Modifier, large: Boolean = true) {
    Box(modifier.fillMaxWidth()) {
        MathText(
            value,
            if (large) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun DetailRow(title: String, detail: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().semantics(mergeDescendants = true) {}.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            detail,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
