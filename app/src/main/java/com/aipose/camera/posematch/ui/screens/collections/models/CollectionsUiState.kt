package com.aipose.camera.posematch.ui.screens.collections.models

sealed interface CollectionsUiState {

    data object Loading : CollectionsUiState

    data class Content(
        val totalCount: Int,
        val stats: CollectionsStats,
        val albums: List<CaptureAlbum>,
        val filter: CollectionsFilter,
        val sort: CollectionsSort,
        val recentPerfectShots: Int,
        val isSortSheetVisible: Boolean
    ) : CollectionsUiState {
        val isEmpty: Boolean get() = albums.isEmpty()
    }
}
