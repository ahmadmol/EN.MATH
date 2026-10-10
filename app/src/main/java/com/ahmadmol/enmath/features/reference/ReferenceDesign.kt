package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes

object RefPalette {
    val background = Color(0xFF0B0F19)
    val card = Color(0xFF0E1527)
    val equation = Color(0xFF020618)
    val border = Color(0xFF213047)
    val purple = Color(0xFF4F39F6)
    val violet = Color(0xFF615FFF)
    val cyan = Color(0xFF06B6D4)
    val green = Color(0xFF00D19A)
    val muted = Color(0xFF8092AF)
    val white = Color(0xFFE5E7EB)
}

@Composable
fun refText(ar: String, en: String) =
    if (LocalLayoutDirection.current == LayoutDirection.Rtl) ar else en

/** Dimensions taken from the 277px references, uniformly fitted to the available phone width. */
@Composable
fun ReferenceFrame(content: @Composable () -> Unit) {
    BoxWithConstraints(
        Modifier.fillMaxSize().background(RefPalette.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        val density = LocalDensity.current
        val scale = (maxWidth.value.coerceAtMost(416f) / 277f).coerceAtLeast(.85f)
        val font = FontFamily(Font(R.font.ibm_plex_arabic))
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * scale, density.fontScale)
        ) {
            MaterialTheme(
                colorScheme =
                    darkColorScheme(
                        background = RefPalette.background,
                        surface = RefPalette.card,
                        onSurface = RefPalette.white,
                        primary = RefPalette.violet,
                        outline = RefPalette.border,
                    ),
                typography =
                    Typography(
                        bodyMedium =
                            androidx.compose.ui.text.TextStyle(
                                fontFamily = font,
                                fontSize = 9.sp,
                                lineHeight = 14.sp,
                            ),
                        bodySmall =
                            androidx.compose.ui.text.TextStyle(
                                fontFamily = font,
                                fontSize = 8.sp,
                                lineHeight = 12.sp,
                            ),
                        titleMedium =
                            androidx.compose.ui.text.TextStyle(
                                fontFamily = font,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    ),
            ) {
                Box(Modifier.widthIn(max = 277.dp).fillMaxWidth()) { content() }
            }
        }
    }
}

@Composable
fun ReferencePage(tag: String, top: Dp = 14.dp, content: @Composable ColumnScope.() -> Unit) {
    ReferenceFrame {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 11.dp)
                .padding(top = top, bottom = 14.dp)
                .testTag(tag),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun RefCard(
    modifier: Modifier = Modifier,
    accent: Color = RefPalette.border,
    padding: Dp = 11.dp,
    background: Brush? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(background ?: SolidColor(RefPalette.card), RoundedCornerShape(10.dp))
            .border(.7.dp, accent, RoundedCornerShape(10.dp))
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        content = content,
    )
}

@Composable
fun RefLabel(
    text: String,
    color: Color = RefPalette.muted,
    size: Int = 8,
    bold: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier,
        color = color,
        fontSize = size.sp,
        lineHeight = (size + 4).sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Start,
    )
}

@Composable
fun RefHeading(title: String, subtitle: String = "", icon: ImageVector? = null) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        if (icon != null)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(icon, null, Modifier.size(11.dp), tint = RefPalette.violet)
                RefLabel(subtitle, RefPalette.violet, 8)
            }
        RefLabel(title, RefPalette.white, 12, true)
        if (icon == null && subtitle.isNotEmpty()) RefLabel(subtitle)
    }
}

@Composable
fun RefButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
) {
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 24.dp)
            .background(
                if (selected) RefPalette.purple else RefPalette.equation,
                RoundedCornerShape(6.dp),
            )
            .border(
                .7.dp,
                if (selected) RefPalette.purple else RefPalette.border,
                RoundedCornerShape(6.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        RefLabel(text, if (selected) Color.White else RefPalette.muted, 8, true)
    }
}

@Composable
fun RefIcon(icon: ImageVector, color: Color = RefPalette.violet, size: Int = 14) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Icon(icon, null, Modifier.size(size.dp), tint = color)
    }
}

@Composable
fun RefBottomNav(route: String?, open: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().background(Color(0xFF030717))) {
        val d = LocalDensity.current
        val scale = (maxWidth.value.coerceAtMost(416f) / 277f).coerceAtLeast(.85f)
        CompositionLocalProvider(LocalDensity provides Density(d.density * scale, d.fontScale)) {
            Row(
                Modifier.fillMaxWidth()
                    .border(.6.dp, RefPalette.border)
                    .navigationBarsPadding()
                    .padding(top = 7.dp, bottom = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val tabs =
                    listOf(
                        Triple(Routes.Home, "الرئيسية", Icons.Outlined.Home),
                        Triple(Routes.Study, "المنهج", Icons.AutoMirrored.Outlined.MenuBook),
                        Triple(Routes.Solver, "الحل المفصل", Icons.Outlined.Calculate),
                        Triple("explore", "المختبر", Icons.AutoMirrored.Outlined.ShowChart),
                        Triple(Routes.Profile, "حسابي", Icons.Outlined.PersonOutline),
                    )
                val english = listOf("Home", "Study", "Solution", "Lab", "Profile")
                tabs.forEachIndexed { i, t ->
                    val selected = route == t.first
                    val color = if (selected) RefPalette.violet else RefPalette.muted
                    Column(
                        Modifier.weight(1f)
                            .clickable { open(t.first) }
                            .testTag("nav-${t.first}")
                            .padding(horizontal = 1.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        RefIcon(t.third, color, 12)
                        Text(
                            refText(t.second, english[i]),
                            color = color,
                            fontSize = 7.sp,
                            lineHeight = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
