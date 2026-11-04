package com.aipose.camera.posematch.ui.screens.home.models

import com.aipose.camera.posematch.domain.models.CaptureProgress
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseCategoryCount

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Content(
        val hero: HomeHero?,
        val categories: List<PoseCategoryCount>,
        val selectedCategory: String?,
        val poses: List<Pose>,
        val savedPoseIds: Set<Int>,
        val lockedPoseIds: Set<Int>,
        val progress: CaptureProgress,
        val lockedPose: Pose?,
        val unlockTarget: HomeUnlockTarget
    ) : HomeUiState
}
