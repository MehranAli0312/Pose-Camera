package com.aipose.camera.posematch.ui.screens.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.ImmersiveDialogWindowEffect
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.screens.bottomSheet.components.GoPremiumButton
import com.aipose.camera.posematch.ui.screens.bottomSheet.components.PremiumPosePreview
import com.aipose.camera.posematch.ui.screens.bottomSheet.components.WatchAdToUnlockButton
import com.aipose.camera.posematch.ui.theme.PosePremiumSheet
import com.aipose.camera.posematch.ui.theme.PoseSheetShape
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TitleSize = 20.sp
private val MessageSize = 14.sp
private const val MESSAGE_ALPHA = 0.7f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumFeatureBottomSheet(
    posePreviewPath: String,
    poseTitle: String,
    isAdLoading: Boolean,
    onWatchAdClick: () -> Unit,
    onGoPremiumClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !isAdLoading },
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isAdLoading) onDismissRequest() },
        sheetState = sheetState,
        shape = PoseSheetShape,
        containerColor = PosePremiumSheet,
    ) {
        ImmersiveDialogWindowEffect()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeBottomSystemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PremiumPosePreview(
                imagePath = posePreviewPath,
                contentDescription = poseTitle,
                modifier = Modifier.padding(top = 8.dp),
            )

            Text(
                text = stringResource(R.string.premium_pose_title),
                style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 22.dp),
            )

            Text(
                text = stringResource(R.string.premium_pose_message),
                style = poseTextStyle(
                    MessageSize,
                    FontWeight.Normal,
                    Color.White.copy(alpha = MESSAGE_ALPHA),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            WatchAdToUnlockButton(
                isLoading = isAdLoading,
                onClick = onWatchAdClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoPremiumButton(
                enabled = !isAdLoading,
                onClick = onGoPremiumClick,
            )
        }
    }
}
