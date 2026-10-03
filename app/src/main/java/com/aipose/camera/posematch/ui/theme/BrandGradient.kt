package com.aipose.camera.posematch.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape

object BrandGradient {
    val colors: List<androidx.compose.ui.graphics.Color>
        get() = listOf(AppMainColor, AppSecondaryColor)

    val brush: Brush
        get() = Brush.horizontalGradient(colors = colors)
}

fun Modifier.brandGradientBackground(shape: Shape): Modifier =
    background(brush = BrandGradient.brush, shape = shape)
