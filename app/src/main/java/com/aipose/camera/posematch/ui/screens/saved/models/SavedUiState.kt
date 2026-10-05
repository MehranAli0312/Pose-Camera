package com.aipose.camera.posematch.ui.screens.saved.models

sealed interface SavedUiState {

    data object Loading : SavedUiState

    data class Content(
        val selectedTab: SavedTab,
        val sort: SavedSort,
        val shots: List<SavedShot>,
        val poses: List<SavedPose>
    ) : SavedUiState {
        val totalCount: Int get() = shots.size + poses.size
    }
}
