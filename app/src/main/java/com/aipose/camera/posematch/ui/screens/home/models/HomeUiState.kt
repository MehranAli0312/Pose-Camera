package com.aipose.camera.posematch.ui.screens.home.models

import com.aipose.camera.posematch.domain.models.CaptureProgress
import com.aipose.camera.posematch.domain.models.Pose

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Content(
        val hero: HomeHero?,
        val quickActions: List<HomeQuickAction>,
        val progress: CaptureProgress,
        val poseOfTheDay: HomeDailyPose?,
        val lockedPose: Pose?,
        val unlockTarget: HomeUnlockTarget,
        val isUnlockAdLoading: Boolean
    ) : HomeUiState
}
