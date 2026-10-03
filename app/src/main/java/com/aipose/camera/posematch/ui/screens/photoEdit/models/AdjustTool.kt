package com.aipose.camera.posematch.ui.screens.photoEdit.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoAdjustments

enum class AdjustTool(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val isGeometry: Boolean = false
) {
    Exposure(R.string.adjust_exposure, Icons.Default.Exposure),
    Brightness(R.string.adjust_brightness, Icons.Default.LightMode),
    Contrast(R.string.adjust_contrast, Icons.Default.Contrast),
    Saturation(R.string.adjust_saturation, Icons.Default.WaterDrop),
    Warmth(R.string.adjust_warmth, Icons.Default.Thermostat),
    Tint(R.string.adjust_tint, Icons.Default.Colorize),
    Hue(R.string.adjust_hue, Icons.Default.Palette),
    Fade(R.string.adjust_fade, Icons.Default.Gradient),
    Rotate(R.string.adjust_rotate, Icons.Default.RotateRight, isGeometry = true),
    Crop(R.string.adjust_crop, Icons.Default.Crop, isGeometry = true)
}

fun PhotoAdjustments.valueOf(tool: AdjustTool): Float = when (tool) {
    AdjustTool.Exposure -> exposure
    AdjustTool.Brightness -> brightness
    AdjustTool.Contrast -> contrast
    AdjustTool.Saturation -> saturation
    AdjustTool.Warmth -> warmth
    AdjustTool.Tint -> tint
    AdjustTool.Hue -> hue
    AdjustTool.Fade -> fade
    AdjustTool.Rotate, AdjustTool.Crop -> 0f
}

fun PhotoAdjustments.withValue(tool: AdjustTool, value: Float): PhotoAdjustments = when (tool) {
    AdjustTool.Exposure -> copy(exposure = value)
    AdjustTool.Brightness -> copy(brightness = value)
    AdjustTool.Contrast -> copy(contrast = value)
    AdjustTool.Saturation -> copy(saturation = value)
    AdjustTool.Warmth -> copy(warmth = value)
    AdjustTool.Tint -> copy(tint = value)
    AdjustTool.Hue -> copy(hue = value)
    AdjustTool.Fade -> copy(fade = value)
    AdjustTool.Rotate, AdjustTool.Crop -> this
}
