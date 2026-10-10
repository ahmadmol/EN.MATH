package com.ahmadmol.enmath.features.explore

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.design.*
import kotlin.math.abs

@Composable
fun ExploreScreen() {
    val engine = remember { DemoGraphEngine() }
    var functions by rememberSaveable { mutableStateOf(listOf("x^2", "sin(x)", "2x+1")) }
    var hidden by rememberSaveable { mutableStateOf(listOf<String>()) }
    var input by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var grid by rememberSaveable { mutableStateOf(true) }
    var axes by rememberSaveable { mutableStateOf(true) }
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
        )
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    ScreenPage(Modifier.testTag("screen-explore")) {
        PageHeading(label(TextKey.t_8d680ec8eb), label(TextKey.t_ccdff60317))
        DemoNotice(label(TextKey.t_729df17606))
        AdaptivePair(
            first = {
                AppTextField(
                    input,
                    {
                        input = it
                        error = false
                    },
                    label(TextKey.t_6f4922a8ad),
                    Modifier.testTag("graph-input"),
                )
                SecondaryButton(
                    label(TextKey.t_665b9023b5),
                    {
                        if (engine.evaluate(input, 0.0) != null) {
                            functions = (functions + input).distinct().take(6)
                            input = ""
                        } else error = true
                    },
                )
                if (error)
                    Text(label(TextKey.t_021fc6959d), color = MaterialTheme.colorScheme.error)
                functions.forEachIndexed { i, f ->
                    AppCard {
                        Row {
                            Checkbox(f !in hidden, { hidden = if (it) hidden - f else hidden + f })
                            MathText("y = $f")
                        }
                        TextButton({ functions = functions - f }) {
                            Text(label(TextKey.t_f0102ff57c))
                        }
                        when (f) {
                            "x^2" -> Text(label(TextKey.t_473f260a04))
                            "sin(x)" -> Text(label(TextKey.t_5994568d38))
                            "2x+1" -> Text(label(TextKey.t_972fd332cf))
                        }
                        Text("●", color = colors[i % 3])
                    }
                }
                Row {
                    TextButton({ zoom = (zoom * 1.25f).coerceAtMost(8f) }) { Text("+") }
                    TextButton({ zoom = (zoom / 1.25f).coerceAtLeast(.25f) }) { Text("−") }
                    TextButton({
                        zoom = 1f
                        pan = Offset.Zero
                    }) {
                        Text(label(TextKey.t_114c6c149d))
                    }
                }
                Row {
                    Checkbox(grid, { grid = it })
                    Text(label(TextKey.t_b2b90cdc0d))
                    Checkbox(axes, { axes = it })
                    Text(label(TextKey.t_98cd0de4fd))
                }
            },
            second = {
                Canvas(
                    Modifier.fillMaxWidth().height(360.dp).testTag("graph-canvas").pointerInput(
                        Unit
                    ) {
                        detectTransformGestures { _, delta, factor, _ ->
                            pan += delta
                            zoom = (zoom * factor).coerceIn(.25f, 8f)
                        }
                    }
                ) {
                    val scale = 35f * zoom
                    val origin = Offset(size.width / 2 + pan.x, size.height / 2 + pan.y)
                    if (grid) {
                        for (i in -60..60) {
                            val x = origin.x + i * scale
                            val y = origin.y + i * scale
                            if (x in 0f..size.width)
                                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height))
                            if (y in 0f..size.height)
                                drawLine(gridColor, Offset(0f, y), Offset(size.width, y))
                        }
                    }
                    if (axes) {
                        drawLine(axisColor, Offset(0f, origin.y), Offset(size.width, origin.y), 2f)
                        drawLine(axisColor, Offset(origin.x, 0f), Offset(origin.x, size.height), 2f)
                    }
                    functions
                        .filter { it !in hidden }
                        .forEach { f ->
                            val path = Path()
                            var start = true
                            var previous = 0f
                            for (px in 0..size.width.toInt() step 2) {
                                val x = (px - origin.x) / scale
                                val y = engine.evaluate(f, x.toDouble())?.toFloat()
                                val sy = if (y == null) Float.NaN else origin.y - y * scale
                                if (
                                    !sy.isFinite() ||
                                        sy !in -size.height..size.height * 2 ||
                                        (!start && abs(sy - previous) > size.height / 2)
                                ) {
                                    start = true
                                    continue
                                }
                                if (start) {
                                    path.moveTo(px.toFloat(), sy)
                                    start = false
                                } else path.lineTo(px.toFloat(), sy)
                                previous = sy
                            }
                            drawPath(
                                path,
                                colors[functions.indexOf(f) % 3],
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
                            )
                        }
                }
                SectionTitle(label(TextKey.t_1a25d7806a))
                functions
                    .filter { it !in hidden }
                    .forEach { f ->
                        AppCard {
                            MathText("y=$f")
                            (-2..2).forEach { x ->
                                MathText(
                                    "x=$x → y=${engine.evaluate(f,x.toDouble())?.let {"%.3f".format(java.util.Locale.US,it)}?:"—"}"
                                )
                            }
                        }
                    }
            },
        )
    }
}
