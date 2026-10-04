package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val CAMERA_GLASS_SCRIM_ALPHA = 0.45f
internal const val CAMERA_CARD_SCRIM_ALPHA = 0.5f
internal const val CAMERA_GLASS_BORDER_ALPHA = 0.2f
internal const val CAMERA_CARD_BORDER_ALPHA = 0.16f

private val GlossInset = 3.dp
private const val GLOSS_ALPHA = 0.3f
private const val GLOSS_HEIGHT_RATIO = 0.42f

internal fun Modifier.cameraGlass(
    shape: Shape,
    scrimAlpha: Float = CAMERA_GLASS_SCRIM_ALPHA,
    borderColor: Color = Color.White.copy(alpha = CAMERA_GLASS_BORDER_ALPHA),
    glossLayerAlpha: Float = 0.5f,
    glossInset: Dp = GlossInset,
): Modifier = this
    .clip(shape)
    .background(Color.Black.copy(alpha = scrimAlpha))
    .drawBehind {
        val inset = glossInset.toPx()
        val height = size.height * GLOSS_HEIGHT_RATIO
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                startY = inset,
                endY = inset + height,
            ),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2f, height),
            cornerRadius = CornerRadius(height / 2f),
            alpha = glossLayerAlpha,
        )
    }
    .border(1.dp, borderColor, shape)
