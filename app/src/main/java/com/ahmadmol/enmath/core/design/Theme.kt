package com.ahmadmol.enmath.core.design

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.model.*

object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val hero = 48.dp
    val contentMax = 1040.dp
    val formMax = 520.dp
    val symbol = 56.dp
    val touch = 48.dp
    val chart = 112.dp
}

val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF4944BD),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE6E4FF),
        onPrimaryContainer = Color(0xFF242065),
        secondary = Color(0xFF006B5A),
        secondaryContainer = Color(0xFFD8F5E9),
        onSecondaryContainer = Color(0xFF00382E),
        background = Color(0xFFF8F9FE),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFEEF0F8),
        onSurface = Color(0xFF202438),
        onSurfaceVariant = Color(0xFF5C6277),
        outline = Color(0xFF797F93),
        outlineVariant = Color(0xFFE0E3EF),
    )
val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF8F87FF),
        onPrimary = Color(0xFF151136),
        primaryContainer = Color(0xFF36317C),
        onPrimaryContainer = Color(0xFFE6E4FF),
        secondary = Color(0xFF83D6B8),
        secondaryContainer = Color(0xFF004D40),
        onSecondaryContainer = Color(0xFFD8F5E9),
        background = Color(0xFF0B0F19),
        surface = Color(0xFF0E1527),
        surfaceVariant = Color(0xFF182235),
        onSurface = Color(0xFFEBECF6),
        onSurfaceVariant = Color(0xFFB7BCD2),
        outline = Color(0xFF969CB3),
        outlineVariant = Color(0xFF353B50),
    )
val AppTypography =
    Typography(
        displaySmall =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 46.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 40.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 36.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 30.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 24.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
    )
val AppShapes =
    Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )
val LocalLanguage = staticCompositionLocalOf { Language.Arabic }
val LocalIsDarkTheme = staticCompositionLocalOf { false }
val LocalStudentExperience = staticCompositionLocalOf { false }

@Composable
fun isDarkTheme(): Boolean = LocalIsDarkTheme.current

@Composable
fun tr(en: String, ar: String) = if (LocalLanguage.current == Language.Arabic) ar else en

@Composable fun Copy.text() = tr(en, ar)

@Composable
fun ENMathTheme(mode: ThemeMode, language: Language, student: Boolean = false, content: @Composable () -> Unit) {
    val dark =
        when (mode) {
            ThemeMode.System -> isSystemInDarkTheme()
            ThemeMode.Dark -> true
            ThemeMode.Light -> false
        }
    val font =
        FontFamily(Font(if (language == Language.Arabic) R.font.ibm_plex_arabic else R.font.inter))
    val typography =
        AppTypography.copy(
            displaySmall = AppTypography.displaySmall.copy(fontFamily = font),
            headlineLarge = AppTypography.headlineLarge.copy(fontFamily = font),
            headlineMedium = AppTypography.headlineMedium.copy(fontFamily = font),
            titleLarge = AppTypography.titleLarge.copy(fontFamily = font),
            titleMedium = AppTypography.titleMedium.copy(fontFamily = font),
            bodyLarge = AppTypography.bodyLarge.copy(fontFamily = font),
            bodyMedium = AppTypography.bodyMedium.copy(fontFamily = font),
            bodySmall = AppTypography.bodySmall.copy(fontFamily = font),
            labelLarge = AppTypography.labelLarge.copy(fontFamily = font),
            labelMedium = AppTypography.labelMedium.copy(fontFamily = font),
            labelSmall = AppTypography.labelSmall.copy(fontFamily = font),
        )
    val activity = LocalActivity.current
    SideEffect {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(
        LocalLanguage provides language,
        LocalIsDarkTheme provides dark,
        LocalStudentExperience provides student,
        LocalLayoutDirection provides
            if (language == Language.Arabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        MaterialTheme(
            colorScheme = if (student) studentColorScheme(dark) else if (dark) DarkColorScheme else LightColorScheme,
            typography = typography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** Student reference palette; role-scoped so teacher styling keeps its existing theme. */
fun studentColorScheme(dark: Boolean): ColorScheme =
    if (dark) DarkColorScheme.copy(
        primary = Color(0xFF9BBCFF), onPrimary = Color(0xFF002C70),
        primaryContainer = Color(0xFF163763), onPrimaryContainer = Color(0xFFDCE9FF),
        secondary = Color(0xFF80DBB1), secondaryContainer = Color(0xFF123F32),
        onSecondaryContainer = Color(0xFFC5F8DF), background = Color(0xFF0B1424),
    ) else LightColorScheme.copy(
        primary = Color(0xFF2563EB), onPrimary = Color.White,
        primaryContainer = Color(0xFFEBF3FF), onPrimaryContainer = Color(0xFF174388),
        secondary = Color(0xFF0F8250), secondaryContainer = Color(0xFFD1F4E2),
        onSecondaryContainer = Color(0xFF065F46), background = Color(0xFFF4F7FB),
        onSurface = Color(0xFF0F172A), onSurfaceVariant = Color(0xFF475569),
        outlineVariant = Color(0xFFE2E8F0), surfaceVariant = Color(0xFFF1F5FA),
    )
