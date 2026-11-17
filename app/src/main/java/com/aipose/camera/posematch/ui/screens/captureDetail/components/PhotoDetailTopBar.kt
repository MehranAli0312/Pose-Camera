package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aipose.camera.posematch.ui.common.PoseBackButton
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing

@Composable
internal fun PhotoDetailTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = PoseScreenGutter, end = PoseScreenGutter, top = PoseScreenTopSpacing),
    ) {
        PoseBackButton(onClick = onBack)
    }
}
