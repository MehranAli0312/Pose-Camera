package com.aipose.camera.posematch.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush

private const val BACKGROUND_MID_STOP = 0.45f

@Composable
@ReadOnlyComposable
fun studioBackgroundBrush(): Brush {
    val palette = LocalAppPalette.current
    return Brush.verticalGradient(
        0f to palette.backgroundTop,
        BACKGROUND_MID_STOP to palette.backgroundMid,
        1f to palette.backgroundBottom,
    )
}
