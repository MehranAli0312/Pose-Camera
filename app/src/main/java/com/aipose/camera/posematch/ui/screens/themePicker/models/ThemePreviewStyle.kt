package com.aipose.camera.posematch.ui.screens.themePicker.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.ThemePreviewCharcoalBackground
import com.aipose.camera.posematch.ui.theme.ThemePreviewCopperBar
import com.aipose.camera.posematch.ui.theme.ThemePreviewCopperSwatch
import com.aipose.camera.posematch.ui.theme.ThemePreviewCyberpunkBackground
import com.aipose.camera.posematch.ui.theme.ThemePreviewDarkBackground
import com.aipose.camera.posematch.ui.theme.ThemePreviewLightBackground
import com.aipose.camera.posematch.ui.theme.ThemePreviewMagentaBar
import com.aipose.camera.posematch.ui.theme.ThemePreviewPlumSwatch
import com.aipose.camera.posematch.ui.theme.Violet

data class ThemePreviewStyle(
    @StringRes val descriptionRes: Int,
    val background: Color,
    val bar: Color,
    val swatch: Color,
    val lineAlpha: Float,
    val tileAlpha: Float
)

private const val DEFAULT_LINE_ALPHA = 0.22f
private const val DEFAULT_TILE_ALPHA = 0.1f
private const val LIGHT_LINE_ALPHA = 0.3f
private const val LIGHT_TILE_ALPHA = 0.16f

fun themePreviewStyle(option: AppThemeOption): ThemePreviewStyle = when (option) {
    AppThemeOption.Dark -> ThemePreviewStyle(
        descriptionRes = R.string.theme_desc_dark,
        background = ThemePreviewDarkBackground,
        bar = Indigo,
        swatch = Violet,
        lineAlpha = DEFAULT_LINE_ALPHA,
        tileAlpha = DEFAULT_TILE_ALPHA,
    )

    AppThemeOption.Light -> ThemePreviewStyle(
        descriptionRes = R.string.theme_desc_light,
        background = ThemePreviewLightBackground,
        bar = Indigo,
        swatch = Violet,
        lineAlpha = LIGHT_LINE_ALPHA,
        tileAlpha = LIGHT_TILE_ALPHA,
    )

    AppThemeOption.SleekCharcoal -> ThemePreviewStyle(
        descriptionRes = R.string.theme_desc_sleek_charcoal,
        background = ThemePreviewCharcoalBackground,
        bar = ThemePreviewCopperBar,
        swatch = ThemePreviewCopperSwatch,
        lineAlpha = DEFAULT_LINE_ALPHA,
        tileAlpha = DEFAULT_TILE_ALPHA,
    )

    AppThemeOption.CyberpunkViolet -> ThemePreviewStyle(
        descriptionRes = R.string.theme_desc_cyberpunk_violet,
        background = ThemePreviewCyberpunkBackground,
        bar = ThemePreviewMagentaBar,
        swatch = ThemePreviewPlumSwatch,
        lineAlpha = DEFAULT_LINE_ALPHA,
        tileAlpha = DEFAULT_TILE_ALPHA,
    )
}
