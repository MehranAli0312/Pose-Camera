package com.aipose.camera.posematch.ui.screens.home.models

import com.aipose.camera.posematch.domain.models.Pose

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Content(
        val heroPose: Pose?,
        val sections: List<PoseCategorySection>,
        val searchResults: List<Pose>?
    ) : HomeUiState {
        val isSearching: Boolean get() = searchResults != null
    }
}
