package com.aipose.camera.posematch.ui.screens.collections.models

sealed interface CollectionsUiState {

    data object Loading : CollectionsUiState

    data class Content(
        val albums: List<CaptureAlbum>,
        val favoritePoses: List<FavoritePose>,
        val filter: CollectionsFilter,
        val recentPerfectShots: Int,
    ) : CollectionsUiState
}
