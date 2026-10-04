package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.PoseFilterGlyph
import com.aipose.camera.posematch.ui.theme.PoseShadow

private val ShadowOffsetY = 4.dp
private const val BUTTON_BORDER_ALPHA = 0.14f
private const val BUTTON_SHADOW_ALPHA = 0.5f

@Composable
fun PoseRaisedIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    glyphSize: DpSize,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    color = PoseShadow.copy(alpha = BUTTON_SHADOW_ALPHA),
                    radius = this.size.minDimension / 2f,
                    center = center.copy(y = center.y + ShadowOffsetY.toPx()),
                )
            }
            .poseRaisedSurface(CircleShape, borderAlpha = BUTTON_BORDER_ALPHA)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(PoseFilterGlyph),
            modifier = Modifier.size(glyphSize),
        )
    }
}
