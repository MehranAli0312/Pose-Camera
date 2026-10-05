package com.aipose.camera.posematch.ui.screens.poseAlbum.models

import com.aipose.camera.posematch.domain.models.Pose

sealed interface PoseAlbumUiState {

    data object Loading : PoseAlbumUiState

    data class Content(
        val totalCount: Int,
        val categories: List<PoseCategoryCount>,
        val selectedCategory: String?,
        val poses: List<Pose>,
        val savedPoseIds: Set<Int>,
        val sort: PoseSort,
        val isSortSheetVisible: Boolean
    ) : PoseAlbumUiState
}
