package com.aipose.camera.posematch.ui.screens.poseAlbum.models

import com.aipose.camera.posematch.domain.models.Pose

sealed interface PoseAlbumUiState {

    data object Loading : PoseAlbumUiState

    data class Content(
        val categories: List<PoseCategoryCount>,
        val selectedCategory: String?,
        val poses: List<Pose>,
        val savedPoseIds: Set<Int>,
        val lockedPoseIds: Set<Int>,
        val lockedPose: Pose?,
        val isUnlockAdLoading: Boolean,
        val sort: PoseSort,
        val isSortSheetVisible: Boolean
    ) : PoseAlbumUiState
}
