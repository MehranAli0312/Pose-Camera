package com.aipose.camera.posematch.ui.common

import android.os.SystemClock
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

private const val PRESSED_SCALE = 0.95f
private const val THROTTLED_CLICK_INTERVAL_MS = 600L

@Composable
fun rememberThrottledClick(onClick: () -> Unit): () -> Unit {
    val currentOnClick by rememberUpdatedState(onClick)
    var lastClickAtMs by remember { mutableLongStateOf(0L) }
    return remember {
        {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickAtMs >= THROTTLED_CLICK_INTERVAL_MS) {
                lastClickAtMs = now
                currentOnClick()
            }
        }
    }
}

fun Modifier.click(enabled: Boolean = true, onClick: () -> Unit = {}) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}

fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressedScale: Float = PRESSED_SCALE,
    onClick: () -> Unit = {},
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "bounceScale",
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bounceCombinedClick(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    pressedScale: Float = PRESSED_SCALE,
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "bounceCombinedScale",
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            },
        )
}
