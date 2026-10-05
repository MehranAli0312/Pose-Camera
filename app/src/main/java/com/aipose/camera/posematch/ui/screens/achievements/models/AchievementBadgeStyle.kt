package com.aipose.camera.posematch.ui.screens.achievements.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

@Immutable
data class AchievementBadgeStyle(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val palette: GlossyBadgePalette,
    val glyphSize: Dp
)
