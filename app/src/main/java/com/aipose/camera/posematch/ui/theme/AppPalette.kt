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
)

val LocalAppPalette = staticCompositionLocalOf { StudioDarkPalette }
