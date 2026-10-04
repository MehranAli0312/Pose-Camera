package com.aipose.camera.posematch.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.domain.models.AppThemeOption

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

private val StudioDarkPalette = AppPalette(
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

private val StudioLightPalette = AppPalette(
    accent = Indigo,
    accentSecondary = Violet,
    backgroundTop = StudioBackgroundTopLight,
    backgroundMid = StudioBackgroundMidLight,
    backgroundBottom = StudioBackgroundBottomLight,
    card = StudioCardLight,
    cardBorder = StudioGlassLight,
    glass = StudioGlassLight,
    textMuted = StudioTextMutedLight,
    textFaint = StudioTextFaintLight,
    navSurface = StudioNavSurfaceLight,
    navShadow = StudioNavShadowLight,
    navActive = StudioNavActiveLight,
    navInactive = StudioNavInactiveLight
)

private val SleekCharcoalPalette = AppPalette(
    accent = Copper,
    accentSecondary = CopperLight,
    backgroundTop = CharcoalBackgroundTop,
    backgroundMid = CharcoalBackgroundMid,
    backgroundBottom = CharcoalBackgroundBottom,
    card = CharcoalCard,
    cardBorder = CharcoalGlass,
    glass = CharcoalGlass,
    textMuted = CharcoalTextMuted,
    textFaint = CharcoalTextFaint,
    navSurface = CharcoalNavSurface,
    navShadow = CharcoalNavShadow,
    navActive = CharcoalNavActive,
    navInactive = CharcoalNavInactive
)

private val CyberpunkVioletPalette = AppPalette(
    accent = Fuchsia,
    accentSecondary = VioletDeep,
    backgroundTop = CyberpunkBackgroundTop,
    backgroundMid = CyberpunkBackgroundMid,
    backgroundBottom = CyberpunkBackgroundBottom,
    card = CyberpunkCard,
    cardBorder = CyberpunkGlass,
    glass = CyberpunkGlass,
    textMuted = CyberpunkTextMuted,
    textFaint = CyberpunkTextFaint,
    navSurface = CyberpunkNavSurface,
    navShadow = CyberpunkNavShadow,
    navActive = CyberpunkNavActive,
    navInactive = CyberpunkNavInactive
)

fun paletteFor(option: AppThemeOption): AppPalette = when (option) {
    AppThemeOption.Dark -> StudioDarkPalette
    AppThemeOption.Light -> StudioLightPalette
    AppThemeOption.SleekCharcoal -> SleekCharcoalPalette
    AppThemeOption.CyberpunkViolet -> CyberpunkVioletPalette
}

val LocalAppPalette = staticCompositionLocalOf { StudioDarkPalette }
