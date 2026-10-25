package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

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

/** Official EN.MATH MN Brand Logo Mark (Continuous Ribbon Style with Electric Blue & Cyan Gradients) */
@Composable
fun MnLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier.size(72.dp)) {
        val w = size.width
        val h = size.height

        // Radial ambient glow behind symbol
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00D2FF).copy(alpha = 0.35f),
                    Color(0xFF0052D4).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Path 1: Left Vertical Pillar of M
        val mLeft = Path().apply {
            moveTo(w * 0.18f, h * 0.28f)
            lineTo(w * 0.30f, h * 0.28f)
            lineTo(w * 0.30f, h * 0.78f)
            lineTo(w * 0.18f, h * 0.78f)
            close()
        }
        drawPath(
            mLeft,
            Brush.linearGradient(
                listOf(Color(0xFF00E5FF), Color(0xFF0066FF)),
                start = Offset(w * 0.18f, h * 0.28f),
                end = Offset(w * 0.30f, h * 0.78f)
            )
        )

        // Path 2: Center V Left Stroke
        val mV = Path().apply {
            moveTo(w * 0.30f, h * 0.28f)
            lineTo(w * 0.46f, h * 0.58f)
            lineTo(w * 0.54f, h * 0.58f)
            lineTo(w * 0.30f, h * 0.28f)
            close()
        }
        drawPath(
            mV,
            Brush.linearGradient(
                listOf(Color(0xFF00D2FF), Color(0xFF0052D4)),
                start = Offset(w * 0.30f, h * 0.28f),
                end = Offset(w * 0.54f, h * 0.58f)
            )
        )

        // Path 3: Center Stem
        val mStem = Path().apply {
            moveTo(w * 0.46f, h * 0.46f)
            lineTo(w * 0.56f, h * 0.46f)
            lineTo(w * 0.56f, h * 0.78f)
            lineTo(w * 0.46f, h * 0.78f)
            close()
        }
        drawPath(
            mStem,
            Brush.linearGradient(
                listOf(Color(0xFF00B0FF), Color(0xFF0040C4)),
                start = Offset(w * 0.46f, h * 0.46f),
                end = Offset(w * 0.56f, h * 0.78f)
            )
        )

        // Path 4: Intertwining Ribbon Loop (Bridging M and N)
        val loop = Path().apply {
            moveTo(w * 0.46f, h * 0.58f)
            cubicTo(
                w * 0.52f, h * 0.40f,
                w * 0.62f, h * 0.24f,
                w * 0.73f, h * 0.28f
            )
            cubicTo(
                w * 0.82f, h * 0.31f,
                w * 0.78f, h * 0.52f,
                w * 0.84f, h * 0.78f
            )
            lineTo(w * 0.72f, h * 0.78f)
            cubicTo(
                w * 0.66f, h * 0.54f,
                w * 0.70f, h * 0.40f,
                w * 0.62f, h * 0.36f
            )
            cubicTo(
                w * 0.56f, h * 0.33f,
                w * 0.48f, h * 0.46f,
                w * 0.46f, h * 0.58f
            )
            close()
        }
        drawPath(
            loop,
            Brush.linearGradient(
                listOf(
                    Color(0xFF80EEFF),
                    Color(0xFF00D2FF),
                    Color(0xFF0066FF),
                    Color(0xFF0038A8)
                ),
                start = Offset(w * 0.46f, h * 0.24f),
                end = Offset(w * 0.84f, h * 0.78f)
            )
        )

        // Path 5: N Right Pillar
        val nRight = Path().apply {
            moveTo(w * 0.72f, h * 0.28f)
            lineTo(w * 0.84f, h * 0.28f)
            lineTo(w * 0.84f, h * 0.78f)
            lineTo(w * 0.72f, h * 0.78f)
            close()
        }
        drawPath(
            nRight,
            Brush.linearGradient(
                listOf(Color(0xFF00D2FF), Color(0xFF0052D4)),
                start = Offset(w * 0.72f, h * 0.28f),
                end = Offset(w * 0.84f, h * 0.78f)
            )
        )
    }
}

/** Full EN.MATH Brand Logo (MN Ribbon Mark + Spaced EN.MATH Subtitle) */
@Composable
fun MnLogoFull(modifier: Modifier = Modifier, showText: Boolean = true) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MnLogoMark(modifier = Modifier.size(96.dp))
        if (showText) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "E N . M A T H",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.25.em
                ),
                color = Color.White
            )
        }
    }
}

/** Legacy & shared Logo composable, delegating to official MN Logo Mark */
@Composable
fun BookLogo(modifier: Modifier = Modifier) {
    MnLogoMark(modifier = modifier)
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

@Composable
fun AssignmentsHeroArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.size(110.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFFE0F2FE),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        val paper = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(w * 0.28f, h * 0.18f, w * 0.82f, h * 0.82f),
                    cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
                )
            )
        }
        drawPath(paper, Color.White)
        drawPath(
            paper,
            Color(0xFF3B82F6).copy(alpha = 0.2f),
            style = Stroke(width = 2.dp.toPx())
        )

        val lineY1 = h * 0.35f
        val lineY2 = h * 0.50f
        val lineY3 = h * 0.65f

        drawCircle(Color(0xFF3B82F6), radius = w * 0.03f, center = Offset(w * 0.38f, lineY1))
        drawLine(
            Color(0xFF93C5FD),
            start = Offset(w * 0.46f, lineY1),
            end = Offset(w * 0.72f, lineY1),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(Color(0xFF3B82F6), radius = w * 0.03f, center = Offset(w * 0.38f, lineY2))
        drawLine(
            Color(0xFF93C5FD),
            start = Offset(w * 0.46f, lineY2),
            end = Offset(w * 0.72f, lineY2),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(Color(0xFF3B82F6), radius = w * 0.03f, center = Offset(w * 0.38f, lineY3))
        drawLine(
            Color(0xFF93C5FD),
            start = Offset(w * 0.46f, lineY3),
            end = Offset(w * 0.65f, lineY3),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        val badgeCenter = Offset(w * 0.25f, h * 0.62f)
        drawCircle(Color(0xFF2563EB), radius = w * 0.18f, center = badgeCenter)

        val cap = Path().apply {
            moveTo(badgeCenter.x - w * 0.10f, badgeCenter.y - h * 0.02f)
            lineTo(badgeCenter.x, badgeCenter.y - h * 0.07f)
            lineTo(badgeCenter.x + w * 0.10f, badgeCenter.y - h * 0.02f)
            lineTo(badgeCenter.x, badgeCenter.y + h * 0.03f)
            close()
        }
        drawPath(cap, Color.White)
        drawPath(
            Path().apply {
                moveTo(badgeCenter.x - w * 0.06f, badgeCenter.y)
                lineTo(badgeCenter.x - w * 0.06f, badgeCenter.y + h * 0.05f)
                cubicTo(
                    badgeCenter.x - w * 0.03f, badgeCenter.y + h * 0.08f,
                    badgeCenter.x + w * 0.03f, badgeCenter.y + h * 0.08f,
                    badgeCenter.x + w * 0.06f, badgeCenter.y + h * 0.05f
                )
                lineTo(badgeCenter.x + w * 0.06f, badgeCenter.y)
            },
            Color.White
        )

        drawFourPointStar(this, Offset(w * 0.85f, h * 0.22f), w * 0.08f, Color(0xFFF59E0B))
        drawFourPointStar(this, Offset(w * 0.12f, h * 0.28f), w * 0.06f, Color(0xFF3B82F6))
    }
}

@Composable
fun AssignmentsEmptyArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.size(130.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        drawArc(
            color = Color(0xFF93C5FD),
            startAngle = 130f,
            sweepAngle = 200f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, h * 0.1f),
            size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.8f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
        )

        val sheet = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(w * 0.25f, h * 0.22f, w * 0.65f, h * 0.78f),
                    cornerRadius = CornerRadius(w * 0.06f, h * 0.06f)
                )
            )
        }
        drawPath(sheet, Color.White)
        drawPath(
            sheet,
            Color(0xFFDBEAFE),
            style = Stroke(width = 1.5.dp.toPx())
        )

        for (i in 0..3) {
            val y = h * (0.34f + i * 0.10f)
            drawCircle(Color(0xFF93C5FD), radius = w * 0.025f, center = Offset(w * 0.33f, y))
            drawLine(
                Color(0xFFE2E8F0),
                start = Offset(w * 0.39f, y),
                end = Offset(w * 0.58f, y),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val centerGlass = Offset(w * 0.54f, h * 0.48f)
        val glassRadius = w * 0.20f

        drawCircle(
            color = Color(0xFF2563EB).copy(alpha = 0.1f),
            radius = glassRadius,
            center = centerGlass
        )
        drawCircle(
            color = Color(0xFF2563EB),
            radius = glassRadius,
            center = centerGlass,
            style = Stroke(width = 4.dp.toPx())
        )

        val handleStart = Offset(
            centerGlass.x + glassRadius * 0.707f,
            centerGlass.y + glassRadius * 0.707f
        )
        val handleEnd = Offset(w * 0.82f, h * 0.82f)
        drawLine(
            color = Color(0xFF1D4ED8),
            start = handleStart,
            end = handleEnd,
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawFourPointStar(this, Offset(w * 0.82f, h * 0.25f), w * 0.07f, Color(0xFF3B82F6))
        drawFourPointStar(this, Offset(w * 0.16f, h * 0.65f), w * 0.05f, Color(0xFF60A5FA))
    }
}

@Composable
fun LibraryHeroArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.size(110.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        val bookLeft = Path().apply {
            moveTo(w * 0.15f, h * 0.30f)
            cubicTo(w * 0.30f, h * 0.25f, w * 0.45f, h * 0.30f, w * 0.50f, h * 0.35f)
            lineTo(w * 0.50f, h * 0.78f)
            cubicTo(w * 0.45f, h * 0.73f, w * 0.30f, h * 0.68f, w * 0.15f, h * 0.73f)
            close()
        }
        drawPath(bookLeft, Color.White)
        drawPath(
            bookLeft,
            Color(0xFFBFDBFE),
            style = Stroke(width = 1.5.dp.toPx())
        )

        val bookRight = Path().apply {
            moveTo(w * 0.50f, h * 0.35f)
            cubicTo(w * 0.55f, h * 0.30f, w * 0.70f, h * 0.25f, w * 0.85f, h * 0.30f)
            lineTo(w * 0.85f, h * 0.73f)
            cubicTo(w * 0.70f, h * 0.68f, w * 0.55f, h * 0.73f, w * 0.50f, h * 0.78f)
            close()
        }
        drawPath(bookRight, Color.White)
        drawPath(
            bookRight,
            Color(0xFFBFDBFE),
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawLine(
            Color(0xFF93C5FD),
            start = Offset(w * 0.22f, h * 0.42f),
            end = Offset(w * 0.42f, h * 0.42f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            Color(0xFF93C5FD),
            start = Offset(w * 0.22f, h * 0.52f),
            end = Offset(w * 0.42f, h * 0.52f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            Color(0xFF93C5FD),
            start = Offset(w * 0.22f, h * 0.62f),
            end = Offset(w * 0.38f, h * 0.62f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        val ribbon = Path().apply {
            moveTo(w * 0.62f, h * 0.28f)
            lineTo(w * 0.74f, h * 0.26f)
            lineTo(w * 0.74f, h * 0.58f)
            lineTo(w * 0.68f, h * 0.52f)
            lineTo(w * 0.62f, h * 0.58f)
            close()
        }
        drawPath(ribbon, Color(0xFF2563EB))

        drawFourPointStar(this, Offset(w * 0.72f, h * 0.16f), w * 0.08f, Color(0xFFF59E0B))
        drawFourPointStar(this, Offset(w * 0.90f, h * 0.30f), w * 0.06f, Color(0xFF3B82F6))
        drawFourPointStar(this, Offset(w * 0.10f, h * 0.30f), w * 0.05f, Color(0xFF60A5FA))
    }
}

@Composable
fun LibraryLessonThumbnail(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(80.dp)
            .background(Color(0xFFEFF6FF), androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height

            drawCircle(Color(0xFFDBEAFE), radius = 2.dp.toPx(), center = Offset(w * 0.25f, h * 0.25f))
            drawCircle(Color(0xFFDBEAFE), radius = 2.dp.toPx(), center = Offset(w * 0.75f, h * 0.25f))
            drawCircle(Color(0xFFDBEAFE), radius = 2.dp.toPx(), center = Offset(w * 0.25f, h * 0.75f))
            drawCircle(Color(0xFFDBEAFE), radius = 2.dp.toPx(), center = Offset(w * 0.75f, h * 0.75f))

            drawLine(
                Color(0xFF94A3B8),
                start = Offset(w * 0.15f, h * 0.80f),
                end = Offset(w * 0.90f, h * 0.80f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                Color(0xFF94A3B8),
                start = Offset(w * 0.20f, h * 0.85f),
                end = Offset(w * 0.20f, h * 0.15f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                Color(0xFF94A3B8),
                start = Offset(w * 0.20f, h * 0.38f),
                end = Offset(w * 0.80f, h * 0.38f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )

            val curve = Path().apply {
                moveTo(w * 0.20f, h * 0.75f)
                cubicTo(
                    w * 0.35f, h * 0.70f,
                    w * 0.50f, h * 0.40f,
                    w * 0.80f, h * 0.38f
                )
            }
            drawPath(
                curve,
                Color(0xFF2563EB),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            drawCircle(
                Color(0xFF2563EB),
                radius = 3.5.dp.toPx(),
                center = Offset(w * 0.80f, h * 0.38f)
            )
            drawCircle(
                Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(w * 0.80f, h * 0.38f)
            )
        }
    }
}

private fun drawFourPointStar(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    scope.drawPath(path, color)
}

@Composable
fun GemsHeroArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.size(110.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 3D Diamond
        val topY = h * 0.25f
        val midY = h * 0.42f
        val botY = h * 0.75f
        val leftX = w * 0.20f
        val midX1 = w * 0.38f
        val midX2 = w * 0.62f
        val rightX = w * 0.80f

        // Top facets
        val f1 = Path().apply {
            moveTo(leftX, midY)
            lineTo(midX1, topY)
            lineTo(w * 0.5f, midY)
            close()
        }
        drawPath(f1, Color(0xFF60A5FA))

        val f2 = Path().apply {
            moveTo(midX1, topY)
            lineTo(midX2, topY)
            lineTo(w * 0.5f, midY)
            close()
        }
        drawPath(f2, Color(0xFF93C5FD))

        val f3 = Path().apply {
            moveTo(midX2, topY)
            lineTo(rightX, midY)
            lineTo(w * 0.5f, midY)
            close()
        }
        drawPath(f3, Color(0xFF3B82F6))

        // Bottom facets
        val f4 = Path().apply {
            moveTo(leftX, midY)
            lineTo(w * 0.5f, midY)
            lineTo(w * 0.5f, botY)
            close()
        }
        drawPath(f4, Color(0xFF2563EB))

        val f5 = Path().apply {
            moveTo(w * 0.5f, midY)
            lineTo(rightX, midY)
            lineTo(w * 0.5f, botY)
            close()
        }
        drawPath(f5, Color(0xFF1D4ED8))

        drawFourPointStar(this, Offset(w * 0.82f, h * 0.22f), w * 0.08f, Color(0xFFF59E0B))
        drawFourPointStar(this, Offset(w * 0.15f, h * 0.30f), w * 0.06f, Color(0xFF3B82F6))
        drawFourPointStar(this, Offset(w * 0.85f, h * 0.68f), w * 0.05f, Color(0xFF60A5FA))
    }
}

@Composable
fun SearchHeroArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.size(110.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Cards behind
        val card1 = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(w * 0.45f, h * 0.20f, w * 0.82f, h * 0.65f),
                    cornerRadius = CornerRadius(w * 0.06f, h * 0.06f)
                )
            )
        }
        drawPath(card1, Color.White)
        drawPath(card1, Color(0xFFDBEAFE), style = Stroke(width = 1.5.dp.toPx()))

        for (i in 0..2) {
            val y = h * (0.32f + i * 0.10f)
            drawLine(
                Color(0xFF93C5FD),
                start = Offset(w * 0.52f, y),
                end = Offset(w * 0.74f, y),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Magnifying glass
        val centerGlass = Offset(w * 0.38f, h * 0.45f)
        val glassRadius = w * 0.22f

        drawCircle(Color(0xFF2563EB).copy(alpha = 0.1f), radius = glassRadius, center = centerGlass)
        drawCircle(Color(0xFF2563EB), radius = glassRadius, center = centerGlass, style = Stroke(width = 4.dp.toPx()))

        val handleStart = Offset(centerGlass.x + glassRadius * 0.707f, centerGlass.y + glassRadius * 0.707f)
        val handleEnd = Offset(w * 0.75f, h * 0.80f)
        drawLine(Color(0xFF1D4ED8), start = handleStart, end = handleEnd, strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)

        drawFourPointStar(this, Offset(w * 0.80f, h * 0.18f), w * 0.08f, Color(0xFFF59E0B))
        drawFourPointStar(this, Offset(w * 0.15f, h * 0.28f), w * 0.06f, Color(0xFF3B82F6))
    }
}

@Composable
fun SolverHistoryHeroArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.size(110.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        val doc = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(w * 0.25f, h * 0.20f, w * 0.72f, h * 0.80f),
                    cornerRadius = CornerRadius(w * 0.06f, h * 0.06f)
                )
            )
        }
        drawPath(doc, Color.White)
        drawPath(doc, Color(0xFFBFDBFE), style = Stroke(width = 1.5.dp.toPx()))

        for (i in 0..2) {
            val y = h * (0.32f + i * 0.12f)
            drawLine(
                Color(0xFF93C5FD),
                start = Offset(w * 0.34f, y),
                end = Offset(w * 0.62f, y),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Clock badge
        val clockCenter = Offset(w * 0.68f, h * 0.68f)
        val clockRadius = w * 0.15f
        drawCircle(Color(0xFF2563EB), radius = clockRadius, center = clockCenter)
        drawLine(Color.White, start = clockCenter, end = Offset(clockCenter.x, clockCenter.y - clockRadius * 0.6f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(Color.White, start = clockCenter, end = Offset(clockCenter.x + clockRadius * 0.5f, clockCenter.y), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

        drawFourPointStar(this, Offset(w * 0.82f, h * 0.22f), w * 0.08f, Color(0xFFF59E0B))
        drawFourPointStar(this, Offset(w * 0.15f, h * 0.30f), w * 0.06f, Color(0xFF3B82F6))
    }
}

@Composable
fun QuestionHeroArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.size(110.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        val bookLeft = Path().apply {
            moveTo(w * 0.18f, h * 0.32f)
            cubicTo(w * 0.32f, h * 0.27f, w * 0.46f, h * 0.32f, w * 0.50f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.76f)
            cubicTo(w * 0.46f, h * 0.71f, w * 0.32f, h * 0.66f, w * 0.18f, h * 0.71f)
            close()
        }
        drawPath(bookLeft, Color.White)
        drawPath(bookLeft, Color(0xFF2563EB), style = Stroke(width = 2.dp.toPx()))

        val bookRight = Path().apply {
            moveTo(w * 0.82f, h * 0.32f)
            cubicTo(w * 0.68f, h * 0.27f, w * 0.54f, h * 0.32f, w * 0.50f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.76f)
            cubicTo(w * 0.54f, h * 0.71f, w * 0.68f, h * 0.66f, w * 0.82f, h * 0.71f)
            close()
        }
        drawPath(bookRight, Color.White)
        drawPath(bookRight, Color(0xFF2563EB), style = Stroke(width = 2.dp.toPx()))

        // Bookmark ribbon
        val bookmark = Path().apply {
            moveTo(w * 0.60f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.52f)
            lineTo(w * 0.66f, h * 0.46f)
            lineTo(w * 0.60f, h * 0.52f)
            close()
        }
        drawPath(bookmark, Color(0xFF2563EB))

        drawFourPointStar(this, Offset(w * 0.82f, h * 0.20f), w * 0.08f, Color(0xFFF59E0B))
        drawFourPointStar(this, Offset(w * 0.15f, h * 0.28f), w * 0.06f, Color(0xFF3B82F6))
    }
}
