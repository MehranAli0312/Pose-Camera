package com.aipose.camera.posematch.ui.screens.saved.models

import androidx.compose.runtime.Immutable
import com.aipose.camera.posematch.domain.models.Pose

@Immutable
data class SavedPose(
    val pose: Pose,
    val savedAtMillis: Long,
    val isLocked: Boolean
)
