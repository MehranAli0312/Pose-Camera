package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.models.Icon3DGlyphFit
import com.aipose.camera.posematch.ui.models.Icon3DPalette

private const val ICON_UNITS = 52f
private const val ICON_HEIGHT_UNITS = 58f
private const val FACE_HEIGHT_UNITS = 48f
private const val DEPTH_OFFSET_UNITS = 5f
private const val CORNER_UNITS = 16f
private const val RIM_WIDTH_UNITS = 1.5f
private const val GLYPH_SCALE = 0.54f
private const val GLYPH_SHADE_OFFSET_UNITS = 2f

@Composable
fun Icon3D(
    @DrawableRes iconRes: Int,
    palette: Icon3DPalette,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    glyphFit: Icon3DGlyphFit = Icon3DGlyphFit.Centered,
) {
    val unit = size / ICON_UNITS
    val painter = painterResource(iconRes)
    val glyphModifier = when (glyphFit) {
        Icon3DGlyphFit.Centered -> Modifier.size(size * GLYPH_SCALE)
        Icon3DGlyphFit.FullFace -> Modifier.fillMaxSize()
    }
    Box(modifier = modifier.size(width = size, height = unit * ICON_HEIGHT_UNITS)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIconBody(palette = palette, unitPx = this.size.width / ICON_UNITS)
        }
        Box(
            modifier = Modifier.size(width = size, height = unit * FACE_HEIGHT_UNITS),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(palette.glyphShade.copy(alpha = 0.45f)),
                modifier = Modifier
                    .offset(y = unit * GLYPH_SHADE_OFFSET_UNITS)
                    .then(glyphModifier),
            )
            Image(
                painter = painter,
                contentDescription = null,
                modifier = glyphModifier,
            )
        }
    }
}

private fun DrawScope.drawIconBody(palette: Icon3DPalette, unitPx: Float) {
    val width = ICON_UNITS * unitPx
    val faceHeight = FACE_HEIGHT_UNITS * unitPx
    val corner = CornerRadius(CORNER_UNITS * unitPx)

    drawOval(
        color = palette.end.copy(alpha = 0.22f),
        topLeft = Offset(7f * unitPx, 50.5f * unitPx),
        size = Size(38f * unitPx, 7f * unitPx),
    )
    drawRoundRect(
        color = palette.depth,
        topLeft = Offset(0f, DEPTH_OFFSET_UNITS * unitPx),
        size = Size(width, faceHeight),
        cornerRadius = corner,
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(palette.start, palette.end),
            start = Offset.Zero,
            end = Offset(width, faceHeight),
        ),
        size = Size(width, faceHeight),
        cornerRadius = corner,
    )
    val rimInset = RIM_WIDTH_UNITS * unitPx / 2f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(rimInset, rimInset),
        size = Size(width - rimInset * 2f, faceHeight - rimInset * 2f),
        cornerRadius = CornerRadius((CORNER_UNITS - RIM_WIDTH_UNITS / 2f) * unitPx),
        style = Stroke(width = RIM_WIDTH_UNITS * unitPx),
    )
    val glossTop = 2.5f * unitPx
    val glossHeight = 20f * unitPx
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
            startY = glossTop,
            endY = glossTop + glossHeight,
        ),
        topLeft = Offset(4f * unitPx, glossTop),
        size = Size(44f * unitPx, glossHeight),
        cornerRadius = CornerRadius(10f * unitPx),
    )
}
