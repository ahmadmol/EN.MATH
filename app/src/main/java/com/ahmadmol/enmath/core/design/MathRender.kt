package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Native, selectable-direction-safe equation surface; no HTML/WebView or calculation changes. */
@Composable
fun MathRender(value: String, style: TextStyle = MaterialTheme.typography.titleLarge) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.medium,
            modifier =
                Modifier.fillMaxWidth().testTag("math-render").semantics {
                    stateDescription = "math-ready"
                },
        ) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(Space.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                val equation = remember(value) { nativeEquationParts(value) }
                val mathStyle =
                    style.copy(textDirection = TextDirection.Ltr, fontFamily = FontFamily.Serif)
                if (equation.limit != null)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("lim", style = mathStyle)
                        Text(
                            equation.limit,
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    textDirection = TextDirection.Ltr
                                ),
                        )
                    }
                if (equation.denominator == null)
                    Text(equation.numerator, style = mathStyle, softWrap = false)
                else
                    Column(
                        Modifier.width(IntrinsicSize.Max),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(equation.numerator, style = mathStyle, softWrap = false)
                        Box(
                            Modifier.fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface)
                        )
                        Text(equation.denominator, style = mathStyle, softWrap = false)
                    }
            }
        }
    }
}

internal data class NativeEquationParts(
    val limit: String?,
    val numerator: String,
    val denominator: String?,
)

/** Formatting only. Ambiguous expressions remain literal; never rewrite algebra or evaluate. */
internal fun nativeEquationParts(value: String): NativeEquationParts {
    val match = Regex("^lim\\s+(x\\s*→\\s*[^ ]+)\\s+(.+)$").matchEntire(value.trim())
    val bound = match?.groupValues?.get(1)
    val body = match?.groupValues?.get(2) ?: value
    var depth = 0
    val slashes = mutableListOf<Int>()
    body.forEachIndexed { i, c ->
        when (c) {
            '(' -> depth++
            ')' -> depth--
            '/' -> if (depth == 0) slashes += i
        }
    }
    fun atomic(s: String): Boolean {
        if (Regex("[A-Za-z0-9_²³ⁿπ∞]+|(?:sin|cos|tan|ln)\\([^/]+\\)").matches(s)) return true
        if (!s.startsWith('(') || !s.endsWith(')')) return false
        var nesting = 0
        s.forEachIndexed { i, c ->
            if (c == '(') nesting++ else if (c == ')') nesting--
            if (nesting == 0 && i < s.lastIndex) return false
            if (nesting < 0) return false
        }
        return nesting == 0
    }
    if (depth == 0 && slashes.size == 1 && !body.contains('=')) {
        val top = body.substring(0, slashes.single()).trim()
        val bottom = body.substring(slashes.single() + 1).trim()
        if (atomic(top) && atomic(bottom)) return NativeEquationParts(bound, top, bottom)
    }
    return NativeEquationParts(bound, body, null)
}

fun mathTex(value: String): String {
    if (value.startsWith("\\")) return value
    return value
        .replace("\\", "\\backslash ")
        .replace("{", "\\{")
        .replace("}", "\\}")
        .replace("%", "\\%")
        .replace("#", "\\#")
        .replace("&", "\\&")
        .replace("ⁿ⁻¹", "^{n-1}")
        .replace("ⁿ⁺¹", "^{n+1}")
        .replace("²", "^{2}")
        .replace("³", "^{3}")
        .replace("ⁿ", "^{n}")
        .replace("⁻¹", "^{-1}")
        .replace("¹", "^{1}")
        .replace("ₙ", "_{n}")
        .replace("₁", "_{1}")
        .replace("₂", "_{2}")
        .replace("₀", "_{0}")
        .replace("∫", "\\int ")
        .replace("∑", "\\sum ")
        .replace("π", "\\pi ")
        .replace("∞", "\\infty ")
        .replace("→", "\\to ")
        .replace("×", "\\times ")
        .replace("÷", "\\div ")
        .replace("−", "-")
        .replace("≠", "\\ne ")
        .replace("∩", "\\cap ")
        .replace("Ω", "\\Omega ")
        .replace("ln(", "\\ln(")
        .replace("sin(", "\\sin(")
        .replace("cos(", "\\cos(")
        .replace("tan(", "\\tan(")
        .replace("d/dx", "\\frac{d}{dx}")
        .replace(" at ", "\\text{ at } ")
        .replace("det ", "\\det ")
        .replace("even on fair die", "\\text{even on fair die}")
        .replace("head on fair coin", "\\text{head on fair coin}")
}
