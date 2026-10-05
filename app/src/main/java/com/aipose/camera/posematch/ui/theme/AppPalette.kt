package com.aipose.camera.posematch.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppPalette(
    val accent: Color,
    val accentSecondary: Color,
    val backgroundTop: Color,
    val backgroundMid: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardBorder: Color,
    val glass: Color,
    val textMuted: Color,
    val textFaint: Color,
    val navSurface: Color,
    val navShadow: Color,
    val navActive: Color,
    val navInactive: Color
)

internal val StudioDarkPalette = AppPalette(
    accent = Indigo,
    accentSecondary = Violet,
    backgroundTop = PoseNightTop,
    backgroundMid = PoseNightMid,
    backgroundBottom = PoseNightBottom,
    card = PoseSurface,
    cardBorder = PoseSurfaceBorder,
    glass = StudioGlassDark,
    textMuted = PoseTextMuted,
    textFaint = PoseTextFaint,
    navSurface = PoseNavSurface,
    navShadow = PoseNavShadow,
    navActive = PoseNavActive,
    navInactive = PoseNavInactive
)

val LocalAppPalette = staticCompositionLocalOf { StudioDarkPalette }
