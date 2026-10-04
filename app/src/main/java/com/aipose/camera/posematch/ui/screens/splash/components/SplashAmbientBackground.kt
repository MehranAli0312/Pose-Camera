package com.aipose.camera.posematch.ui.screens.splash.components

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
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PoseCyan
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.studioBackgroundBrush

private const val DESIGN_WIDTH = 390f
private const val DESIGN_HEIGHT = 844f

private data class SplashGlow(
    val color: Color,
    val alpha: Float,
    val centerX: Float,
    val centerY: Float,
    val radiusX: Float,
    val radiusY: Float,
)

private val Glows = listOf(
    SplashGlow(Violet, 0.38f, 195f, 330f, 265f, 255f),
    SplashGlow(Indigo, 0.30f, 25f, 70f, 205f, 185f),
    SplashGlow(PosePink, 0.22f, 365f, 770f, 215f, 185f),
    SplashGlow(PoseCyan, 0.16f, 15f, 640f, 185f, 165f),
)

@Composable
internal fun SplashAmbientBackground(
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
                drawGlows()
            },
        content = content,
    )
}

private fun DrawScope.drawGlows() {
    val scaleX = size.width / DESIGN_WIDTH
    val scaleY = size.height / DESIGN_HEIGHT
    Glows.forEach { glow ->
        val center = Offset(glow.centerX * scaleX, glow.centerY * scaleY)
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
}
