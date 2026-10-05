package com.aipose.camera.posematch.ui.screens.photoSuccess.models

import com.aipose.camera.posematch.domain.models.Capture

sealed interface PhotoSuccessUiState {
    data object Loading : PhotoSuccessUiState

    data class Content(
        val capture: Capture,
        val isPersonalBest: Boolean,
        val savedShotCount: Int
    ) : PhotoSuccessUiState
}
