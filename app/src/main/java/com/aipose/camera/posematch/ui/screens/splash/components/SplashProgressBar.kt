package com.aipose.camera.posematch.ui.screens.splash.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseFuchsiaLight

private val TrackWidth = 120.dp
private val TrackHeight = 5.dp
private const val TRACK_ALPHA = 0.10f
private const val GRADIENT_MID_STOP = 0.5f

@Composable
internal fun SplashProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = TrackWidth, height = TrackHeight)) {
        val corner = CornerRadius(size.height / 2f)
        drawRoundRect(
            color = Color.White.copy(alpha = TRACK_ALPHA),
            cornerRadius = corner,
        )

        val filledWidth = size.width * progress.coerceIn(0f, 1f)
        if (filledWidth <= 0f) return@Canvas

        val isRtl = layoutDirection == LayoutDirection.Rtl
        val left = if (isRtl) size.width - filledWidth else 0f
        drawRoundRect(
            brush = Brush.horizontalGradient(
                0f to PoseCyanBright,
                GRADIENT_MID_STOP to Indigo,
                1f to PoseFuchsiaLight,
                startX = if (isRtl) size.width else 0f,
                endX = if (isRtl) left else filledWidth,
            ),
            topLeft = Offset(left, 0f),
            size = Size(filledWidth, size.height),
            cornerRadius = corner,
        )
    }
}
