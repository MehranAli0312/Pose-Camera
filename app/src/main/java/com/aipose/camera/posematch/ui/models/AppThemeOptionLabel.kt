package com.aipose.camera.posematch.ui.models

import androidx.annotation.StringRes
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AppThemeOption

@StringRes
fun themeOptionLabel(option: AppThemeOption): Int = when (option) {
    AppThemeOption.Dark -> R.string.theme_dark
    AppThemeOption.Light -> R.string.theme_light
    AppThemeOption.SleekCharcoal -> R.string.theme_sleek_charcoal
    AppThemeOption.CyberpunkViolet -> R.string.theme_cyberpunk_violet
}
