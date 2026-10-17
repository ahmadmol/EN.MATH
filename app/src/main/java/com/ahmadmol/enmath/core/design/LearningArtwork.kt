package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

@Composable
fun OutcomeMedallion(success: Boolean) {
    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color =
                if (success) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer,
        ) {
            Icon(
                if (success) Icons.Outlined.CheckCircle else Icons.Outlined.Refresh,
                null,
                Modifier.padding(20.dp).size(64.dp),
                tint =
                    if (success) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

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

/**
 * Book, pages and geometry are decorative native paths; no generated equations are used as data.
 */
@Composable
fun LearningArtwork(symbol: String, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val paper = MaterialTheme.colorScheme.surfaceContainerLowest
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier.fillMaxWidth().height(144.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun face(vararg points: Pair<Float, Float>): Path =
                Path().apply {
                    points.forEachIndexed { i, p ->
                        if (i == 0) moveTo(w * p.first, h * p.second)
                        else lineTo(w * p.first, h * p.second)
                    }
                    close()
                }
            drawOval(
                primary.copy(alpha = .08f),
                topLeft = Offset(w * .13f, h * .78f),
                size = androidx.compose.ui.geometry.Size(w * .73f, h * .15f),
            )
            drawPath(face(.29f to .2f, .63f to .08f, .72f to .15f, .37f to .28f), paper)
            drawPath(
                face(.29f to .2f, .37f to .28f, .37f to .85f, .29f to .77f),
                primary.copy(alpha = .6f),
            )
            drawPath(
                face(.37f to .28f, .72f to .15f, .72f to .73f, .37f to .85f),
                Brush.linearGradient(
                    listOf(primary, tertiary),
                    Offset(w * .4f, h * .2f),
                    Offset(w * .7f, h * .8f),
                ),
            )
            drawLine(
                paper.copy(alpha = .5f),
                Offset(w * .4f, h * .3f),
                Offset(w * .4f, h * .78f),
                1.dp.toPx(),
            )
            drawPath(
                face(.15f to .65f, .26f to .6f, .33f to .67f, .22f to .73f),
                secondary.copy(alpha = .7f),
            )
            drawPath(
                face(.15f to .65f, .22f to .73f, .22f to .86f, .15f to .78f),
                secondary.copy(alpha = .5f),
            )
            drawPath(face(.22f to .73f, .33f to .67f, .33f to .8f, .22f to .86f), secondary)
            drawCircle(
                Brush.radialGradient(
                    listOf(paper, tertiary),
                    center = Offset(w * .82f, h * .64f),
                    radius = h * .15f,
                ),
                radius = h * .12f,
                center = Offset(w * .8f, h * .67f),
            )
        }
        Text(
            symbol,
            Modifier.padding(start = Space.lg),
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                ),
            color = onPrimary,
        )
    }
}

/** Shared native emblem for entry/recovery screens; no bitmap or remote resource. */
@Composable
fun FeatureEmblem(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Icon(
                icon,
                null,
                Modifier.padding(Space.lg).size(Space.hero),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun EntryWaves(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path =
            Path().apply {
                moveTo(0f, h * .08f)
                cubicTo(w * .3f, h * .3f, w * .65f, -h * .1f, w, h * .04f)
                lineTo(w, h * .3f)
                cubicTo(w * .7f, h * .18f, w * .3f, h * .6f, 0f, h * .38f)
                close()
            }
        drawPath(
            path,
            Brush.verticalGradient(listOf(primary.copy(alpha = .10f), primary.copy(alpha = .02f))),
        )
    }
}
