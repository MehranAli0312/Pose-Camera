package com.aipose.camera.posematch.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

@Composable
fun PoseDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PoseDialog(
        iconRes = R.drawable.ic_trash,
        iconPalette = GlossyBadgePalette.Rose,
        title = title,
        message = message,
        confirmLabel = stringResource(R.string.action_delete),
        onConfirm = onConfirm,
        dismissLabel = stringResource(R.string.action_cancel),
        onDismiss = onDismiss,
    )
}
