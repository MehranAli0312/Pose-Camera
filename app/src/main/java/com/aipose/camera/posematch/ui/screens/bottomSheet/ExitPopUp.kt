package com.aipose.camera.posematch.ui.screens.bottomSheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseDialog
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

@Composable
fun ExitPopUp(
    openFullDialogCustom: MutableState<Boolean>,
    callbackExit: () -> Unit,
) {
    if (!openFullDialogCustom.value) return

    PoseDialog(
        iconRes = R.drawable.ic_pose_exit,
        iconPalette = GlossyBadgePalette.HeroCta,
        title = stringResource(R.string.exitApp),
        message = stringResource(R.string.exitAppDescription),
        confirmLabel = stringResource(R.string.yes),
        onConfirm = callbackExit,
        dismissLabel = stringResource(R.string.no),
        onDismiss = { openFullDialogCustom.value = false },
    )
}
