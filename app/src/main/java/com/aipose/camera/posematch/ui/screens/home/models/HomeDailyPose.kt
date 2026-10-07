package com.aipose.camera.posematch.ui.screens.home.models

import androidx.compose.runtime.Immutable
import com.aipose.camera.posematch.domain.models.Pose

@Immutable
data class HomeDailyPose(
    val pose: Pose,
    val isLocked: Boolean
)
