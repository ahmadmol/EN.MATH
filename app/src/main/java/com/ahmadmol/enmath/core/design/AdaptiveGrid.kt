package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> AdaptiveGrid(items: List<T>, content: @Composable (T) -> Unit) {
    BoxWithConstraints {
        val wide = maxWidth >= 600.dp
        Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
            if (wide)
                items.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.lg)) {
                        Column(Modifier.weight(1f)) { content(pair[0]) }
                        Column(Modifier.weight(1f)) { pair.getOrNull(1)?.let { content(it) } }
                    }
                }
            else items.forEach { content(it) }
        }
    }
}
