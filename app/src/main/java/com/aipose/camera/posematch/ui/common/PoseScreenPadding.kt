package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val PoseScreenGutter = 20.dp
val PoseScreenTopSpacing = 22.dp

@Composable
fun Modifier.poseScreenPadding(
    horizontal: Dp = PoseScreenGutter,
    top: Dp = PoseScreenTopSpacing,
): Modifier = this
    .safeTopSystemBarsPadding()
    .padding(start = horizontal, end = horizontal, top = top)
