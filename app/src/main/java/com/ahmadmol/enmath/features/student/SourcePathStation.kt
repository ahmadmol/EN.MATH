package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.design.*

/** Indexed source stations are descriptive, never a test/completion/reward action. */
@Composable fun SourcePathStation(order: Int, title: String, lessons: List<String>, bac: Boolean) {
    val accent = if(bac) EntryIndigo else EntryTeal
    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Surface(shape=CircleShape, color=accent.copy(alpha=.15f), modifier=Modifier.size(48.dp)) {
                Box(contentAlignment=Alignment.Center) { Text("$order", color=entryInk(), style=MaterialTheme.typography.titleMedium) }
            }
            Box(Modifier.width(3.dp).height((60+lessons.size*18).dp).background(accent.copy(alpha=.18f)))
        }
        Card(Modifier.weight(1f).padding(bottom=12.dp), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.07f))) {
            Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(title, style=MaterialTheme.typography.titleMedium, color=entryInk())
                Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_43), color=MaterialTheme.colorScheme.onSurfaceVariant, style=MaterialTheme.typography.labelLarge)
                lessons.forEach { Text(it, style=MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}
