package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseRaisedBottom
import com.aipose.camera.posematch.ui.theme.PoseRaisedTop
import com.aipose.camera.posematch.ui.theme.PoseShadow

private val BorderWidth = 1.dp
private val RaisedShadowInsetX = 2.dp
private val RaisedShadowOffsetY = 6.dp
private val RaisedShadowShrinkY = 2.dp

private val SurfaceGlossInset = 8.dp
private val SurfaceGlossTop = 4.dp
private val SurfaceGlossHeight = 26.dp
private const val RAISED_SURFACE_BORDER_ALPHA = 0.09f
private const val ELEVATED_BORDER_ALPHA = 0.1f
private const val ELEVATED_GLOSS_ALPHA = 0.14f
private const val ELEVATED_SHADOW_ALPHA = 0.45f

fun Modifier.poseRaisedSurface(
    shape: Shape,
    borderAlpha: Float = RAISED_SURFACE_BORDER_ALPHA,
    glossAlpha: Float = 0f,
): Modifier = this
    .clip(shape)
    .background(Brush.verticalGradient(listOf(PoseRaisedTop, PoseRaisedBottom)))
    .drawBehind {
        if (glossAlpha <= 0f) return@drawBehind
        val inset = SurfaceGlossInset.toPx()
        val top = SurfaceGlossTop.toPx()
        val height = SurfaceGlossHeight.toPx()
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                startY = top,
                endY = top + height,
            ),
            topLeft = Offset(inset, top),
            size = Size(size.width - inset * 2f, height),
            cornerRadius = CornerRadius(height / 2f),
            alpha = glossAlpha,
        )
    }
    .border(BorderWidth, Color.White.copy(alpha = borderAlpha), shape)

fun Modifier.poseElevatedSurface(
    cornerRadius: Dp,
    borderAlpha: Float = ELEVATED_BORDER_ALPHA,
    glossAlpha: Float = ELEVATED_GLOSS_ALPHA,
    shadowAlpha: Float = ELEVATED_SHADOW_ALPHA,
): Modifier = this
    .drawBehind {
        val insetX = RaisedShadowInsetX.toPx()
        drawRoundRect(
            color = PoseShadow.copy(alpha = shadowAlpha),
            topLeft = Offset(insetX, RaisedShadowOffsetY.toPx()),
            size = Size(size.width - insetX * 2f, size.height - RaisedShadowShrinkY.toPx()),
            cornerRadius = CornerRadius(cornerRadius.toPx()),
        )
    }
    .poseRaisedSurface(RoundedCornerShape(cornerRadius), borderAlpha, glossAlpha)

@Composable
fun Modifier.poseCard(shape: Shape): Modifier {
    val palette = LocalAppPalette.current
    return this
        .clip(shape)
        .background(palette.card)
        .border(BorderWidth, palette.cardBorder, shape)
}

fun Modifier.poseRaisedCard(
    cornerRadius: Dp,
    brush: Brush,
    shadowColor: Color,
    shadowAlpha: Float,
    borderColor: Color,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .drawBehind {
            val insetX = RaisedShadowInsetX.toPx()
            drawRoundRect(
                color = shadowColor.copy(alpha = shadowAlpha),
                topLeft = Offset(insetX, RaisedShadowOffsetY.toPx()),
                size = Size(
                    size.width - insetX * 2f,
                    size.height - RaisedShadowShrinkY.toPx(),
                ),
                cornerRadius = CornerRadius(cornerRadius.toPx()),
            )
        }
        .clip(shape)
        .background(brush)
        .border(BorderWidth, borderColor, shape)
}

fun Modifier.poseGradientPill(
    palette: GlossyBadgePalette,
    cornerRadius: Dp,
    shadowInsetX: Dp = 2.dp,
    shadowOffsetY: Dp = 4.dp,
    shadowAlpha: Float = 0.5f,
    glossInsetX: Dp = 8.dp,
    glossTop: Dp = 4.dp,
    glossHeight: Dp = 26.dp,
    gradientMidStop: Float = 0.5f,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .drawBehind {
            val insetX = shadowInsetX.toPx()
            drawRoundRect(
                color = palette.shadow.copy(alpha = shadowAlpha),
                topLeft = Offset(insetX, shadowOffsetY.toPx()),
                size = Size(size.width - insetX * 2f, size.height),
                cornerRadius = CornerRadius(cornerRadius.toPx()),
            )
        }
        .clip(shape)
        .background(
            Brush.linearGradient(
                0f to palette.top,
                gradientMidStop to palette.mid,
                1f to palette.bottom,
            )
        )
        .drawBehind {
            val insetX = glossInsetX.toPx()
            val top = glossTop.toPx()
            val height = glossHeight.toPx()
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                    startY = top,
                    endY = top + height,
                ),
                topLeft = Offset(insetX, top),
                size = Size(size.width - insetX * 2f, height),
                cornerRadius = CornerRadius(height / 2f),
            )
        }
}

private const val GLOSS_ALPHA = 0.42f
