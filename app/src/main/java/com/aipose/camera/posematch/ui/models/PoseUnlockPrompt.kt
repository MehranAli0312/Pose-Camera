package com.aipose.camera.posematch.ui.models

import androidx.compose.runtime.Immutable
import com.aipose.camera.posematch.domain.models.Pose

@Immutable
data class PoseUnlockPrompt(
    val pose: Pose,
    val isAdLoading: Boolean = false,
)
