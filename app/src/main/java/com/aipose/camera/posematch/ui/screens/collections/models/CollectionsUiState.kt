package com.aipose.camera.posematch.ui.screens.collections.models

sealed interface CollectionsUiState {

    data object Loading : CollectionsUiState

    data class Content(
        val totalCount: Int,
        val albums: List<CaptureAlbum>
    ) : CollectionsUiState {
        val isEmpty: Boolean get() = albums.isEmpty()
    }
}
