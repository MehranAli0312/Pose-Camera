package com.aipose.camera.posematch.ui.screens.captureDetail.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

data class PhotoDetailInfo(
    @DrawableRes val iconRes: Int,
    val palette: GlossyBadgePalette,
    val glyphSize: Dp,
    val label: String,
    val value: String
)
