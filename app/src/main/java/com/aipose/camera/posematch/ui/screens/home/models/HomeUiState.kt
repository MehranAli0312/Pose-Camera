package com.aipose.camera.posematch.ui.screens.home.models

import com.aipose.camera.posematch.domain.models.CaptureProgress
import com.aipose.camera.posematch.domain.models.Pose

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Content(
        val hero: HomeHero?,
        val progress: CaptureProgress,
        val lockedPose: Pose?,
        val unlockTarget: HomeUnlockTarget
    ) : HomeUiState
}
