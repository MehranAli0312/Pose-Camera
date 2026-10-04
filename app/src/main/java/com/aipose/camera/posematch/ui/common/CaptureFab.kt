package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

private val DefaultButtonSize = 60.dp
private val DefaultGlowSize = 72.dp
private val DefaultGlyphSize = 25.dp
private const val DEFAULT_GLOW_ALPHA = 0.2f

@Composable
fun CaptureFab(
    onClick: () -> Unit,
    glowColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    palette: GlossyBadgePalette = GlossyBadgePalette.Capture,
    buttonSize: Dp = DefaultButtonSize,
    glowSize: Dp = DefaultGlowSize,
    glyphSize: Dp = DefaultGlyphSize,
    @DrawableRes iconRes: Int = R.drawable.ic_pose_camera_fab,
    glowAlpha: Float = DEFAULT_GLOW_ALPHA,
) {
    Box(
        modifier = modifier
            .size(glowSize)
            .drawBehind {
                drawCircle(
                    color = glowColor.copy(alpha = glowAlpha),
                    radius = size.minDimension / 2f,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        GlossyIconCircle(
            iconRes = iconRes,
            palette = palette,
            size = buttonSize,
            glyphSize = glyphSize,
            contentDescription = contentDescription,
            modifier = Modifier
                .clip(CircleShape)
                .bounceClick(onClick = onClick),
        )
    }
}
