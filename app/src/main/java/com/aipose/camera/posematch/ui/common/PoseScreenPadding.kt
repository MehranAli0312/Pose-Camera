package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val PoseScreenGutter = 20.dp
val PoseScreenTopSpacing = 22.dp

fun Modifier.poseScreenPadding(
    horizontal: Dp = PoseScreenGutter,
    top: Dp = PoseScreenTopSpacing,
): Modifier = this
    .statusBarsPadding()
    .padding(start = horizontal, end = horizontal, top = top)

fun Modifier.poseScreenInsets(top: Dp = PoseScreenTopSpacing): Modifier = this
    .statusBarsPadding()
    .padding(top = top)
