package com.ahmadmol.enmath.features.courses.interactive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.features.explore.DemoGraphEngine
import com.ahmadmol.enmath.features.explore.GraphEngine
import java.util.Locale
import kotlin.math.roundToInt

/** The puncture belongs to this lesson; evaluation uses the existing graph engine. */
internal object LimitExploration {
    private val engine: GraphEngine = DemoGraphEngine()
    const val expression = "x + 2"
    fun curve(x: Double): Double = requireNotNull(engine.evaluate(expression, x))
    fun value(x: Double): Double? = if (x == 3.0) null else curve(x)
    fun sliderValue(value: Float): Float = (value.coerceIn(2f, 4f) * 100).roundToInt() / 100f
    fun format(value: Double): String = String.format(Locale.US, "%.2f", value)
    fun fractionX(x: Double): Float = (x / 6.5).toFloat()
    fun fractionY(y: Double): Float = (1 - y / 9.3).toFloat()
}

@Composable
internal fun ExplorationPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .46f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .05f))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LimitApproachVisual(x: Float, onXChange: (Float) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val value = LimitExploration.value(x.toDouble())
    val xText = LimitExploration.format(x.toDouble())
    val result = value?.let(LimitExploration::format)
    val sliderDescription = tr("Value of x", "قيمة x")
    ExplorationPanel {
        LimitGraph(x)
    }
    ExplorationPanel {
        MathRender("f(x) = x + 2, x ≠ 3", MaterialTheme.typography.titleLarge,
            Color.Transparent, contentPadding = PaddingValues(4.dp))
        // Separate rows can wrap vertically at accessibility font sizes without reversing math.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints {
            val stacked = maxWidth < 280.dp || LocalDensity.current.fontScale > 1.3f
            val values: @Composable () -> Unit = {
                MathRender("x = $xText", MaterialTheme.typography.titleLarge, Color.Transparent,
                    contentPadding = PaddingValues(4.dp))
                MathRender(if (result == null) "f(3): undefined" else "f(x) = $result",
                    MaterialTheme.typography.titleLarge, Color.Transparent, contentPadding = PaddingValues(4.dp))
            }
            if (stacked) Column { values() }
            else Row {
                Box(Modifier.weight(1f)) { MathRender("x = $xText", MaterialTheme.typography.titleLarge,
                    Color.Transparent, contentPadding = PaddingValues(4.dp)) }
                Box(Modifier.weight(1f)) { MathRender(if (result == null) "f(3): undefined" else "f(x) = $result",
                    MaterialTheme.typography.titleLarge, Color.Transparent, contentPadding = PaddingValues(4.dp)) }
            }
        }
        }
        if (result == null) Text(tr("Undefined at x = 3; the limit is 5", "الدالة غير معرّفة عند x = 3؛ النهاية تساوي 5"),
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("limit-undefined"))
    }
    ExplorationPanel {
        Text(tr("Move the slider to change x", "حرّك المنزلق لتغيير قيمة x"),
            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(30.dp), color = colors.primary.copy(alpha = .09f)) {
                    Text(xText, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium, color = colors.primary)
                }
                Slider(x, { onXChange(LimitExploration.sliderValue(it)) }, valueRange = 2f..4f,
                    modifier = Modifier.weight(1f).testTag("limit-slider").semantics {
                        contentDescription = sliderDescription
                        stateDescription = xText
                    },
                    colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.secondary.copy(alpha = .28f)),
                    thumb = {
                        Surface(Modifier.size(26.dp), shape = CircleShape, color = colors.primary,
                            border = BorderStroke(2.dp, colors.surface), shadowElevation = 4.dp) {}
                    })
            }
        }
    }
    ExplorationPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Info, null, tint = colors.primary)
            Box(Modifier.weight(1f)) {
                MathProse(tr("The open circle is at (3, 5). Approach 3 from either side: the values approach 5.",
                    "الدائرة المفتوحة عند (3, 5). اقترب من 3 من الجانبين؛ تقترب قيم الدالة من 5."),
                    MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LimitGraph(x: Float) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = colors.onSurfaceVariant)
    val equationStyle = MaterialTheme.typography.titleMedium.copy(color = colors.primary)
    val fontScale = LocalDensity.current.fontScale
    val summary = tr("Graph y = x + 2. Horizontal axis 0 to 6.5; vertical axis 0 to 9.3. Open circle at (3, 5).",
        "رسم y = x + 2. المحور الأفقي من 0 إلى 6.5 والعمودي من 0 إلى 9.3. دائرة مفتوحة عند (3, 5).")
    Canvas(Modifier.fillMaxWidth().height((210 + 24 * (fontScale - 1).coerceAtLeast(0f)).dp)
        .testTag("limit-visual").semantics { contentDescription = summary }) {
        val sample = measurer.measure("9.3", labelStyle)
        val left = sample.size.width + 10.dp.toPx()
        val bottom = size.height - sample.size.height - 12.dp.toPx()
        val top = sample.size.height + 8.dp.toPx()
        val right = size.width - 20.dp.toPx()
        fun point(v: Double, y: Double = LimitExploration.curve(v)) = Offset(
            left + LimitExploration.fractionX(v) * (right - left),
            top + LimitExploration.fractionY(y) * (bottom - top))
        fun label(text: String, at: Offset, style: TextStyle = labelStyle) {
            val measured = measurer.measure(text, style)
            drawText(measured, topLeft = Offset(
                (at.x - measured.size.width / 2f).coerceIn(0f, (size.width - measured.size.width).coerceAtLeast(0f)),
                (at.y - measured.size.height / 2f).coerceIn(0f, (size.height - measured.size.height).coerceAtLeast(0f))))
        }
        for (tick in 0..6) {
            val p = point(tick.toDouble(), 0.0)
            drawLine(colors.outlineVariant.copy(alpha = .4f), Offset(p.x, top), p, 1.dp.toPx())
            label("$tick", Offset(p.x, bottom + sample.size.height / 2 + 6.dp.toPx()))
        }
        for (tick in 0..8 step 2) {
            val p = point(0.0, tick.toDouble())
            drawLine(colors.outlineVariant.copy(alpha = .4f), p, Offset(right, p.y), 1.dp.toPx())
            label("$tick", Offset(left - sample.size.width / 2 - 5.dp.toPx(), p.y))
        }
        drawLine(colors.onSurfaceVariant, Offset(left, top), Offset(left, bottom), 1.2.dp.toPx())
        drawLine(colors.onSurfaceVariant, Offset(left, bottom), Offset(right, bottom), 1.2.dp.toPx())
        val arrow = 4.dp.toPx()
        drawLine(colors.onSurfaceVariant, Offset(left, top), Offset(left - arrow, top + arrow * 2), 1.2.dp.toPx())
        drawLine(colors.onSurfaceVariant, Offset(left, top), Offset(left + arrow, top + arrow * 2), 1.2.dp.toPx())
        drawLine(colors.onSurfaceVariant, Offset(right, bottom), Offset(right - arrow * 2, bottom - arrow), 1.2.dp.toPx())
        drawLine(colors.onSurfaceVariant, Offset(right, bottom), Offset(right - arrow * 2, bottom + arrow), 1.2.dp.toPx())
        label("y", Offset(left, top - sample.size.height / 2 - 2.dp.toPx()))
        label("x", Offset(right + 10.dp.toPx(), bottom))
        val hole = point(3.0)
        drawLine(colors.onSurfaceVariant.copy(alpha = .4f), Offset(hole.x, top), Offset(hole.x, bottom),
            1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())))
        // The existing engine supplies every plotted endpoint and the live point.
        drawLine(colors.primary, point(0.0), point(6.3), 2.dp.toPx())
        label("y = x + 2", point(4.9, 5.3), equationStyle)
        LimitExploration.value(x.toDouble())?.let { drawCircle(colors.primary, 4.dp.toPx(), point(x.toDouble(), it)) }
        drawCircle(colors.surfaceVariant, 5.dp.toPx(), hole)
        drawCircle(colors.primary, 5.dp.toPx(), hole, style = Stroke(1.5.dp.toPx()))
    }
}
