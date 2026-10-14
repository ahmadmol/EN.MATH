// EN.MATH resource pack v2 tokens. Unified presentation baseline for all existing destinations.
package com.ahmadmol.enmath.core.design.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

val ResourceLightColorScheme =
    lightColorScheme(
        primary = Color(0xFF175CD3),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCEBFF),
        onPrimaryContainer = Color(0xFF123368),
        secondary = Color(0xFF047A64),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD7F6EC),
        onSecondaryContainer = Color(0xFF0B4D3F),
        tertiary = Color(0xFF6941C6),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFECE5FF),
        onTertiaryContainer = Color(0xFF3C2474),
        error = Color(0xFFC02D3A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFE8E9),
        onErrorContainer = Color(0xFF7D1424),
        background = Color(0xFFF7F9FC),
        onBackground = Color(0xFF0F1F38),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F1F38),
        surfaceVariant = Color(0xFFEDF2F8),
        onSurfaceVariant = Color(0xFF526178),
        outline = Color(0xFF6D7D94),
        outlineVariant = Color(0xFFD9E3F0),
        inverseSurface = Color(0xFF26354B),
        inverseOnSurface = Color(0xFFF2F6FF),
        inversePrimary = Color(0xFF8CB9FF),
        scrim = Color(0xFF000000),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F8FD),
        surfaceContainer = Color(0xFFEEF3FA),
        surfaceContainerHigh = Color(0xFFE7EEF8),
        surfaceContainerHighest = Color(0xFFDFE8F5),
    )

val ResourceDarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF8CB9FF),
        onPrimary = Color(0xFF092140),
        primaryContainer = Color(0xFF173E76),
        onPrimaryContainer = Color(0xFFE0ECFF),
        secondary = Color(0xFF61D7BB),
        onSecondary = Color(0xFF06291F),
        secondaryContainer = Color(0xFF164B40),
        onSecondaryContainer = Color(0xFFD8F8EF),
        tertiary = Color(0xFFC4B5FD),
        onTertiary = Color(0xFF241549),
        tertiaryContainer = Color(0xFF48326F),
        onTertiaryContainer = Color(0xFFF0E9FF),
        error = Color(0xFFFFB4B9),
        onError = Color(0xFF5A1020),
        errorContainer = Color(0xFF722834),
        onErrorContainer = Color(0xFFFFE7EA),
        background = Color(0xFF0B1322),
        onBackground = Color(0xFFF2F6FF),
        surface = Color(0xFF121E30),
        onSurface = Color(0xFFF2F6FF),
        surfaceVariant = Color(0xFF1B2B42),
        onSurfaceVariant = Color(0xFFABBAD0),
        outline = Color(0xFF91A2BB),
        outlineVariant = Color(0xFF35465F),
        inverseSurface = Color(0xFFE6ECF5),
        inverseOnSurface = Color(0xFF132238),
        inversePrimary = Color(0xFF175CD3),
        scrim = Color(0xFF000000),
        surfaceContainerLowest = Color(0xFF0B1322),
        surfaceContainerLow = Color(0xFF111D2F),
        surfaceContainer = Color(0xFF16243A),
        surfaceContainerHigh = Color(0xFF1C2C45),
        surfaceContainerHighest = Color(0xFF263953),
    )

/** One shared resource baseline for student, teacher and shared screens. */
val LocalResourceBaseline = staticCompositionLocalOf { false }

/** Presentation colors reconciled to L06/D06, with readable gradient foregrounds. */
object ApprovedHomeVisuals {
    val compactGoal = 48.dp
    val lightHeader = Color(0xFFE1EEFF)
    val darkHeader = Color(0xFF0B2847)
    val lightContinueStart = Color(0xFF245B98)
    val lightContinueEnd = Color(0xFF145763)
    val darkContinueStart = Color(0xFF005BD4)
    val darkContinueEnd = Color(0xFF065C86)
    val continueText = Color.White
    val continueIcon = Color(0xFF15324A)
    val darkSurface = Color(0xFF0D2235)
    val darkOutline = Color(0xFF205374)
    val darkPrimary = Color(0xFF56B4FF)
    val darkSecondary = Color(0xFF20DBC2)
}

object ResourceDimensions {
    val phoneInset = 16.dp
    val tabletInset = 24.dp
    val gutter = 12.dp
    val sectionGap = 24.dp
    val cardInset = 16.dp
    val contentMax = 720.dp
    val touch = 48.dp
    val prominentButton = 52.dp
    val icon = 24.dp
    val navHeight = 80.dp
    val goalRing = 96.dp
    val radius = 16.dp
    val heroRadius = 20.dp
    val buttonRadius = 12.dp
}

@Composable
fun ResourcePackPresentation(enabled: Boolean, content: @Composable () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val font = MaterialTheme.typography.bodyMedium.fontFamily
    val type =
        MaterialTheme.typography.copy(
            displayLarge =
                TextStyle(
                    fontFamily = font,
                    fontSize = 42.sp,
                    lineHeight = 54.sp,
                    fontWeight = FontWeight(700),
                ),
            displayMedium =
                TextStyle(
                    fontFamily = font,
                    fontSize = 36.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight(700),
                ),
            headlineLarge =
                TextStyle(
                    fontFamily = font,
                    fontSize = 28.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight(700),
                ),
            headlineMedium =
                TextStyle(
                    fontFamily = font,
                    fontSize = 24.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight(700),
                ),
            headlineSmall =
                TextStyle(
                    fontFamily = font,
                    fontSize = 22.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight(700),
                ),
            titleLarge =
                TextStyle(
                    fontFamily = font,
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight(600),
                ),
            titleMedium =
                TextStyle(
                    fontFamily = font,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight(600),
                ),
            titleSmall =
                TextStyle(
                    fontFamily = font,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(600),
                ),
            bodyLarge =
                TextStyle(
                    fontFamily = font,
                    fontSize = 16.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight(400),
                ),
            bodyMedium =
                TextStyle(
                    fontFamily = font,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(400),
                ),
            bodySmall =
                TextStyle(
                    fontFamily = font,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(400),
                ),
            labelLarge =
                TextStyle(
                    fontFamily = font,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(600),
                ),
            labelMedium =
                TextStyle(
                    fontFamily = font,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight(600),
                ),
            labelSmall =
                TextStyle(
                    fontFamily = font,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight(600),
                ),
        )
    CompositionLocalProvider(LocalResourceBaseline provides enabled) {
        MaterialTheme(
            colorScheme =
                if (!enabled) MaterialTheme.colorScheme
                else if (dark) ResourceDarkColorScheme.copy(
                    primary = ApprovedHomeVisuals.darkPrimary,
                    secondary = ApprovedHomeVisuals.darkSecondary,
                    surface = ApprovedHomeVisuals.darkSurface,
                    outlineVariant = ApprovedHomeVisuals.darkOutline,
                ) else ResourceLightColorScheme,
            typography = if (enabled) type else MaterialTheme.typography,
            shapes =
                if (enabled)
                    Shapes(
                        extraSmall = RoundedCornerShape(8.dp),
                        small = RoundedCornerShape(12.dp),
                        medium = RoundedCornerShape(16.dp),
                        large = RoundedCornerShape(20.dp),
                        extraLarge = RoundedCornerShape(28.dp),
                    )
                else MaterialTheme.shapes,
            content = content,
        )
    }
}
