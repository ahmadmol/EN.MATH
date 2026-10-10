package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*

/** Native Compose equation primitives; no browser, HTML, or JavaScript. */
@Composable
fun MathRun(value: String, size: Int = 15, color: Color = RefPalette.violet) {
    Text(
        value,
        color = color,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        style =
            androidx.compose.ui.text.TextStyle(
                textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
            ),
        fontSize = size.sp,
        lineHeight = (size + 5).sp,
    )
}

@Composable
fun Fraction(top: String, bottom: String, size: Int = 15, color: Color = RefPalette.violet) {
    Column(Modifier.width(IntrinsicSize.Max), horizontalAlignment = Alignment.CenterHorizontally) {
        MathRun(top, size, color)
        Box(Modifier.fillMaxWidth().height(.6.dp).background(color))
        MathRun(bottom, size, color)
    }
}

@Composable
fun LimitSymbol(size: Int = 15, color: Color = RefPalette.violet) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MathRun("lim", size, color)
        MathRun("x→2", (size - 6).coerceAtLeast(6), color)
    }
}

@Composable
fun LimitFormula(stage: Int = 0, size: Int = 15, color: Color = RefPalette.violet) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            when (stage) {
                1 -> {
                    MathRun("f(2) =", size, color)
                    Fraction("2² − 4", "2 − 2", size, color)
                    MathRun("=", size, color)
                    Fraction("0", "0", size, color)
                    MathRun("[حالة عدم تعيين]", 8, color)
                }
                2 -> MathRun("x² − 4 = (x − 2)(x + 2)", size, color)
                3 -> {
                    LimitSymbol(size, color)
                    Fraction("(x − 2)(x + 2)", "x − 2", size, color)
                    MathRun("=", size, color)
                    LimitSymbol(size, color)
                    MathRun("(x + 2)", size, color)
                }
                4 -> {
                    LimitSymbol(size, color)
                    MathRun("(x + 2) = 2 + 2 = 4", size, color)
                }
                5 -> {
                    LimitSymbol(size, color)
                    Fraction("2x", "1", size, color)
                    MathRun("= 4", size, color)
                }
                else -> {
                    LimitSymbol(size, color)
                    Fraction("x² − 4", "x − 2", size, color)
                }
            }
        }
    }
}

@Composable
fun NativeFormula(kind: Int, size: Int = 14, color: Color = RefPalette.white) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        when (kind) {
            0 -> LimitFormula(size = size, color = color)
            1 ->
                Column {
                    MathRun("f(x) = ln(x² + 1) ⟹", size, color)
                    MathRun("f′(x)", size, color)
                }
            2 -> MathRun("∫ xeˣ² dx", size, color)
            3 ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    MathRun("A =", size, color)
                    MathRun("(", size + 6, color)
                    Column {
                        MathRun("3  1", size, color)
                        MathRun("2  4", size, color)
                    }
                    MathRun(") ⇒ A⁻¹", size, color)
                }
        }
    }
}
