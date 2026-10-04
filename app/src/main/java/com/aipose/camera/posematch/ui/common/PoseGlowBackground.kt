package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PoseCyan
import com.aipose.camera.posematch.ui.theme.PoseGlowPurple
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.studioBackgroundBrush

private const val DESIGN_WIDTH = 390f
private const val DESIGN_HEIGHT = 980f

@Immutable
data class PoseGlow(
    val color: Color,
    val alpha: Float,
    val centerX: Float,
    val centerY: Float,
    val radius: Float
)

object PoseGlows {

    val Home = listOf(
        PoseGlow(Indigo, 0.40f, 50f, 80f, 240f),
        PoseGlow(PoseGlowPurple, 0.32f, 358f, 268f, 215f),
        PoseGlow(PoseCyan, 0.18f, 30f, 690f, 205f),
        PoseGlow(PosePink, 0.22f, 346f, 852f, 205f),
    )

    val Saved = listOf(
        PoseGlow(PosePink, 0.26f, 40f, 70f, 230f),
        PoseGlow(PoseGlowPurple, 0.26f, 370f, 380f, 210f),
        PoseGlow(Indigo, 0.22f, 20f, 760f, 200f),
    )
}

@Composable
fun PoseGlowBackground(
    modifier: Modifier = Modifier,
    glows: List<PoseGlow> = PoseGlows.Home,
    content: @Composable BoxScope.() -> Unit,
) {
    val backgroundBrush = studioBackgroundBrush()
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .drawBehind {
                drawRect(backgroundBrush)
                drawGlows(glows)
            },
        content = content,
    )
}

private fun DrawScope.drawGlows(glows: List<PoseGlow>) {
    val scaleX = size.width / DESIGN_WIDTH
    val scaleY = size.height / DESIGN_HEIGHT
    glows.forEach { glow ->
        val radius = glow.radius * scaleX
        val center = Offset(glow.centerX * scaleX, glow.centerY * scaleY)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.color.copy(alpha = glow.alpha), Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}
