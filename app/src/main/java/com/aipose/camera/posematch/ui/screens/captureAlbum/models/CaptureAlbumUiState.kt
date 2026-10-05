package com.aipose.camera.posematch.ui.screens.captureAlbum.models

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsSort

sealed interface CaptureAlbumUiState {

    data object Loading : CaptureAlbumUiState

    data object Removed : CaptureAlbumUiState

    data class Content(
        val locationLabel: String,
        val shots: List<Capture>,
        val totalCount: Int,
        val averageMatch: Int,
        val bestMatch: Int,
        val lastShotDaysAgo: Int,
        val filter: CollectionsFilter,
        val sort: CollectionsSort,
        val isRemoveDialogVisible: Boolean,
        val isSortSheetVisible: Boolean
    ) : CaptureAlbumUiState
}
