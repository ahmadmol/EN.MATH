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
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes

object RefPalette {
    val background: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val card: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val equation: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow

    val border: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val purple: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val violet: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val cyan: Color
        @Composable get() = MaterialTheme.colorScheme.secondary

    val green: Color
        @Composable get() = MaterialTheme.colorScheme.secondary

    val muted: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val white: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
}

@Composable
fun refText(ar: String, en: String) =
    if (LocalLayoutDirection.current == LayoutDirection.Rtl) ar else en

/** Dimensions taken from the 277px references, uniformly fitted to the available phone width. */
@Composable
fun ReferenceFrame(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            Modifier.widthIn(max = com.ahmadmol.enmath.core.design.Space.contentMax).fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun ReferencePage(tag: String, top: Dp = 14.dp, content: @Composable ColumnScope.() -> Unit) {
    ReferenceFrame {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
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
            .background(background ?: SolidColor(RefPalette.card), RoundedCornerShape(12.dp))
            .border(.7.dp, accent, RoundedCornerShape(12.dp))
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
        fontSize = (size + 2).coerceAtLeast(12).sp,
        lineHeight = (size + 10).coerceAtLeast(20).sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
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
            .heightIn(min = 48.dp)
            .background(
                if (selected) RefPalette.purple else RefPalette.equation,
                RoundedCornerShape(9.dp),
            )
            .border(
                .7.dp,
                if (selected) RefPalette.purple else RefPalette.border,
                RoundedCornerShape(9.dp),
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
fun RefBottomNav(route: String?, open: (String) -> Unit, dark: Boolean = true) {
    CleanBottomBar(
        listOf(
            CleanNavItem(
                Routes.Study,
                refText("المنهاج", "Curriculum"),
                Icons.AutoMirrored.Outlined.MenuBook,
            ),
            CleanNavItem(
                Routes.Solver,
                refText("الحل", "Solution"),
                Icons.Outlined.Calculate,
            ),
            CleanNavItem(Routes.Home, refText("الرئيسية", "Home"), Icons.Outlined.Home),
            CleanNavItem(
                "explore",
                refText("المختبر", "Math Lab"),
                Icons.AutoMirrored.Outlined.ShowChart,
            ),
            CleanNavItem(Routes.Profile, refText("حسابي", "Profile"), Icons.Outlined.PersonOutline),
        ),
        route,
        "nav-",
        dark = dark,
        open = open,
    )
}
