package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-performance Liquid Glass styling that provides a translucent, frosted glass appearance
 * with specular gradient borders, soft ambient lighting, and high contrast text readability.
 * Compatible with all supported Android versions without requiring hardware-specific blur shaders.
 */
@Composable
fun liquidGlassBackgroundBrush(dark: Boolean = isDarkTheme(), alpha: Float = 0.72f): Brush {
    return if (dark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E293B).copy(alpha = alpha),
                Color(0xFF0F172A).copy(alpha = (alpha * 0.95f).coerceAtMost(1f))
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color(0xFFF1F5F9).copy(alpha = (alpha * 0.85f).coerceAtMost(1f))
            )
        )
    }
}

@Composable
fun liquidGlassBorderBrush(dark: Boolean = isDarkTheme()): Brush {
    return if (dark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF64748B).copy(alpha = 0.55f),
                Color(0xFF334155).copy(alpha = 0.20f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                Color(0xFFCBD5E1).copy(alpha = 0.45f)
            )
        )
    }
}

/**
 * Modifier extension to apply the Liquid Glass effect to any composable.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = CircleShape,
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.72f,
    elevation: Dp = 4.dp
): Modifier {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgBrush = liquidGlassBackgroundBrush(dark, alpha)
    val borderBrush = liquidGlassBorderBrush(dark)
    val shadowColor = if (dark) Color.Black.copy(alpha = 0.35f) else Color(0xFF0066FF).copy(alpha = 0.08f)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .background(brush = bgBrush, shape = shape)
        .border(width = borderWidth, brush = borderBrush, shape = shape)
        .clip(shape)
}

/**
 * Reusable Liquid Glass Surface Container.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.72f,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .liquidGlass(shape = shape, borderWidth = borderWidth, alpha = alpha, elevation = elevation)
            .then(clickModifier),
        contentAlignment = Alignment.Center,
        content = content
    )
}
