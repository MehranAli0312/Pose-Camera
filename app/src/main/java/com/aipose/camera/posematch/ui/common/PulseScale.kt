package com.aipose.camera.posematch.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val DEFAULT_PULSE_MAX_SCALE = 1.05f
private const val DEFAULT_PULSE_DURATION_MS = 800

@Composable
fun Modifier.pulseScale(
    enabled: Boolean,
    maxScale: Float = DEFAULT_PULSE_MAX_SCALE,
    durationMillis: Int = DEFAULT_PULSE_DURATION_MS,
): Modifier {
    if (!enabled) return this
    val pulse = rememberInfiniteTransition(label = "pulseScale")
    val scale = pulse.animateFloat(
        initialValue = 1f,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
