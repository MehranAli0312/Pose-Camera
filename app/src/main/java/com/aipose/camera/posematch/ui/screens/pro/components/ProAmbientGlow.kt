package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.aipose.camera.posematch.ui.theme.AppTheme

private const val PRIMARY_GLOW_X = 0.18f
private const val SECONDARY_GLOW_X = 0.9f
private const val PRIMARY_GLOW_Y = 0.02f
private const val SECONDARY_GLOW_Y = 0.12f
private const val GLOW_RADIUS = 0.75f

@Composable
internal fun Modifier.proAmbientGlow(): Modifier {
    val colors = AppTheme.extendedColors
    val primary = colors.glowPrimary
    val secondary = colors.glowSecondary
    return drawBehind {
        drawGlow(Offset(size.width * PRIMARY_GLOW_X, size.height * PRIMARY_GLOW_Y), primary)
        drawGlow(Offset(size.width * SECONDARY_GLOW_X, size.height * SECONDARY_GLOW_Y), secondary)
    }
}

private fun DrawScope.drawGlow(center: Offset, color: Color) {
    val radius = size.width * GLOW_RADIUS
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
