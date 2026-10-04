package com.aipose.camera.posematch.ui.screens.photoEdit.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoAdjustments

enum class AdjustTool(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    Exposure(R.string.adjust_exposure, R.drawable.ic_adjust_exposure),
    Brightness(R.string.adjust_brightness, R.drawable.ic_adjust_brightness),
    Contrast(R.string.adjust_contrast, R.drawable.ic_adjust_contrast),
    Saturation(R.string.adjust_saturation, R.drawable.ic_adjust_saturation),
    Warmth(R.string.adjust_warmth, R.drawable.ic_adjust_warmth)
}

fun PhotoAdjustments.valueOf(tool: AdjustTool): Float = when (tool) {
    AdjustTool.Exposure -> exposure
    AdjustTool.Brightness -> brightness
    AdjustTool.Contrast -> contrast
    AdjustTool.Saturation -> saturation
    AdjustTool.Warmth -> warmth
}

fun PhotoAdjustments.withValue(tool: AdjustTool, value: Float): PhotoAdjustments = when (tool) {
    AdjustTool.Exposure -> copy(exposure = value)
    AdjustTool.Brightness -> copy(brightness = value)
    AdjustTool.Contrast -> copy(contrast = value)
    AdjustTool.Saturation -> copy(saturation = value)
    AdjustTool.Warmth -> copy(warmth = value)
}
