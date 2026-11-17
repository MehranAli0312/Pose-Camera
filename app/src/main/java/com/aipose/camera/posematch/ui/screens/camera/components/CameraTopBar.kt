package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseBackButton
import com.aipose.camera.posematch.ui.models.CaptureTimer
import com.aipose.camera.posematch.ui.screens.camera.models.CameraTool
import com.aipose.camera.posematch.ui.screens.camera.models.FlashMode
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseVioletPale

@Composable
internal fun CameraTopBar(
    flashMode: FlashMode,
    timer: CaptureTimer,
    isTimerMenuVisible: Boolean,
    isToolActive: (CameraTool) -> Boolean,
    onBack: () -> Unit,
    onToolClick: (CameraTool) -> Unit,
    onTimerSelect: (CaptureTimer) -> Unit,
    onTimerMenuDismiss: () -> Unit,
    onToggleFacing: () -> Unit,
    onToggleFlash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(CameraTopBarButtonSize),
            contentAlignment = Alignment.CenterStart,
        ) {
            PoseBackButton(onClick = onBack)
        }
        Spacer(modifier = Modifier.weight(1f))
        CameraTool.entries.forEach { tool ->
            Box {
                val isActive = isToolActive(tool)
                CameraTopBarButton(
                    iconRes = tool.iconRes,
                    tint = if (isActive) PoseVioletPale else Color.White,
                    contentDescription = stringResource(tool.labelRes),
                    onClick = { onToolClick(tool) },
                    badgeText = if (tool == CameraTool.Timer) timerBadge(timer) else null,
                )
                if (tool == CameraTool.Timer && isTimerMenuVisible) {
                    CaptureTimerMenu(
                        selected = timer,
                        onSelect = onTimerSelect,
                        onDismiss = onTimerMenuDismiss,
                    )
                }
            }
        }
        CameraTopBarButton(
            iconRes = R.drawable.ic_camera_flip,
            tint = Color.White,
            contentDescription = stringResource(R.string.camera_switch),
            onClick = onToggleFacing,
        )
        CameraTopBarButton(
            iconRes = if (flashMode != FlashMode.Off) R.drawable.ic_camera_flash else R.drawable.ic_camera_flash_off,
            tint = if (flashMode != FlashMode.Off) PoseAmber400 else Color.White,
            contentDescription = stringResource(R.string.camera_flash),
            onClick = onToggleFlash,
            badgeText = if (flashMode == FlashMode.Auto) stringResource(R.string.camera_flash_auto) else null,
        )
    }
}
