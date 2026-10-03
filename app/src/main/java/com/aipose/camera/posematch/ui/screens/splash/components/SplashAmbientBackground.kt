package com.aipose.camera.posematch.ui.screens.splash.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
internal fun SplashAmbientBackground(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    val primary = colorScheme.primary
    val tertiary = colorScheme.tertiary

    val topRightAlpha = if (isDark) 0.50f else 0.28f
    val bottomLeftAlpha = if (isDark) 0.42f else 0.22f
    val midGlowAlpha = if (isDark) 0.12f else 0.06f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .drawBehind {
                val w = size.width
                val h = size.height
                val glowRadius = maxOf(w, h) * 0.72f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tertiary.copy(alpha = topRightAlpha),
                            tertiary.copy(alpha = topRightAlpha * 0.35f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.92f, h * 0.06f),
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = Offset(w * 0.92f, h * 0.06f),
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = bottomLeftAlpha),
                            primary.copy(alpha = bottomLeftAlpha * 0.35f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.08f, h * 0.94f),
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = Offset(w * 0.08f, h * 0.94f),
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = midGlowAlpha),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.5f, h * 0.42f),
                        radius = maxOf(w, h) * 0.45f,
                    ),
                    radius = maxOf(w, h) * 0.45f,
                    center = Offset(w * 0.5f, h * 0.42f),
                )
            }
    )
}
