package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import kotlin.math.*

@Composable
fun ReferenceGraph() {
    var visible by rememberSaveable { mutableStateOf(listOf(true, true, false, false)) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val colors = listOf(Color(0xFF818CF8), RefPalette.cyan, Color(0xFF10B981), Color(0xFFF59E0B))
    val functions = listOf("f(x) = x² − 4", "g(x) = 2 sin(x)", "h(x) = 0.5x² − 2", "y = 4x − 8")
    val types =
        listOf(
            refText("تابع قطع مكافئ", "Parabola"),
            refText("تابع الجيب", "Sine wave"),
            refText("تابع تربيعي", "Quadratic"),
            refText("مماس عند x=2", "Tangent at x=2"),
        )
    ReferencePage("screen-explore") {
        Column {
            RefHeading(
                refText("تمثيل التوابع والمقاربات والمماسات", "Functions, asymptotes and tangents"),
                refText("المختبر البياني التفاعلي", "Interactive graph laboratory"),
                Icons.Outlined.Layers,
            )
            RefLabel(
                refText(
                    "استكشف التغيرات، نقاط التقاطع، المماسات، واحفظ استنتاجك العلمي المباشر.",
                    "Explore variations, intersections and tangents to build mathematical insight.",
                )
            )
        }
        Box(
            Modifier.fillMaxWidth()
                .height(211.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFF070B14), RoundedCornerShape(11.dp))
                .border(.7.dp, RefPalette.border, RoundedCornerShape(11.dp))
        ) {
            Canvas(
                Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, delta, scale, _ ->
                            pan += delta
                            zoom = (zoom * scale).coerceIn(.45f, 3f)
                        }
                    }
                    .testTag("reference-graph")
            ) {
                val unit = size.width / 16f * zoom
                val yUnit = size.height / 12f * zoom
                val origin = Offset(size.width / 2 + pan.x, size.height / 2 + pan.y)
                val clipColor = Color(0xFF182536)
                for (i in -50..50) {
                    val x = origin.x + i * unit
                    val y = origin.y + i * yUnit
                    if (x in 0f..size.width)
                        drawLine(
                            clipColor,
                            Offset(x, 0f),
                            Offset(x, size.height),
                            strokeWidth = .6.dp.toPx(),
                        )
                    if (y in 0f..size.height)
                        drawLine(
                            clipColor,
                            Offset(0f, y),
                            Offset(size.width, y),
                            strokeWidth = .6.dp.toPx(),
                        )
                }
                drawLine(
                    RefPalette.muted.copy(alpha = .7f),
                    Offset(origin.x, 0f),
                    Offset(origin.x, size.height),
                    strokeWidth = .6.dp.toPx(),
                )
                drawLine(
                    RefPalette.muted.copy(alpha = .7f),
                    Offset(0f, origin.y),
                    Offset(size.width, origin.y),
                    strokeWidth = .6.dp.toPx(),
                )
                visible.forEachIndexed { index, on ->
                    if (on) {
                        val path = Path()
                        var started = false
                        for (px in 0..size.width.toInt()) {
                            val x = ((px - origin.x) / unit).toDouble()
                            val y =
                                when (index) {
                                    0 -> x * x - 4
                                    1 -> 2 * sin(x)
                                    2 -> .5 * x * x - 2
                                    else -> 4 * x - 8
                                }
                            val sy = origin.y - y.toFloat() * yUnit
                            if (!sy.isFinite() || sy < -size.height || sy > size.height * 2) {
                                started = false
                                continue
                            }
                            if (!started) {
                                path.moveTo(px.toFloat(), sy)
                                started = true
                            } else path.lineTo(px.toFloat(), sy)
                        }
                        drawPath(
                            path,
                            colors[index],
                            style =
                                androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                        )
                    }
                }
                val roots = buildList {
                    if (visible[0] || visible[2]) addAll(listOf(-2f, 2f))
                    if (visible[1]) addAll(listOf(-PI.toFloat(), 0f, PI.toFloat()))
                    if (visible[3]) add(2f)
                }
                roots.distinct().forEach { r ->
                    val x = origin.x + r * unit
                    if (x in 0f..size.width && origin.y in 0f..size.height)
                        drawCircle(
                            RefPalette.green,
                            radius = 2.5.dp.toPx(),
                            center = Offset(x, origin.y),
                        )
                }
                val paint =
                    android.graphics.Paint().apply {
                        color = RefPalette.muted.copy(alpha = .65f).toArgb()
                        textSize = 6.dp.toPx()
                        isAntiAlias = true
                    }
                drawContext.canvas.nativeCanvas.apply {
                    for (i in -6..6 step 2) if (i != 0) {
                        drawText(i.toString(), origin.x + i * unit, origin.y + 8.dp.toPx(), paint)
                        if (abs(i) <= 4)
                            drawText(
                                i.toString(),
                                origin.x - 6.dp.toPx(),
                                origin.y - i * yUnit,
                                paint,
                            )
                    }
                }
            }
            Row(
                Modifier.align(AbsoluteAlignment.BottomRight)
                    .padding(8.dp)
                    .background(RefPalette.card, RoundedCornerShape(8.dp))
                    .border(.6.dp, RefPalette.border, RoundedCornerShape(8.dp))
                    .padding(3.dp)
            ) {
                listOf(Icons.Outlined.Remove, Icons.Outlined.Add, Icons.Outlined.RestartAlt)
                    .forEachIndexed { index, icon ->
                        Box(
                            Modifier.size(22.dp)
                                .clickable {
                                    when (index) {
                                        0 -> zoom = (zoom * .8f).coerceAtLeast(.45f)
                                        1 -> zoom = (zoom * 1.25f).coerceAtMost(3f)
                                        else -> {
                                            zoom = 1f
                                            pan = Offset.Zero
                                        }
                                    }
                                }
                                .testTag("graph-control-$index"),
                            contentAlignment = Alignment.Center,
                        ) {
                            RefIcon(icon, RefPalette.muted, 12)
                        }
                    }
            }
        }
        RefLabel(
            refText("قائمة التوابع والمستقيمات المقارنة", "Functions and comparison lines"),
            RefPalette.white,
            9,
        )
        functions.forEachIndexed { i, function ->
            RefCard(
                Modifier.clickable {
                        visible = visible.mapIndexed { n, v -> if (n == i) !v else v }
                    }
                    .testTag("graph-function-$i"),
                padding = 7.dp,
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.size(10.dp).background(colors[i], CircleShape))
                    Column(Modifier.weight(1f)) {
                        CompositionLocalProvider(
                            androidx.compose.ui.platform.LocalLayoutDirection provides
                                LayoutDirection.Ltr
                        ) {
                            MathRun(function, 11, RefPalette.violet)
                        }
                        RefLabel(types[i], size = 7)
                    }
                    RefIcon(
                        if (visible[i]) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        if (visible[i]) RefPalette.violet else RefPalette.muted,
                        12,
                    )
                }
            }
        }
    }
}
