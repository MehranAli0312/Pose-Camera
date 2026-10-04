package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

private const val CORNER_RATIO = 0.3214f
private const val SHADOW_OFFSET_X_RATIO = 0.0179f
private const val SHADOW_OFFSET_Y_RATIO = 0.0893f
private const val SHADOW_ALPHA = 0.6f
private const val GLOSS_INSET_X_RATIO = 0.0893f
private const val GLOSS_INSET_Y_RATIO = 0.0714f
private const val GLOSS_HEIGHT_RATIO = 0.3929f
private const val GLOSS_ALPHA = 0.42f
private const val GRADIENT_SKEW = 0.4f
private const val GRADIENT_MID_STOP = 0.55f
private const val CIRCLE_SHADOW_ALPHA = 0.55f
private const val CIRCLE_GLOSS_ALPHA = 0.45f

@Composable
fun GlossyIconBadge(
    @DrawableRes iconRes: Int,
    palette: GlossyBadgePalette,
    size: Dp,
    modifier: Modifier = Modifier,
    glyphSize: Dp = size,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind { drawGlossyBadge(palette) },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = if (glyphSize == size) Modifier.fillMaxSize() else Modifier.size(glyphSize),
        )
    }
}

@Composable
fun GlossyIconCircle(
    @DrawableRes iconRes: Int,
    palette: GlossyBadgePalette,
    size: Dp,
    modifier: Modifier = Modifier,
    glyphSize: Dp = size,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind { drawGlossyCircle(palette) },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = if (glyphSize == size) Modifier.fillMaxSize() else Modifier.size(glyphSize),
        )
    }
}

private fun DrawScope.drawGlossyBadge(palette: GlossyBadgePalette) {
    val width = size.width
    val height = size.height
    val corner = CornerRadius(width * CORNER_RATIO)

    drawRoundRect(
        color = palette.shadow.copy(alpha = SHADOW_ALPHA),
        topLeft = Offset(width * SHADOW_OFFSET_X_RATIO, height * SHADOW_OFFSET_Y_RATIO),
        size = Size(width, height),
        cornerRadius = corner,
    )
    drawRoundRect(
        brush = badgeBrush(palette, width, height),
        size = Size(width, height),
        cornerRadius = corner,
    )
    val glossInsetX = width * GLOSS_INSET_X_RATIO
    val glossInsetY = height * GLOSS_INSET_Y_RATIO
    val glossHeight = height * GLOSS_HEIGHT_RATIO
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
            startY = glossInsetY,
            endY = glossInsetY + glossHeight,
        ),
        topLeft = Offset(glossInsetX, glossInsetY),
        size = Size(width - glossInsetX * 2f, glossHeight),
        cornerRadius = CornerRadius(glossHeight / 2f),
    )
}

private fun DrawScope.drawGlossyCircle(palette: GlossyBadgePalette) {
    val width = size.width
    val radius = width / 2f
    val center = Offset(radius, radius)

    drawCircle(
        color = palette.shadow.copy(alpha = CIRCLE_SHADOW_ALPHA),
        radius = radius,
        center = center.copy(y = radius + width * SHADOW_OFFSET_Y_RATIO / 2f),
    )
    drawCircle(
        brush = badgeBrush(palette, width, width),
        radius = radius,
        center = center,
    )
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = CIRCLE_GLOSS_ALPHA), Color.Transparent),
            startY = 0f,
            endY = width,
        ),
        radius = radius,
        center = center,
        alpha = CIRCLE_SHADOW_ALPHA,
    )
}

private fun badgeBrush(palette: GlossyBadgePalette, width: Float, height: Float): Brush =
    Brush.linearGradient(
        0f to palette.top,
        GRADIENT_MID_STOP to palette.mid,
        1f to palette.bottom,
        start = Offset.Zero,
        end = Offset(width * GRADIENT_SKEW, height),
    )
