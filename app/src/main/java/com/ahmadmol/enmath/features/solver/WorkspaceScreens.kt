package com.ahmadmol.enmath.features.solver

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

/** Helper theme colors for Math Solver UI */
object SolverColors {
    val darkMainContainer = Color(0xFF10243C)
    val darkInputBg = Color(0xFF142D4D)
    val primaryBlue = Color(0xFF168BFF)
    val cyanAccent = Color(0xFF23C6F5)
    val purpleAccent = Color(0xFF9462F5)
    val darkPrimaryText = Color(0xFFF5F8FF)
    val darkSecondaryText = Color(0xFFA6B7D2)
    val darkBorder = Color(0xFF285D98)

    @Composable
    fun isDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

    @Composable
    fun containerBackground(): Color = if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else if (isDark()) darkMainContainer else Color(0xFFFFFFFF)

    @Composable
    fun inputBackground(): Color = if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else if (isDark()) darkInputBg else Color(0xFFF0F6FF)

    @Composable
    fun primaryText(): Color = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else if (isDark()) darkPrimaryText else Color(0xFF0F172A)

    @Composable
    fun secondaryText(): Color = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else if (isDark()) darkSecondaryText else Color(0xFF475569)
}

/** Header section with decorative math canvas graphics and title */
@Composable
fun MathSolverHeader() {
    val isDark = SolverColors.isDark()
    val secondaryColor = SolverColors.secondaryText()
    val primaryColor = SolverColors.primaryText()
    val cyanColor = SolverColors.cyanAccent
    val blueColor = SolverColors.primaryBlue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D1B2A), Color(0xFF081426)),
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFEAF2FF), Color(0xFFF4F7FC)),
                    )
                }
            )
            .padding(horizontal = Space.lg, vertical = Space.sm)
    ) {
        // Background Decorative Math Graphics (Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Star dots
            val dotAlpha = if (isDark) 0.5f else 0.25f
            drawCircle(cyanColor.copy(alpha = dotAlpha), radius = 3.dp.toPx(), center = Offset(width * 0.12f, height * 0.25f))
            drawCircle(blueColor.copy(alpha = dotAlpha * 0.8f), radius = 2.5.dp.toPx(), center = Offset(width * 0.26f, height * 0.75f))
            drawCircle(cyanColor.copy(alpha = dotAlpha * 0.6f), radius = 2.dp.toPx(), center = Offset(width * 0.38f, height * 0.35f))

            // Parabola graph on left side
            val axisXStart = width * 0.05f
            val axisXEnd = width * 0.35f
            val axisYTop = height * 0.12f
            val axisYBottom = height * 0.88f
            val axisColor = blueColor.copy(alpha = if (isDark) 0.3f else 0.18f)

            drawLine(axisColor, Offset(axisXStart, (axisYTop + axisYBottom) / 2), Offset(axisXEnd, (axisYTop + axisYBottom) / 2), strokeWidth = 1.dp.toPx())
            drawLine(axisColor, Offset((axisXStart + axisXEnd) / 2, axisYTop), Offset((axisXStart + axisXEnd) / 2, axisYBottom), strokeWidth = 1.dp.toPx())

            val curvePath = Path()
            val centerX = (axisXStart + axisXEnd) / 2
            val centerY = ((axisYTop + axisYBottom) / 2) + 8.dp.toPx()
            val scaleX = (axisXEnd - axisXStart) / 2
            val scaleY = (axisYBottom - axisYTop) * 0.6f

            var first = true
            val steps = 30
            for (i in 0..steps) {
                val t = (i.toFloat() / steps) * 2f - 1f
                val x = centerX + (t * scaleX * 0.8f)
                val y = centerY - ((t * t) * scaleY * 0.8f)
                if (first) {
                    curvePath.moveTo(x, y)
                    first = false
                } else {
                    curvePath.lineTo(x, y)
                }
            }
            drawPath(
                path = curvePath,
                color = cyanColor.copy(alpha = if (isDark) 0.8f else 0.5f),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Math symbols (π and f(x))
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "π",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = cyanColor.copy(alpha = if (isDark) 0.85f else 0.6f)
                )
            )
            Spacer(Modifier.width(32.dp))
            Text(
                "f(x)",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Serif,
                    color = blueColor.copy(alpha = if (isDark) 0.75f else 0.5f)
                )
            )
        }

        // Title text aligned to RTL
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tr("Math Workspace", "مساحة الرياضيات"),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = secondaryColor
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("Understand every step", "افهم كل خطوة"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryColor
                )
                Text(
                    text = ".",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = cyanColor
                )
            }
        }
    }
}

/** Three input mode tabs: Device Keyboard, Math Keyboard, Camera */
@Composable
fun SolverInputModeTabs(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    if (LocalStudentExperience.current) {
        RefCategoryTabs(
            listOf(
                RefTabItem("0", tr("Device keyboard", "لوحة الجهاز"), Icons.Outlined.Calculate),
                RefTabItem("1", tr("Math keyboard", "لوحة رياضية"), Icons.Outlined.Edit),
                RefTabItem("2", tr("Trial camera", "كاميرا تجريبية"), Icons.Outlined.PhotoCamera),
            ), selectedMode.toString(), { onModeSelected(it.toInt()) },
        )
        return
    }
    val isDark = SolverColors.isDark()
    val activeBg = (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF))
    val activeText = (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onPrimary else Color.White)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf(
            Triple(0, tr("Device Keyboard", "لوحة الجهاز"), Icons.Outlined.Calculate),
            Triple(1, tr("Math Keyboard", "لوحة رياضية"), Icons.Outlined.Edit),
            Triple(2, tr("Trial Camera", "كاميرا تجريبية"), Icons.Outlined.PhotoCamera)
        )

        tabs.forEach { (index, title, icon) ->
            val isSelected = selectedMode == index
            Surface(
                onClick = { onModeSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .studentMinHeight(44.dp)
                    .testTag("solver-tab-$index"),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) activeBg else if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF142D4D)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF0F4FA)),
                border = if (!isSelected) BorderStroke(1.dp, if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF285D98)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD3E0EE))) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) activeText else if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFA6B7D2)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF475569)),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp
                        ),
                        color = if (isSelected) activeText else if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else Color(0xFFF5F8FF)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else Color(0xFF0F172A)),
                        maxLines = if (LocalStudentExperience.current) Int.MAX_VALUE else 1
                    )
                }
            }
        }
    }
}

/** Mathematical Expression Editor Card with LTR formatting */
@Composable
fun MathematicalExpressionEditor(
    fieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = SolverColors.isDark()
    val bgColor = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF142D4D)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF0F6FF))
    val borderColor = (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(18.dp)),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.md)
        ) {
            // Header row inside card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle badge with fx
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFF1A3B66)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE2EDFB))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "fx",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF))
                        )
                    )
                }

                // Label: التعبير الرياضي
                Text(
                    text = tr("Mathematical Expression", "التعبير الرياضي"),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SolverColors.secondaryText()
                )
            }

            Spacer(Modifier.height(10.dp))

            // Mathematical LTR TextField
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                BasicTextField(
                    value = fieldValue,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .testTag("solver-input"),
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = SolverColors.primaryText(),
                        textDirection = TextDirection.Ltr
                    ),
                    cursorBrush = SolidColor((if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF))),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (fieldValue.text.isEmpty()) {
                                Text(
                                    text = tr("Enter an equation or math expression...", "أدخل معادلة أو تعبيرًا رياضيًا..."),
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = SolverColors.secondaryText().copy(alpha = 0.6f),
                                        textDirection = if (LocalStudentExperience.current) TextDirection.Content else TextDirection.Rtl
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

/** Toolbar with Clear, Delete, and Undo actions */
@Composable
fun SolverActionToolbar(
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onUndo: () -> Unit
) {
    val isDark = SolverColors.isDark()
    val actionBg = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF142D4D)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF0F4FA))
    val actionText = (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clear (مسح)
        Surface(
            onClick = onClear,
            modifier = Modifier
                .weight(1f)
                .studentMinHeight(38.dp)
                .testTag("solver-clear"),
            shape = RoundedCornerShape(10.dp),
            color = actionBg,
            border = BorderStroke(1.dp, if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF285D98)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD3E0EE)))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CleaningServices,
                    contentDescription = null,
                    tint = actionText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = tr("Clear", "مسح"),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = actionText
                )
            }
        }

        // Delete (حذف)
        Surface(
            onClick = onDelete,
            modifier = Modifier
                .weight(1f)
                .studentMinHeight(38.dp)
                .testTag("solver-delete"),
            shape = RoundedCornerShape(10.dp),
            color = actionBg,
            border = BorderStroke(1.dp, if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF285D98)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD3E0EE)))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = null,
                    tint = actionText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = tr("Delete", "حذف"),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = actionText
                )
            }
        }

        // Undo (تراجع)
        Surface(
            onClick = onUndo,
            modifier = Modifier
                .weight(1f)
                .studentMinHeight(38.dp)
                .testTag("solver-undo"),
            shape = RoundedCornerShape(10.dp),
            color = actionBg,
            border = BorderStroke(1.dp, if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF285D98)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD3E0EE)))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = null,
                    tint = actionText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = tr("Undo", "تراجع"),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = actionText
                )
            }
        }
    }
}

/** Primary Solve Button ("حل الآن") */
@Composable
fun PrimarySolveButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColors = if (enabled) {
        listOf((if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF0F2B5B)), (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF1E3A8A)))
    } else {
        listOf((if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF1F324B)), (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF1F324B)))
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .studentMinHeight(54.dp)
            .testTag("solve"),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = if (enabled) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(bgColors))
                .padding(horizontal = Space.lg),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Translucent circle with arrow
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (enabled) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onPrimary else Color.White).copy(alpha = 0.2f) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF142438))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = if (enabled) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onPrimary else Color.White) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF6B809B)),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Main Text
                Text(
                    text = tr("Solve Now", "حل الآن"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = if (enabled) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onPrimary else Color.White) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF6B809B))
                )

                // Sparkle Icon
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = if (enabled) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onPrimary else Color.White) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF6B809B)),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/** Info banner for supported examples */
@Composable
fun SupportedExamplesInfoCard(
    onInfoClick: () -> Unit
) {
    val isDark = SolverColors.isDark()
    val bgColor = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else Color(0xFF10243C)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else Color(0xFFFFFFFF))
    val borderColor = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF1F4875)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD3E0EE))

    Surface(
        onClick = onInfoClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = tr(
                        "Curated trial solver • Seven supported examples, no general engine",
                        "حل تجريبي منتقى • سبعة أمثلة مدعومة، دون محرك عام"
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = SolverColors.secondaryText(),
                    maxLines = if (LocalStudentExperience.current) Int.MAX_VALUE else 1
                )
            }
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = SolverColors.cyanAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Supported Examples Section */
@Composable
fun SupportedExamplesSection(
    examples: List<ProblemExample>,
    onSelectExample: (String) -> Unit
) {
    val isDark = SolverColors.isDark()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = tr("Supported Examples", "أمثلة مدعومة"),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = SolverColors.primaryText()
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = SolverColors.primaryText(),
                modifier = Modifier.size(22.dp)
            )
        }

        // Example Cards
        examples.forEachIndexed { index, example ->
            val iconSymbol = when {
                example.expression.contains("=") -> "x²"
                example.expression.contains("d/dx") -> "d/dx"
                example.expression.contains("lim") -> "lim"
                example.expression.contains("∫") -> "∫"
                example.expression.contains("det") -> "det"
                else -> "f(x)"
            }

            Surface(
                onClick = { onSelectExample(example.expression) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("example-$index"),
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else Color(0xFF10243C)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else Color(0xFFFFFFFF)),
                border = BorderStroke(1.dp, if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF1F4875)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD0DBEA)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Space.md)
                ) {
                    // Category & Icon Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SolverColors.purpleAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iconSymbol,
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.surface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                        }

                        // Category Title + Chevron
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = example.category.text(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SolverColors.primaryText()
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = SolverColors.secondaryText(),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Expression Inner Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF081426)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF0F4FA)),
                        border = BorderStroke(0.8.dp, if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF1C385C)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE2EDFB)))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = example.expression,
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = SolverColors.primaryText(),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Mathematical Custom Keyboard component matching Image 2 */
@Composable
fun MathKeyboard(group: Int, onGroup: (Int) -> Unit, onKey: (String) -> Unit) {
    val isDark = SolverColors.isDark()
    val categories = listOf(
        tr("Numbers", "أرقام"),
        tr("Arithmetic", "حساب"),
        tr("Algebra", "جبر"),
        tr("Analysis", "تحليل"),
        tr("Trigonometry", "مثلثات")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Horizontal Row of Symbol Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEachIndexed { index, catName ->
                val isSelected = group == index
                Surface(
                    onClick = { onGroup(index) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) {
                        if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.secondaryContainer else Color(0xFF0D5C4D)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFA7F3D0))
                    } else {
                        if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF142D4D)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9))
                    },
                    border = if (!isSelected) BorderStroke(1.dp, if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFF285D98)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0))) else null,
                    modifier = Modifier.then(if (LocalStudentExperience.current) Modifier.heightIn(min = 48.dp) else Modifier).testTag("math-group-$index")
                ) {
                    Text(
                        text = catName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = if (isSelected) {
                            if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFFECFDF5)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF064E3B))
                        } else {
                            if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFA6B7D2)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF475569))
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Keyboard Key Layout per Category
        val keyRows: List<List<String>> = remember(group) {
            when (group) {
                0 -> listOf( // "أرقام"
                    listOf("(", ")", "x"),
                    listOf("y", "π", "e"),
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "−"),
                    listOf("0", ".", "=", "+")
                )
                1 -> listOf( // "حساب"
                    listOf("+", "−", "×", "÷"),
                    listOf("a/b", "abs(", "%", "^"),
                    listOf("(", ")", "=", "."),
                    listOf("7", "8", "9", "0"),
                    listOf("4", "5", "6", "1"),
                    listOf("2", "3", "x", "y")
                )
                2 -> listOf( // "جبر"
                    listOf("x", "y", "z", "="),
                    listOf("a", "b", "x²", "xʸ"),
                    listOf("√", "e", "π", "abs("),
                    listOf("(", ")", ",", "/"),
                    listOf("7", "8", "9", "4"),
                    listOf("5", "6", "1", "2"),
                    listOf("3", "0", ".", "+")
                )
                3 -> listOf( // "تحليل"
                    listOf("lim", "x→", "d/dx", "∫"),
                    listOf("dx", "∞", "∑", "f(x)"),
                    listOf("(", ")", "^", "="),
                    listOf("7", "8", "9", "4"),
                    listOf("5", "6", "1", "2"),
                    listOf("3", "0", ".", "+")
                )
                4 -> listOf( // "مثلثات"
                    listOf("sin", "cos", "tan", "π"),
                    listOf("θ", "asin", "acos", "atan"),
                    listOf("(", ")", "^", "/"),
                    listOf("7", "8", "9", "4"),
                    listOf("5", "6", "1", "2"),
                    listOf("3", "0", ".", "+")
                )
                else -> listOf(
                    listOf("(", ")", "x", "y"),
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "−"),
                    listOf("0", ".", "=", "+")
                )
            }
        }

        // Render Button Grid
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                keyRows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowKeys.forEach { keyLabel ->
                            val keyBg = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.secondaryContainer else Color(0xFF1E3A5F)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFD1FAE5))
                            val keyText = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else Color(0xFFF5F8FF)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else Color(0xFF0F172A))

                            Surface(
                                onClick = {
                                    val mapped = when (keyLabel) {
                                        "x²" -> "x^2"
                                        "xʸ" -> "^"
                                        "sin" -> "sin("
                                        "cos" -> "cos("
                                        "tan" -> "tan("
                                        "asin" -> "asin("
                                        "acos" -> "acos("
                                        "atan" -> "atan("
                                        "lim" -> "lim x→"
                                        "d/dx" -> "d/dx("
                                        "√" -> "√("
                                        "a/b" -> "/"
                                        else -> keyLabel
                                    }
                                    onKey(mapped)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .studentMinHeight(44.dp)
                                    .testTag("key-$keyLabel"),
                                shape = RoundedCornerShape(10.dp),
                                color = keyBg
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = keyLabel,
                                        style = TextStyle(
                                            fontSize = if (keyLabel.length > 3) 12.sp else 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Serif,
                                            color = keyText
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Main Math Solver Workspace Screen */
@Composable
fun SolverWorkspace(
    vm: SolverViewModel,
    state: LearningState,
    onSolve: (String, String) -> Unit,
    onRoute: (String) -> Unit,
) {
    val expression by vm.expression.collectAsStateWithLifecycle()
    val student = LocalStudentExperience.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var field by rememberSaveable(state.activeStudentId, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(expression, TextRange(expression.length)))
    }
    LaunchedEffect(expression) {
        if (expression != field.text)
            field = TextFieldValue(expression, TextRange(expression.length))
    }
    var group by rememberSaveable { mutableIntStateOf(0) }
    var mode by rememberSaveable { mutableIntStateOf(1) } // Math Keyboard selected by default (Image 2)
    var showInfoDialog by remember { mutableStateOf(false) }
    var showUnsupportedDialog by remember { mutableStateOf(false) }

    val defaultExamples = remember {
        listOf(
            ProblemExample(Copy("Equations", "معادلات"), "2x + 4 = 10"),
            ProblemExample(Copy("Derivatives", "اشتقاق"), "d/dx(x^2)"),
            ProblemExample(Copy("Limits", "نهايات"), "lim x→1 (x^2-1)/(x-1)"),
            ProblemExample(Copy("Integrals", "تكامل"), "∫ 2x dx"),
            ProblemExample(Copy("Matrices", "مصفوفات"), "det[[1,2],[3,4]]"),
            ProblemExample(Copy("Probability", "احتمالات"), "p(evenonfairdie)")
        )
    }

    val displayExamples = remember(state.examples) {
        if (state.examples.isNotEmpty()) {
            (state.examples + defaultExamples).distinctBy { it.expression }
        } else {
            defaultExamples
        }
    }

    fun insert(raw: String) {
        val key = if (raw == "a/b") "/" else raw
        val start = field.selection.min
        val end = field.selection.max
        val text = field.text.replaceRange(start, end, key)
        field = TextFieldValue(text, TextRange(start + key.length))
        vm.edit(text)
    }

    fun deleteChar() {
        val start = field.selection.min
        val end = field.selection.max
        if (start != end) {
            val text = field.text.removeRange(start, end)
            field = TextFieldValue(text, TextRange(start))
            vm.edit(text)
        } else if (start > 0) {
            val text = field.text.removeRange(start - 1, start)
            field = TextFieldValue(text, TextRange(start - 1))
            vm.edit(text)
        }
    }

    ScreenPage(Modifier.testTag("screen-solver")) {
        // Header
        MathSolverHeader()

        // Main Solver Container Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SolverColors.containerBackground(),
            border = BorderStroke(1.2.dp, SolverColors.darkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Space.md),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                // Input Mode Tabs
                SolverInputModeTabs(
                    selectedMode = mode,
                    onModeSelected = {
                        mode = if (student && it == 2) 1 else it
                        if (student) {
                            focus.clearFocus()
                            keyboard?.hide()
                        }
                        if (it == 2) onRoute("solver/scan")
                    }
                )

                // Input Field
                MathematicalExpressionEditor(
                    fieldValue = field,
                    onValueChange = {
                        field = it
                        vm.edit(it.text)
                    }
                )

                // Action Toolbar
                SolverActionToolbar(
                    onClear = {
                        field = TextFieldValue("")
                        vm.edit("")
                    },
                    onDelete = ::deleteChar,
                    onUndo = vm::undo
                )

                if (!student && mode == 1) {
                    MathKeyboard(group, { group = it }, ::insert)
                }
                // Primary Solve Button
                PrimarySolveButton(
                    enabled = expression.isNotBlank(),
                    onClick = {
                        if (student) {
                            focus.clearFocus()
                            keyboard?.hide()
                        }
                        if (vm.solve()) {
                            val sol = vm.solution
                            if (sol.answer != null || sol.steps.isNotEmpty()) {
                                onSolve(expression, sol.topicId)
                            } else {
                                showUnsupportedDialog = true
                            }
                        }
                    }
                )
                // Keep expression, tools and Solve together before the scrollable symbol grid.
                if (student && mode == 1) {
                    MathKeyboard(group, { group = it }) {
                        focus.clearFocus()
                        keyboard?.hide()
                        insert(it)
                    }
                }
            }
        }

        // Info Banner
        SupportedExamplesInfoCard(
            onInfoClick = { showInfoDialog = true }
        )

        // Supported Examples List
        SupportedExamplesSection(
            examples = displayExamples,
            onSelectExample = { expr ->
                vm.edit(expr)
                field = TextFieldValue(expr, TextRange(expr.length))
            }
        )

        // Action links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            SecondaryButton(
                label = label(TextKey.t_ae1c6ca083),
                onClick = { onRoute("solver/history") },
                modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                label = label(TextKey.t_a7e385f50a),
                onClick = { onRoute("explore") },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(tr("Supported Examples & Limits", "إمكانيات محرك الحل والحدود")) },
            text = {
                Text(
                    tr(
                        "The current engine supports selected worked examples in Limits, Derivatives, Integrals, Equations, Matrices, and Probability with full curriculum explanations.",
                        "يدعم محرك الحل الحالي أمثلة منتقاة ومحسوبة بدقة في مواضيع النهايات والاشتقاق والتكامل والمعادلات والمصفوفات والاحتمالات مع خطوات تعليمية مفصلة."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(tr("Got it", "فهمت ذلك"))
                }
            }
        )
    }

    // Unsupported Dialog
    if (showUnsupportedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsupportedDialog = false },
            title = { Text(tr("Unsupported Expression", "تعبير غير مدعوم حالياً")) },
            text = {
                Text(
                    tr(
                        "The current engine supports specific curated Baccalaureate examples. Please choose one of the supported examples below.",
                        "المحرك الحالي يدعم نماذج واختبارات شهادة البكالوريا الموضحة في الأمثلة أدناه. الرجاء اختيار أحد الأمثلة المدعومة."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showUnsupportedDialog = false }) {
                    Text(tr("OK", "حسناً"))
                }
            }
        )
    }
}

@Composable
fun ProductSolutionScreen(
    solution: Solution,
    state: LearningState,
    saved: Boolean,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onLesson: (String) -> Unit,
    onExample: (String) -> Unit,
) {
    var method by rememberSaveable(solution.expression) { mutableIntStateOf(0) }
    var hint by rememberSaveable(solution.expression) { mutableIntStateOf(0) }
    var share by remember { mutableStateOf(false) }
    ScreenPage(Modifier.testTag("screen-solution")) {
        if (LocalStudentExperience.current) RefHeroCard(label(TextKey.t_1dd6ad45d7), label(TextKey.t_7a0c16fd84), artwork = { SolverHistoryHeroArtwork() })
        else PageHeading(label(TextKey.t_7a0c16fd84), label(TextKey.t_1dd6ad45d7))
        DemoNotice(label(TextKey.t_1df282cbf8))
        EquationCard(solution.expression)
        if (solution.answer == null) {
            EmptyState(label(TextKey.t_93efc76003), label(TextKey.t_2f5557f305))
            state.examples.forEach { e ->
                AppCard(onClick = { onExample(e.expression) }) {
                    MathText(e.expression)
                    SecondaryButton(label(TextKey.t_1931081814), { onExample(e.expression) })
                }
            }
        } else {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Space.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Text(
                        label(TextKey.t_b067c69b60),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    MathText(solution.answer, MaterialTheme.typography.headlineLarge)
                }
            }
            if (solution.methods.isNotEmpty())
                ChipRow(solution.methods.map { it.name.text() }, method, { method = it })
            val steps = solution.methods.getOrNull(method)?.steps ?: solution.steps
            steps.forEachIndexed { i, step ->
                var expand by
                    rememberSaveable(solution.expression, method, i) { mutableStateOf(true) }
                AppCard(onClick = { expand = !expand }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.md),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(
                                Modifier.size(Space.touch),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "${i + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                label(TextKey.t_377ff08181, i + 1),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(step.title.text(), style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            if (expand) "−" else "+",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    MathText(step.expression)
                    if (expand) {
                        MathProse(step.explanation.text())
                        MathProse(step.reason.text(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            TextButton({ hint = (hint + 1).coerceAtMost(steps.size) }) {
                Text(label(TextKey.t_98340e1f7e))
            }
            steps.take(hint).forEach { DemoNotice(it.reason.text()) }
            val topic = state.topic(solution.topicId) ?: if (!LocalStudentExperience.current) state.topic("derivatives") else null
            if (LocalStudentExperience.current) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    OutlinedButton(onSave, Modifier.heightIn(min = 48.dp)) {
                        Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null)
                        Spacer(Modifier.width(Space.xs))
                        Text(tr(if (saved) "Saved" else "Save", if (saved) "محفوظ" else "حفظ"))
                    }
                    OutlinedButton({ share = true }, Modifier.heightIn(min = 48.dp)) {
                        Text(label(TextKey.t_9d8c72545a))
                    }
                    topic?.lessons?.firstOrNull()?.let { lesson ->
                        OutlinedButton({ onLesson(lesson.id) }, Modifier.heightIn(min = 48.dp)) {
                            Text(label(TextKey.t_6a87ec3397))
                        }
                    }
                }
            } else {
                BookmarkButton(saved, onSave)
                SecondaryButton(label(TextKey.t_9d8c72545a), { share = true })
                topic?.lessons?.firstOrNull()?.let { lesson ->
                    SecondaryButton(label(TextKey.t_6a87ec3397), { onLesson(lesson.id) })
                }
            }
            SectionTitle(label(TextKey.t_ecd7a0348c))
            val suggestions = if (LocalStudentExperience.current)
                similarPractice(state.questions, solution.topicId, solution.expression)
            else state.questions.filter { it.topicId == topic?.id }.take(2)
            if (suggestions.isEmpty()) {
                Text(tr("No additional practice for this solution yet.", "لا توجد تدريبات إضافية لهذا الحل حاليًا."),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            suggestions.forEach { q -> key(q.id) {
                    AppCard(onClick = { onExample(q.expression) }) {
                        MathText(q.expression)
                        SecondaryButton(label(TextKey.t_6841cd8fe8), { onExample(q.expression) })
                    }
                } }
        }
        PrimaryButton(label(TextKey.t_f762e78c7a), onEdit, Modifier.testTag("edit-problem"))
    }
    if (share)
        AlertDialog(
            onDismissRequest = { share = false },
            title = { Text(label(TextKey.t_606419751d)) },
            text = { Text(label(TextKey.t_b097319490)) },
            confirmButton = { TextButton({ share = false }) { Text(label(TextKey.t_115c64b035)) } },
        )
}

/** Experimental Camera Scanner Screen matching Image 1 */
@Composable
fun SolverScanScreen(onRecognized: (String) -> Unit) {
    val isDark = SolverColors.isDark()
    val secondaryColor = SolverColors.secondaryText()
    val primaryColor = SolverColors.primaryText()
    val cyanColor = SolverColors.cyanAccent
    val blueColor = SolverColors.primaryBlue

    ScreenPage(Modifier.testTag("screen-scan")) {
        // Hero title & banner section with math background canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0D1B2A), Color(0xFF081426))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFEAF2FF), Color(0xFFF4F7FC))
                        )
                    }
                )
                .padding(Space.lg)
        ) {
            // Background Canvas graphics (Pi, graph lines, grid dots)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val dotAlpha = if (isDark) 0.4f else 0.2f
                drawCircle(cyanColor.copy(alpha = dotAlpha), radius = 3.dp.toPx(), center = Offset(width * 0.15f, height * 0.3f))
                drawCircle(blueColor.copy(alpha = dotAlpha), radius = 2.dp.toPx(), center = Offset(width * 0.35f, height * 0.7f))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Right Pill Badge: "مسح تجريبي"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ) {
                        Text(
                            text = tr("Trial Scan", "مسح تجريبي"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Title: "مساحة التعرّف المستقبلية"
                Text(
                    text = tr("Future Recognition Workspace", "مساحة التعرّف المستقبلية"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryColor
                )

                // Subtitle / Info: "كاميرا ورُفع ومحاكيات، لا تُلتقط أو تُعالج أي صورة."
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = cyanColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = tr(
                            "Simulated camera and upload. No image is captured or processed.",
                            "كاميرا ورُفع ومحاكيات، لا تُلتقط أو تُعالج أي صورة."
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = secondaryColor
                    )
                }
            }
        }

        // Camera Preview Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(22.dp),
            color = if (isDark) Color(0xFF0D1F38) else Color(0xFFF0F6FF),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A60) else Color(0xFFD0E1F9))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Space.lg),
                contentAlignment = Alignment.Center
            ) {
                // Blue Scan Corner Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cornerLen = 24.dp.toPx()
                    val strokeW = 3.5.dp.toPx()
                    val color = Color(0xFF168BFF)

                    // Top-Left corner
                    drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = strokeW)
                    drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth = strokeW)

                    // Top-Right corner
                    drawLine(color, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeWidth = strokeW)
                    drawLine(color, Offset(w, 0f), Offset(w, cornerLen), strokeWidth = strokeW)

                    // Bottom-Left corner
                    drawLine(color, Offset(0f, h), Offset(cornerLen, h), strokeWidth = strokeW)
                    drawLine(color, Offset(0f, h), Offset(0f, h - cornerLen), strokeWidth = strokeW)

                    // Bottom-Right corner
                    drawLine(color, Offset(w, h), Offset(w - cornerLen, h), strokeWidth = strokeW)
                    drawLine(color, Offset(w, h), Offset(w, h - cornerLen), strokeWidth = strokeW)
                }

                // Equation Preview Center Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF10243C) else Color(0xFFFFFFFF),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A60) else Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "2x + 4 = 10",
                                style = TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = if (isDark) Color(0xFFF5F8FF) else Color(0xFF0F172A)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Simulation Action Buttons
        // Button 1: "محاكاة التصوير"
        Surface(
            onClick = { onRecognized("2x + 4 = 10") },
            modifier = Modifier
                .fillMaxWidth()
                .studentMinHeight(56.dp)
                .testTag("simulate-capture"),
            shape = RoundedCornerShape(18.dp),
            color = if (isDark) Color(0xFF10243C) else Color(0xFFFFFFFF),
            border = BorderStroke(1.5.dp, Color(0xFF60A5FA)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Space.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Icon Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E3A60) else Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = Color(0xFF168BFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Text
                Text(
                    text = tr("Simulate capture", "محاكاة التصوير"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF168BFF)
                )

                // Right Arrow
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null,
                    tint = Color(0xFF168BFF),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Button 2: "محاكاة الرفع والقص"
        Surface(
            onClick = { onRecognized("2x + 4 = 10") },
            modifier = Modifier
                .fillMaxWidth()
                .studentMinHeight(56.dp)
                .testTag("simulate-upload"),
            shape = RoundedCornerShape(18.dp),
            color = if (isDark) Color(0xFF10243C) else Color(0xFFFFFFFF),
            border = BorderStroke(1.5.dp, Color(0xFF60A5FA)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Space.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Icon Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E3A60) else Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = null,
                        tint = Color(0xFF168BFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Text
                Text(
                    text = tr("Simulate upload & crop", "محاكاة الرفع والقص"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF168BFF)
                )

                // Right Arrow
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null,
                    tint = Color(0xFF168BFF),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun SolverHistoryScreen(
    state: LearningState,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClear: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val topics = LearningRules.historyTopicIds
    val ar = state.language == Language.Arabic

    ScreenPage(Modifier.testTag("screen-history")) {
        if (onBack != null) {
            if (state.user?.type != AccountType.Teacher) Box(Modifier.testTag("history-back")) {
                RefTopBar(tr("Solution history", "سجل الحلول"), onBack = onBack)
            } else TextButton(onClick = onBack, modifier = Modifier.testTag("history-back")) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, label(TextKey.t_794ab43a2c))
                Text(tr("Back", "رجوع"))
            }
        }
        // Hero Card matching Image 6
        RefHeroCard(
            title = tr("Go back to your steps", "عد إلى خطواتك"),
            subtitle = tr("Review the problems you solved and track your progress in your learning journey.", "راجع المسائل التي حليتها وتابع تقدمك في رحلتك التعليمية."),
            badgeText = tr("History", "السجل"),
            badgeColor = RefColors.LightBlueHeroBgStart,
            badgeTextColor = RefColors.PrimaryBlue,
            artwork = { SolverHistoryHeroArtwork() }
        )

        // Search Field matching Image 6
        RefSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = tr("Search", "بحث")
        )

        // Category Filter Chips matching Image 6
        val filterTabs = listOf(
            RefTabItem("all", tr("All", "الكل"), Icons.Outlined.GridView),
            RefTabItem("limits", tr("Limits", "النهايات"), Icons.Outlined.AllInclusive),
            RefTabItem("derivatives", tr("Derivatives", "الاشتقاق"), Icons.Outlined.Functions),
            RefTabItem("integrals", tr("Integrals", "التكامل"), Icons.Outlined.Functions),
            RefTabItem("matrices", tr("Matrices", "المصفوفات"), Icons.Outlined.GridOn),
            RefTabItem("probability", tr("Probability", "الاحتمالات"), Icons.Outlined.Casino)
        )

        RefFilterChipRow(
            items = filterTabs,
            selectedId = topics.getOrElse(filter) { "all" },
            onSelect = { selectedId ->
                filter = topics.indexOf(selectedId).coerceAtLeast(0)
            },
            useMintGreen = true
        )

        // Clear History Action Button matching Image 6
        RefSecondaryButton(
            text = label(TextKey.t_05cfbeaf8f),
            onClick = onClear,
            icon = Icons.Outlined.Delete
        )

        val filtered = LearningRules.solutionHistory(state.product.history, topics.getOrElse(filter) { "all" }, query)

        // Empty State matching Image 6
        if (filtered.isEmpty()) {
            RefEmptyState(
                title = tr("No problems found", "لا توجد مسائل"),
                subtitle = tr("Solve an example or change the filter.", "حل مثالاً أو غير المرشح."),
                artwork = { SearchHeroArtwork() }
            )
        } else {
            filtered
                .groupBy { ((System.currentTimeMillis() - it.timestamp) / 86_400_000).coerceAtLeast(0) }
                .forEach { (day, items) ->
                    Text(
                        text = if (day == 0L) label(TextKey.t_e54621f53e)
                        else if (day == 1L) label(TextKey.t_61ec47c295) else label(TextKey.t_de69ae3b7a),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RefColors.TextPrimary,
                        modifier = Modifier.padding(top = Space.sm)
                    )
                    items.forEach { h ->
                        Card(
                            onClick = { onOpen(h.id) },
                            modifier = Modifier.fillMaxWidth().testTag("history-item-${h.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, RefColors.CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.lg),
                                verticalArrangement = Arrangement.spacedBy(Space.md)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = RefColors.LightBlueHeroBgStart
                                    ) {
                                        Text(
                                            text = state.topic(h.topicId)?.title?.text() ?: tr("Math", "رياضيات"),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = RefColors.PrimaryBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDelete(h.id) },
                                        modifier = Modifier.size(if (LocalStudentExperience.current) 48.dp else 32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = label(TextKey.t_3ffdefc981),
                                            tint = RefColors.TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = RefColors.FormulaBoxBg,
                                    border = BorderStroke(1.dp, RefColors.CardBorder.copy(alpha = 0.5f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Space.md),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MathText(h.expression)
                                    }
                                }

                                RefSecondaryButton(
                                    text = label(TextKey.t_0bbe6c9c93),
                                    onClick = { onOpen(h.id) },
                                    icon = Icons.AutoMirrored.Outlined.ArrowForward
                                )
                            }
                        }
                    }
                }
        }
    }
}
