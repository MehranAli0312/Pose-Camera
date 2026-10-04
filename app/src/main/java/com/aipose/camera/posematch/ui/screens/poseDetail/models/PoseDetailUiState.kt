package com.aipose.camera.posematch.ui.screens.poseDetail.models

import com.aipose.camera.posematch.domain.models.Pose

sealed interface PoseDetailUiState {

    data object Loading : PoseDetailUiState

    data class Content(
        val pose: Pose,
        val categoryCount: Int,
        val tryCount: Int,
        val bestMatch: Int?,
        val isSaved: Boolean
    ) : PoseDetailUiState
}
