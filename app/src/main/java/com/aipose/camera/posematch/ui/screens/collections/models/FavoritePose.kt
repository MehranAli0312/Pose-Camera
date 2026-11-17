package com.aipose.camera.posematch.ui.screens.collections.models

import androidx.compose.runtime.Immutable
import com.aipose.camera.posematch.domain.models.Pose

@Immutable
data class FavoritePose(
    val pose: Pose,
    val savedAtMillis: Long,
    val isLocked: Boolean,
)
