package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseCtaButton
import com.aipose.camera.posematch.ui.common.PoseDialogCard
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

private val ScreenInset = 28.dp
private const val SCRIM_ALPHA = 0.82f

@Composable
internal fun CameraPermissionCard(
    isBlocked: Boolean,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .click { }
            .padding(ScreenInset),
        contentAlignment = Alignment.Center,
    ) {
        PoseDialogCard(
            iconRes = R.drawable.ic_pose_camera,
            iconPalette = GlossyBadgePalette.Violet,
            title = stringResource(R.string.perm_camera_title),
            message = stringResource(
                if (isBlocked) R.string.perm_camera_blocked_desc else R.string.perm_camera_desc
            ),
        ) {
            PoseCtaButton(
                text = stringResource(
                    if (isBlocked) R.string.perm_open_settings else R.string.perm_allow
                ),
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
