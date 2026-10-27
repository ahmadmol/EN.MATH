package com.ahmadmol.enmath.features.explore

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.design.*
import kotlin.math.*

object LabPalette {
    val darkBackground = Color(0xFF081426)
    val darkGraphBackground = Color(0xFF0C2037)
    val darkCardBackground = Color(0xFF10243C)
    val darkElevatedSurface = Color(0xFF152D49)
    val darkBorder = Color(0xFF1F3B5C)
    val darkGrid = Color(0xFF1E3857)
    val darkGridSubtle = Color(0xFF13273E)
    val darkAxis = Color(0xFFCBD5E1)
    val darkTextPrimary = Color(0xFFF5F8FF)
    val darkTextSecondary = Color(0xFFA6B7D2)

    val lightBackground = Color(0xFFF0F4F9)
    val lightGraphBackground = Color(0xFFFFFFFF)
    val lightCardBackground = Color(0xFFFFFFFF)
    val lightElevatedSurface = Color(0xFFE2E8F0)
    val lightBorder = Color(0xFFCBD5E1)
    val lightGrid = Color(0xFFE2E8F0)
    val lightGridSubtle = Color(0xFFF1F5F9)
    val lightAxis = Color(0xFF334155)
    val lightTextPrimary = Color(0xFF0F172A)
    val lightTextSecondary = Color(0xFF475569)

    val electricBlue = Color(0xFF168BFF)
    val mintGreen = Color(0xFF31E6BF)
    val purple = Color(0xFFAF70FF)
    val coralOrange = Color(0xFFFF7555)
    val goldYellow = Color(0xFFFFD166)
    val magentaPink = Color(0xFFFF53A1)

    val functionColors = listOf(electricBlue, mintGreen, purple, coralOrange, goldYellow, magentaPink)
}

fun getFunctionDescription(expression: String, arabic: Boolean = true): String {
    fun text(en: String, ar: String) = if (arabic) ar else en
    val clean = expression.lowercase().replace(" ", "").removePrefix("y=").removePrefix("f(x)=")
        .replace("²", "^2").replace("−", "-")

    return when (clean) {
        "x^2", "x*x" -> text("Root and minimum: (0,0)", "الجذر والقيمة الصغرى: (0,0)")
        "sin(x)" -> text("Range: [-1, 1], roots: kπ", "المدى: [-1, 1] ، الجذور: kπ")
        "cos(x)" -> text("Range: [-1, 1], roots: (k+0.5)π", "المدى: [-1, 1] ، الجذور: (k+0.5)π")
        "2x+1", "2*x+1" -> text("y-intercept: 1, root: -0.5", "التقاطع الرأسي: 1 ، الجذر: -0.5")
        "x^2-4", "x²-4" -> text("y-intercept: -4, roots: -2, 2", "التقاطع الرأسي: -4 ، الجذور: -2, 2")
        "0.5x^2-2", "0.5x²-2" -> text("y-intercept: -2, roots: -2, 2", "التقاطع الرأسي: -2 ، الجذور: -2, 2")
        "4x-8" -> text("y-intercept: -8, root: 2", "التقاطع الرأسي: -8 ، الجذر: 2")
        "tan(x)" -> text("Domain: x ≠ (k+0.5)π, roots: kπ", "المجال: x ≠ (k+0.5)π ، الجذور: kπ")
        "abs(x)" -> text("Minimum: (0,0), range: [0, ∞)", "القيمة الصغرى: (0,0) ، المدى: [0, ∞)")
        else -> {
            val linearMatch = Regex("([+-]?(?:\\d+(?:\\.\\d+)?)?)x([+-]\\d+(?:\\.\\d+)?)?").matchEntire(clean)
            if (linearMatch != null) {
                val aStr = linearMatch.groupValues[1]
                val a = when (aStr) { "", "+" -> 1.0; "-" -> -1.0; else -> aStr.toDoubleOrNull() ?: 1.0 }
                val bStr = linearMatch.groupValues[2]
                val b = if (bStr.isNotEmpty()) bStr.toDoubleOrNull() ?: 0.0 else 0.0
                val root = if (a != 0.0) -b / a else Double.NaN
                val rootStr = if (root.isNaN()) text("None", "لا يوجد") else if (root % 1.0 == 0.0) root.toInt().toString() else "%.2f".format(java.util.Locale.US, root)
                val bStrFormatted = if (b % 1.0 == 0.0) b.toInt().toString() else "%.2f".format(java.util.Locale.US, b)
                text("y-intercept: $bStrFormatted, root: $rootStr", "التقاطع الرأسي: $bStrFormatted ، الجذر: $rootStr")
            } else {
                text("Interactive mathematical function", "دالة رياضية تفاعلية")
            }
        }
    }
}

@Composable
fun ExploreScreen(initialExpression: String = "", onSave: ((String) -> Boolean)? = null) {
    val engine = remember { DemoGraphEngine() }
    var functions by rememberSaveable { mutableStateOf(if (initialExpression.isBlank()) listOf("x^2", "sin(x)", "2x+1") else if (graphSupported(initialExpression)) listOf(initialExpression) else emptyList()) }
    var hidden by rememberSaveable { mutableStateOf(listOf<String>()) }
    var input by rememberSaveable { mutableStateOf(initialExpression.takeIf { !graphSupported(it) }.orEmpty()) }
    var error by rememberSaveable { mutableStateOf(initialExpression.isNotBlank() && !graphSupported(initialExpression)) }
    var savedMessage by remember { mutableStateOf<Boolean?>(null) }
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var panX by rememberSaveable { mutableFloatStateOf(0f) }
    var panY by rememberSaveable { mutableFloatStateOf(0f) }
    val pan = Offset(panX, panY)
    var grid by rememberSaveable { mutableStateOf(true) }
    var axes by rememberSaveable { mutableStateOf(true) }
    var valuesExpanded by rememberSaveable { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val student = LocalStudentExperience.current
    val graphControls: @Composable () -> Unit = {
        GraphControlBar(
            zoom = zoom,
            grid = grid,
            axes = axes,
            onZoomIn = { zoom = (zoom * 1.25f).coerceAtMost(10f) },
            onZoomOut = { zoom = (zoom / 1.25f).coerceAtLeast(0.25f) },
            onReset = {
                zoom = 1f
                panX = 0f
                panY = 0f
            },
            onToggleGrid = { grid = it },
            onToggleAxes = { axes = it },
            isDark = isDark
        )
    }

    ScreenPage(Modifier.imePadding().testTag("screen-explore")) {
        // Header
        FunctionLabHeader(isDark = isDark)

        // Function Input Area
        FunctionInputRow(
            input = input,
            onInputChange = {
                input = it
                error = false
            },
            onAddFunction = {
                if (input.isNotBlank() && graphSupported(input)) {
                    functions = (functions + input).distinct().take(8)
                    input = ""
                    error = false
                } else {
                    error = true
                }
            },
            isDark = isDark
        )

        if (error) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tr("Invalid function. Try x^2, sin(x), or 2x+1.", "صيغة رياضية غير صحيحة. جرّب x^2 أو sin(x) أو 2x+1."),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Interactive Graph Canvas
        InteractiveGraphCanvas(
            functions = functions,
            hidden = hidden,
            colors = LabPalette.functionColors,
            engine = engine,
            zoom = zoom,
            pan = pan,
            grid = grid,
            axes = axes,
            onTransform = { delta, factor ->
                panX += delta.x
                panY += delta.y
                zoom = (zoom * factor).coerceIn(0.25f, 10f)
            },
            isDark = isDark
        )

        if (student) graphControls()

        // Function Cards List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            functions.forEachIndexed { index, expr ->
                FunctionCard(
                    index = index,
                    functionExpr = expr,
                    isVisible = expr !in hidden,
                    color = LabPalette.functionColors[index % LabPalette.functionColors.size],
                    onToggleVisibility = { visible ->
                        hidden = if (visible) hidden - expr else hidden + expr
                    },
                    onDelete = {
                        functions = functions - expr
                    },
                    isDark = isDark
                )
                onSave?.let { save ->
                    OutlinedButton({ savedMessage = save(expr) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(tr("Save function to my library", "حفظ الدالة في مكتبتي"))
                    }
                }
            }
        }
        savedMessage?.let { ok -> Text(if (ok) tr("Saved locally in your library", "حُفظت محليًا في مكتبتك") else tr("Save was not confirmed", "لم يُؤكَّد الحفظ")) }

        if (!student) graphControls()

        // Demo Notice & Value Tables
        DemoNotice(label(TextKey.t_729df17606))

        TextButton(
            onClick = { valuesExpanded = !valuesExpanded },
            modifier = Modifier.testTag("graph-values-toggle")
        ) {
            Text(
                text = if (valuesExpanded) tr("Hide value tables", "إخفاء جداول القيم")
                else tr("Show value tables", "عرض جداول القيم"),
                color = Color(0xFF168BFF),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (valuesExpanded) {
            SectionTitle(label(TextKey.t_1a25d7806a))
            functions
                .filter { it !in hidden }
                .forEach { f ->
                    AppCard {
                        MathText("y = $f")
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            (-2..2).forEach { x ->
                                val yVal = engine.evaluate(f, x.toDouble())
                                val yStr = yVal?.let { "%.3f".format(java.util.Locale.US, it) } ?: "—"
                                MathText("x=$x → y=$yStr")
                            }
                        }
                    }
                }
        }
    }
}

@Composable
fun FunctionLabHeader(isDark: Boolean) {
    if (LocalStudentExperience.current) {
        RefHeroCard(tr("Function lab", "مختبر الدوال"),
            tr("Explore functions and compare their graphs.", "استكشف الدوال وقارن رسومها البيانية."))
        return
    }
    val cardBg = if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else if (isDark) LabPalette.darkGraphBackground else LabPalette.lightCardBackground
    val borderColor = if (isDark) Color(0xFF168BFF).copy(alpha = 0.4f) else LabPalette.lightBorder
    val textColor = if (isDark) LabPalette.darkTextPrimary else LabPalette.lightTextPrimary
    val subtitleColor = if (isDark) LabPalette.darkTextSecondary else LabPalette.lightTextSecondary

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDark) 6.dp else 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isDark) 0.08f else 0.04f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("f(x)", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF168BFF))
                Text("π", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF31E6BF))
                Text("∫", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAF70FF))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isDark) Color(0xFF10243C) else Color(0xFFEEF2FF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.5.dp, Color(0xFF168BFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                        contentDescription = null,
                        tint = Color(0xFF168BFF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🧪",
                                fontSize = 20.sp
                            )
                            Text(
                                text = "مختبر الدوال",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = textColor
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "استكشف الدوال وراقب الرسوم البيانية بشكل تفاعلي",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = subtitleColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FunctionInputRow(
    input: String,
    onInputChange: (String) -> Unit,
    onAddFunction: () -> Unit,
    isDark: Boolean
) {
    if (LocalStudentExperience.current) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                AppTextField(input, onInputChange, "y = f(x)", Modifier.fillMaxWidth().testTag("graph-input"))
            }
            RefPrimaryButton(tr("Add function", "إضافة دالة"), onAddFunction, enabled = input.isNotBlank())
        }
        return
    }
    val inputBg = if (isDark) LabPalette.darkCardBackground else LabPalette.lightCardBackground
    val inputBorder = if (isDark) LabPalette.darkBorder else LabPalette.lightBorder
    val textColor = if (isDark) LabPalette.darkTextPrimary else LabPalette.lightTextPrimary
    val placeholderColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onAddFunction,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF168BFF),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.heightIn(min = 52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إضافة دالة",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = inputBg,
            border = BorderStroke(1.2.dp, inputBorder),
            shadowElevation = if (isDark) 2.dp else 1.dp,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = onInputChange,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = textColor,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("graph-input"),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (input.isEmpty()) {
                                    Text(
                                        text = "... sin(x) أو x^2 :مثال",
                                        color = placeholderColor,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "الدالة = y",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) LabPalette.darkTextSecondary else LabPalette.lightTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun InteractiveGraphCanvas(
    functions: List<String>,
    hidden: List<String>,
    colors: List<Color>,
    engine: DemoGraphEngine,
    zoom: Float,
    pan: Offset,
    grid: Boolean,
    axes: Boolean,
    onTransform: (delta: Offset, scale: Float) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val graphBg = if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else if (isDark) LabPalette.darkGraphBackground else LabPalette.lightGraphBackground
    val containerBorder = if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else if (isDark) Color(0xFF168BFF).copy(alpha = 0.8f) else Color(0xFF0066FF)
    val gridColorMain = if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else if (isDark) LabPalette.darkGrid else LabPalette.lightGrid
    val gridColorSubtle = if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else if (isDark) LabPalette.darkGridSubtle else LabPalette.lightGridSubtle
    val axisColor = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else if (isDark) LabPalette.darkAxis else LabPalette.lightAxis
    val tickTextColor = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val tickFontPixels = with(LocalDensity.current) { 10.sp.toPx() }
    val axisFontPixels = with(LocalDensity.current) { 12.sp.toPx() }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = graphBg,
        border = BorderStroke(1.5.dp, containerBorder),
        shadowElevation = if (isDark) 8.dp else 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clipToBounds()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("graph-canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, delta, factor, _ ->
                        onTransform(delta, factor)
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val viewport = GraphViewport(width, height, zoom, pan.x, pan.y)
            val unitScale = viewport.scale
            val origin = Offset(viewport.originX, viewport.originY)

            if (grid) {
                val minGridIndexX = floor((0f - origin.x) / unitScale).toInt() - 1
                val maxGridIndexX = ceil((width - origin.x) / unitScale).toInt() + 1
                val minGridIndexY = floor((0f - origin.y) / unitScale).toInt() - 1
                val maxGridIndexY = ceil((height - origin.y) / unitScale).toInt() + 1

                for (i in minGridIndexX * 2..maxGridIndexX * 2) {
                    val gx = origin.x + i * (unitScale / 2f)
                    if (gx in 0f..width && i % 2 != 0) {
                        drawLine(
                            color = gridColorSubtle,
                            start = Offset(gx, 0f),
                            end = Offset(gx, height),
                            strokeWidth = 0.7f.dp.toPx()
                        )
                    }
                }
                for (j in minGridIndexY * 2..maxGridIndexY * 2) {
                    val gy = origin.y + j * (unitScale / 2f)
                    if (gy in 0f..height && j % 2 != 0) {
                        drawLine(
                            color = gridColorSubtle,
                            start = Offset(0f, gy),
                            end = Offset(width, gy),
                            strokeWidth = 0.7f.dp.toPx()
                        )
                    }
                }

                for (i in minGridIndexX..maxGridIndexX) {
                    val gx = origin.x + i * unitScale
                    if (gx in 0f..width) {
                        drawLine(
                            color = gridColorMain,
                            start = Offset(gx, 0f),
                            end = Offset(gx, height),
                            strokeWidth = 1.2f.dp.toPx()
                        )
                    }
                }
                for (j in minGridIndexY..maxGridIndexY) {
                    val gy = origin.y + j * unitScale
                    if (gy in 0f..height) {
                        drawLine(
                            color = gridColorMain,
                            start = Offset(0f, gy),
                            end = Offset(width, gy),
                            strokeWidth = 1.2f.dp.toPx()
                        )
                    }
                }
            }

            if (axes) {
                val axisStroke = 2f.dp.toPx()

                if (origin.y in -50f..height + 50f) {
                    val clampedY = origin.y.coerceIn(10f, height - 10f)
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, clampedY),
                        end = Offset(width, clampedY),
                        strokeWidth = axisStroke
                    )

                    val arrowPathX = Path().apply {
                        moveTo(width - 2f, clampedY)
                        lineTo(width - 12f, clampedY - 6f)
                        lineTo(width - 12f, clampedY + 6f)
                        close()
                    }
                    drawPath(arrowPathX, axisColor)

                    val arrowPathXLeft = Path().apply {
                        moveTo(2f, clampedY)
                        lineTo(12f, clampedY - 6f)
                        lineTo(12f, clampedY + 6f)
                        close()
                    }
                    drawPath(arrowPathXLeft, axisColor)
                }

                if (origin.x in -50f..width + 50f) {
                    val clampedX = origin.x.coerceIn(10f, width - 10f)
                    drawLine(
                        color = axisColor,
                        start = Offset(clampedX, 0f),
                        end = Offset(clampedX, height),
                        strokeWidth = axisStroke
                    )

                    val arrowPathY = Path().apply {
                        moveTo(clampedX, 2f)
                        lineTo(clampedX - 6f, 12f)
                        lineTo(clampedX + 6f, 12f)
                        close()
                    }
                    drawPath(arrowPathY, axisColor)

                    val arrowPathYBottom = Path().apply {
                        moveTo(clampedX, height - 2f)
                        lineTo(clampedX - 6f, height - 12f)
                        lineTo(clampedX + 6f, height - 12f)
                        close()
                    }
                    drawPath(arrowPathYBottom, axisColor)
                }

                val paint = android.graphics.Paint().apply {
                    color = tickTextColor.toArgb()
                    textSize = tickFontPixels
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.SANS_SERIF
                }

                val maxLabelWidth = (viewport.xTicks.toList() + viewport.yTicks.toList())
                    .maxOfOrNull { paint.measureText(it.toString()) } ?: tickFontPixels
                val tickStepX = graphTickStep(unitScale, maxLabelWidth + 12.dp.toPx())
                val tickStepY = graphTickStep(unitScale, tickFontPixels + 12.dp.toPx())
                val minTickX = viewport.xTicks.first
                val maxTickX = viewport.xTicks.last

                for (i in minTickX..maxTickX) {
                    if (i != 0 && i % tickStepX == 0) {
                        val tx = origin.x + i * unitScale
                        if (tx > maxLabelWidth / 2f && tx < width - maxLabelWidth / 2f - 24.dp.toPx() &&
                            abs(tx - origin.x) > maxLabelWidth + 8.dp.toPx()) {
                            val clampedY = origin.y.coerceIn(15f, height - 25f)
                            drawLine(
                                color = axisColor,
                                start = Offset(tx, clampedY - 4f.dp.toPx()),
                                end = Offset(tx, clampedY + 4f.dp.toPx()),
                                strokeWidth = 1.5f.dp.toPx()
                            )
                            val labelText = i.toString()
                            paint.textAlign = android.graphics.Paint.Align.CENTER
                            drawContext.canvas.nativeCanvas.drawText(
                                labelText,
                                tx,
                                (clampedY + tickFontPixels + 6.dp.toPx()).coerceAtMost(height - 4.dp.toPx()),
                                paint
                            )
                        }
                    }
                }

                val minTickY = viewport.yTicks.first
                val maxTickY = viewport.yTicks.last

                for (j in minTickY..maxTickY) {
                    if (j != 0 && j % tickStepY == 0) {
                        val ty = origin.y - j * unitScale
                        if (ty > tickFontPixels + 24.dp.toPx() && ty < height - tickFontPixels &&
                            abs(ty - origin.y) > tickFontPixels + 12.dp.toPx()) {
                            val clampedX = origin.x.coerceIn(maxLabelWidth + 8.dp.toPx(), width - 20f)
                            drawLine(
                                color = axisColor,
                                start = Offset(clampedX - 4f.dp.toPx(), ty),
                                end = Offset(clampedX + 4f.dp.toPx(), ty),
                                strokeWidth = 1.5f.dp.toPx()
                            )
                            val labelText = j.toString()
                            paint.textAlign = android.graphics.Paint.Align.RIGHT
                            drawContext.canvas.nativeCanvas.drawText(
                                labelText,
                                clampedX - 6f.dp.toPx(),
                                ty + tickFontPixels / 3f,
                                paint
                            )
                        }
                    }
                }

                if (origin.x in 10f..width - 10f && origin.y in 10f..height - 10f) {
                    paint.textAlign = android.graphics.Paint.Align.RIGHT
                    drawContext.canvas.nativeCanvas.drawText(
                        "0",
                        origin.x - 6f.dp.toPx(),
                        origin.y + 14f.dp.toPx(),
                        paint
                    )
                }

                paint.color = axisColor.toArgb()
                paint.textSize = axisFontPixels
                paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC)

                drawContext.canvas.nativeCanvas.drawText(
                    "x",
                    width - 15f.dp.toPx(),
                    origin.y.coerceIn(20f, height - 20f) + 16f.dp.toPx(),
                    paint
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "y",
                    origin.x.coerceIn(20f, width - 20f) + 10f.dp.toPx(),
                    20f.dp.toPx(),
                    paint
                )
            }

            functions.forEachIndexed { index, expr ->
                if (expr !in hidden) {
                    val curveColor = colors[index % colors.size]
                    val path = Path()
                    var startNewSegment = true
                    var lastSy = Float.NaN

                    for (px in 0..width.toInt() step 2) {
                        val mathX = viewport.mathX(px.toFloat())
                        val evalY = engine.evaluate(expr, mathX.toDouble())
                        val sy = if (evalY == null) Float.NaN else viewport.screenY(evalY.toFloat())

                        if (!sy.isFinite() || sy < -height * 2 || sy > height * 3 ||
                            (!startNewSegment && abs(sy - lastSy) > height / 2f)
                        ) {
                            startNewSegment = true
                            lastSy = sy
                            continue
                        }

                        if (startNewSegment) {
                            path.moveTo(px.toFloat(), sy)
                            startNewSegment = false
                        } else {
                            path.lineTo(px.toFloat(), sy)
                        }
                        lastSy = sy
                    }

                    if (isDark) {
                        drawPath(
                            path = path,
                            color = curveColor.copy(alpha = 0.35f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 6f.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }

                    drawPath(
                        path = path,
                        color = curveColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.8f.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FunctionCard(
    index: Int,
    functionExpr: String,
    isVisible: Boolean,
    color: Color,
    onToggleVisibility: (Boolean) -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else if (isDark) LabPalette.darkCardBackground else LabPalette.lightCardBackground
    val cardBorder = if (isDark) color.copy(alpha = 0.5f) else color.copy(alpha = 0.3f)
    val textColor = color
    val descColor = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else if (isDark) LabPalette.darkTextSecondary else LabPalette.lightTextSecondary
    val description = getFunctionDescription(functionExpr, LocalLanguage.current == com.ahmadmol.enmath.core.model.Language.Arabic)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.2.dp, cardBorder),
        shadowElevation = if (isDark) 4.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("graph-function-$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = if (isDark) Color(0xFF1B2B42) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "حذف الدالة",
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(color, CircleShape)
                )

                Column {
                    CompositionLocalProvider(
                        LocalLayoutDirection provides LayoutDirection.Ltr
                    ) {
                        val displayExpr = if (functionExpr.startsWith("y=") || functionExpr.startsWith("f(x)=")) {
                            functionExpr
                        } else {
                            "y = $functionExpr"
                        }.replace("^2", "²")

                        Text(
                            text = displayExpr,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = textColor
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = descColor
                    )
                }
            }

            Checkbox(
                checked = isVisible,
                onCheckedChange = onToggleVisibility,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF168BFF),
                    uncheckedColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                    checkmarkColor = Color.White
                )
            )
        }
    }
}

@Composable
fun GraphControlBar(
    zoom: Float,
    grid: Boolean,
    axes: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    onToggleGrid: (Boolean) -> Unit,
    onToggleAxes: (Boolean) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    if (LocalStudentExperience.current) {
        StudentGraphControls(zoom, grid, axes, onZoomIn, onZoomOut, onReset, onToggleGrid, onToggleAxes, modifier)
        return
    }
    val barBg = if (isDark) LabPalette.darkCardBackground else LabPalette.lightCardBackground
    val barBorder = if (isDark) LabPalette.darkBorder else LabPalette.lightBorder
    val textColor = if (isDark) LabPalette.darkTextPrimary else LabPalette.lightTextPrimary
    val buttonBg = if (isDark) LabPalette.darkElevatedSurface else Color(0xFFF1F5F9)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = barBg,
        border = BorderStroke(1.2.dp, barBorder),
        shadowElevation = if (isDark) 4.dp else 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBg,
                    contentColor = Color(0xFF168BFF)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("graph-control-2")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = "إعادة العرض",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "إعادة العرض",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onToggleAxes(!axes) }
                ) {
                    Checkbox(
                        checked = axes,
                        onCheckedChange = onToggleAxes,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF168BFF),
                            uncheckedColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "محاور",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onToggleGrid(!grid) }
                ) {
                    Checkbox(
                        checked = grid,
                        onCheckedChange = onToggleGrid,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF168BFF),
                            uncheckedColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "شبكة",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier
                        .size(38.dp)
                        .background(buttonBg, CircleShape)
                        .border(1.dp, Color(0xFF168BFF).copy(alpha = 0.5f), CircleShape)
                        .testTag("graph-control-0")
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF168BFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier
                        .size(38.dp)
                        .background(buttonBg, CircleShape)
                        .border(1.dp, Color(0xFF168BFF).copy(alpha = 0.5f), CircleShape)
                        .testTag("graph-control-1")
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF168BFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudentGraphControls(zoom: Float, grid: Boolean, axes: Boolean,
    onZoomIn: () -> Unit, onZoomOut: () -> Unit, onReset: () -> Unit,
    onToggleGrid: (Boolean) -> Unit, onToggleAxes: (Boolean) -> Unit, modifier: Modifier) {
    PremiumHeroCard(modifier) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm, Alignment.CenterHorizontally)) {
                OutlinedIconButton(onZoomOut, Modifier.size(48.dp).testTag("graph-control-0"), enabled = zoom > .25f) {
                    Text("−", modifier = Modifier.semantics { contentDescription = "Zoom out" })
                }
                Text("× " + String.format(java.util.Locale.US, "%.2f", zoom), style = MaterialTheme.typography.bodyMedium)
                OutlinedIconButton(onZoomIn, Modifier.size(48.dp).testTag("graph-control-1"), enabled = zoom < 10f) {
                    Text("+", modifier = Modifier.semantics { contentDescription = "Zoom in" })
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            OutlinedButton(onReset, Modifier.heightIn(min = 48.dp).testTag("graph-control-2")) { Text(tr("Reset view", "إعادة العرض")) }
            FilterChip(selected = axes, onClick = { onToggleAxes(!axes) }, label = { Text(tr("Axes", "المحاور")) }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = grid, onClick = { onToggleGrid(!grid) }, label = { Text(tr("Grid", "الشبكة")) }, modifier = Modifier.heightIn(min = 48.dp))
        }
    }
}
