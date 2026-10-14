package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Native vector artwork: no screen images, remote assets, or mathematical data. */
@Composable
fun BookLogo(modifier: Modifier = Modifier) {
    val blue = MaterialTheme.colorScheme.primary
    val teal = MaterialTheme.colorScheme.secondary
    Canvas(modifier.size(72.dp)) {
        val w = size.width
        val h = size.height
        fun leaf(left: Boolean): Path =
            Path().apply {
                val direction = if (left) -1 else 1
                moveTo(w * .5f, h * .82f)
                cubicTo(
                    w * (.5f + direction * .12f),
                    h * .62f,
                    w * (.5f + direction * .36f),
                    h * .7f,
                    w * (.5f + direction * .39f),
                    h * .58f,
                )
                lineTo(w * (.5f + direction * .39f), h * .19f)
                cubicTo(
                    w * (.5f + direction * .29f),
                    h * .18f,
                    w * (.5f + direction * .13f),
                    h * .26f,
                    w * .5f,
                    h * .39f,
                )
                close()
            }
        drawPath(leaf(true), Brush.linearGradient(listOf(blue.copy(alpha = .45f), blue)))
        drawPath(leaf(false), Brush.linearGradient(listOf(teal, blue)))
        drawLine(blue, Offset(w * .5f, h * .4f), Offset(w * .5f, h * .86f), w * .035f)
        repeat(3) { i ->
            val x = w * (.59f + i * .1f)
            drawLine(teal, Offset(x, h * (.32f - i * .08f)), Offset(x, h * .55f), w * .05f)
        }
    }
}

@Composable
fun LearningArtwork(symbol: String, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier.fillMaxWidth().height(144.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val left = size.width * .15f
            val right = size.width * .85f
            val mid = size.height * .58f
            repeat(7) { i ->
                val x = left + (right - left) * i / 6
                drawLine(outline.copy(alpha = .5f), Offset(x, 20f), Offset(x, size.height - 20f))
            }
            drawLine(primary.copy(alpha = .5f), Offset(left, mid), Offset(right, mid), 2.dp.toPx())
            val path =
                Path().apply {
                    moveTo(left, mid + 20.dp.toPx())
                    cubicTo(
                        size.width * .34f,
                        -10f,
                        size.width * .44f,
                        size.height + 30f,
                        right,
                        mid - 35.dp.toPx(),
                    )
                }
            drawPath(path, secondary, style = Stroke(3.dp.toPx()))
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .9f),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                symbol,
                Modifier.padding(24.dp),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
