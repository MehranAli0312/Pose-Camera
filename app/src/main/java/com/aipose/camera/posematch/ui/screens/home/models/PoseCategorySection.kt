package com.aipose.camera.posematch.ui.screens.home.models

import com.aipose.camera.posematch.domain.models.Pose

data class PoseCategorySection(
    val category: String,
    val previewPoses: List<Pose>,
    val totalCount: Int
) {
    val hasMore: Boolean get() = totalCount > previewPoses.size
}
