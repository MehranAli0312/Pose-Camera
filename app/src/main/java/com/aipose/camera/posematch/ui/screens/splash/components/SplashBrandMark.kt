package com.aipose.camera.posematch.ui.screens.splash.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.Violet

internal val SplashBrandMarkSize = 112.dp

private val OuterRingRadius = 128.dp
private val InnerRingRadius = 106.dp
private val OuterRingWidth = 1.dp
private val InnerRingWidth = 1.5.dp
private const val OUTER_RING_ALPHA = 0.12f
private const val INNER_RING_FILL_ALPHA = 0.06f
private const val INNER_RING_STROKE_ALPHA = 0.22f

@Composable
internal fun SplashBrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(SplashBrandMarkSize)
            .drawBehind {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    color = Indigo.copy(alpha = OUTER_RING_ALPHA),
                    radius = OuterRingRadius.toPx(),
                    center = center,
                    style = Stroke(width = OuterRingWidth.toPx()),
                )
                drawCircle(
                    color = Violet.copy(alpha = INNER_RING_FILL_ALPHA),
                    radius = InnerRingRadius.toPx(),
                    center = center,
                )
                drawCircle(
                    color = Violet.copy(alpha = INNER_RING_STROKE_ALPHA),
                    radius = InnerRingRadius.toPx(),
                    center = center,
                    style = Stroke(width = InnerRingWidth.toPx()),
                )
            },
    ) {
        GlossyIconBadge(
            iconRes = R.drawable.ic_pose_logo,
            palette = GlossyBadgePalette.Brand,
            size = SplashBrandMarkSize,
        )
    }
}
