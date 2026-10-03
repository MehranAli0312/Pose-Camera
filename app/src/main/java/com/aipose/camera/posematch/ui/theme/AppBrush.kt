package com.aipose.camera.posematch.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush

@Composable
@ReadOnlyComposable
fun studioBackgroundBrush(): Brush {
    val palette = LocalAppPalette.current
    return Brush.verticalGradient(listOf(palette.backgroundTop, palette.backgroundBottom))
}

@Composable
@ReadOnlyComposable
fun accentBrush(): Brush {
    val palette = LocalAppPalette.current
    return Brush.horizontalGradient(listOf(palette.accent, palette.accentSecondary))
}
