package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.LayoutDirection
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PoseGlowPurple
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.studioBackgroundBrush

private const val DESIGN_WIDTH = 390f
private const val DESIGN_HEIGHT = 844f

private data class OnboardGlow(
    val color: Color,
    val alpha: Float,
    val centerX: Float,
    val centerY: Float,
    val radiusX: Float,
    val radiusY: Float,
)

private val StageGlow = OnboardGlow(Violet, 0.16f, 195f, 250f, 180f, 150f)

@Composable
internal fun OnboardBackdrop(
    accent: Color,
    accentAlpha: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val backgroundBrush = studioBackgroundBrush()
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .drawBehind {
                drawRect(backgroundBrush)
                drawGlow(OnboardGlow(Indigo, 0.34f, 40f, 60f, 230f, 200f))
                drawGlow(OnboardGlow(PoseGlowPurple, 0.28f, 370f, 250f, 210f, 200f))
                drawGlow(OnboardGlow(accent, accentAlpha, 350f, 790f, 215f, 185f))
                drawGlow(StageGlow)
            },
        content = content,
    )
}

private fun DrawScope.drawGlow(glow: OnboardGlow) {
    val scaleX = size.width / DESIGN_WIDTH
    val scaleY = size.height / DESIGN_HEIGHT
    val centerX = if (layoutDirection == LayoutDirection.Rtl) {
        DESIGN_WIDTH - glow.centerX
    } else {
        glow.centerX
    }
    val center = Offset(centerX * scaleX, glow.centerY * scaleY)
    val radiusX = glow.radiusX * scaleX
    val radiusY = glow.radiusY * scaleY
    scale(scaleX = 1f, scaleY = radiusY / radiusX, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.color.copy(alpha = glow.alpha), Color.Transparent),
                center = center,
                radius = radiusX,
            ),
            radius = radiusX,
            center = center,
        )
    }
}
