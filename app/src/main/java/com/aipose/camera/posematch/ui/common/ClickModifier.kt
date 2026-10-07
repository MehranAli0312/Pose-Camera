package com.aipose.camera.posematch.ui.common

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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

fun Modifier.click(enabled: Boolean = true, onClick: () -> Unit = {}): Modifier =
    clickable(
        interactionSource = null,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )

fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressedScale: Float = PRESSED_SCALE,
    onClick: () -> Unit = {},
): Modifier = this
    .clickable(
        interactionSource = null,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
    .then(BounceElement(enabled = enabled, pressedScale = pressedScale))
