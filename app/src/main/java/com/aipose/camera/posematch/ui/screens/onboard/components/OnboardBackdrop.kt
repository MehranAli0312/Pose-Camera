package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.aipose.camera.posematch.ui.theme.AppTheme

private const val PRIMARY_GLOW_X = 0.14f
private const val PRIMARY_GLOW_Y = 0.14f
private const val PRIMARY_GLOW_RADIUS = 0.8f
private const val SECONDARY_GLOW_X = 0.92f
private const val SECONDARY_GLOW_Y = 0.44f
private const val SECONDARY_GLOW_RADIUS = 0.75f

@Composable
internal fun OnboardBackdrop(modifier: Modifier = Modifier) {
    val colors = AppTheme.extendedColors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Spacer(
        modifier = modifier.drawBehind {
            drawGlow(PRIMARY_GLOW_X, PRIMARY_GLOW_Y, PRIMARY_GLOW_RADIUS, colors.glowPrimary, isRtl)
            drawGlow(SECONDARY_GLOW_X, SECONDARY_GLOW_Y, SECONDARY_GLOW_RADIUS, colors.glowSecondary, isRtl)
        },
    )
}

private fun DrawScope.drawGlow(
    xFraction: Float,
    yFraction: Float,
    radiusFraction: Float,
    color: Color,
    isRtl: Boolean,
) {
    val x = if (isRtl) 1f - xFraction else xFraction
    val center = Offset(size.width * x, size.height * yFraction)
    val radius = size.width * radiusFraction
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
