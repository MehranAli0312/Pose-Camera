package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.models.CaptureTimer
import com.aipose.camera.posematch.ui.theme.PoseVioletPale

@Composable
internal fun CaptureTimerMenu(
    selected: CaptureTimer,
    onSelect: (CaptureTimer) -> Unit,
    onDismiss: () -> Unit,
) {
    val offsetY = with(LocalDensity.current) { CameraTopBarButtonSize.roundToPx() }
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CaptureTimer.entries.forEach { timer ->
                CameraTopBarButton(
                    iconRes = timer.iconRes,
                    tint = if (timer == selected) PoseVioletPale else Color.White,
                    contentDescription = timerLabel(timer),
                    onClick = { onSelect(timer) },
                    badgeText = timerBadge(timer),
                )
            }
        }
    }
}

@Composable
internal fun timerBadge(timer: CaptureTimer): String? =
    if (timer.isEnabled) stringResource(R.string.camera_timer_badge, timer.seconds) else null

@Composable
private fun timerLabel(timer: CaptureTimer): String =
    if (timer.isEnabled) {
        stringResource(R.string.settings_capture_timer_seconds, timer.seconds)
    } else {
        stringResource(R.string.settings_capture_timer_off)
    }
