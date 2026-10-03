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
    val backgroundBottom: Color,
    val card: Color,
    val glass: Color
)

private val StudioDarkPalette = AppPalette(
    accent = Indigo,
    accentSecondary = Violet,
    backgroundTop = StudioBackgroundTopDark,
    backgroundBottom = StudioBackgroundBottomDark,
    card = StudioCardDark,
    glass = StudioGlassDark
)

private val StudioLightPalette = AppPalette(
    accent = Indigo,
    accentSecondary = Violet,
    backgroundTop = StudioBackgroundTopLight,
    backgroundBottom = StudioBackgroundBottomLight,
    card = StudioCardLight,
    glass = StudioGlassLight
)

private val SleekCharcoalPalette = AppPalette(
    accent = Copper,
    accentSecondary = CopperLight,
    backgroundTop = CharcoalBackgroundTop,
    backgroundBottom = CharcoalBackgroundBottom,
    card = CharcoalCard,
    glass = CharcoalGlass
)

private val CyberpunkVioletPalette = AppPalette(
    accent = Fuchsia,
    accentSecondary = VioletDeep,
    backgroundTop = CyberpunkBackgroundTop,
    backgroundBottom = CyberpunkBackgroundBottom,
    card = CyberpunkCard,
    glass = CyberpunkGlass
)

fun paletteFor(option: AppThemeOption): AppPalette = when (option) {
    AppThemeOption.Dark -> StudioDarkPalette
    AppThemeOption.Light -> StudioLightPalette
    AppThemeOption.SleekCharcoal -> SleekCharcoalPalette
    AppThemeOption.CyberpunkViolet -> CyberpunkVioletPalette
}

val LocalAppPalette = staticCompositionLocalOf { StudioDarkPalette }
