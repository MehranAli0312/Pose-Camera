package com.aipose.camera.posematch.ui.theme

import androidx.compose.ui.graphics.Brush

object BrandGradient {
    val colors: List<androidx.compose.ui.graphics.Color>
        get() = listOf(AppMainColor, AppSecondaryColor)

    val brush: Brush
        get() = Brush.horizontalGradient(colors = colors)
}
