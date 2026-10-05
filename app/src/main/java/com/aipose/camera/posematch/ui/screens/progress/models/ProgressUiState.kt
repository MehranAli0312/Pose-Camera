package com.aipose.camera.posematch.ui.screens.progress.models

import com.aipose.camera.posematch.domain.models.ProgressPeriod
import com.aipose.camera.posematch.domain.models.ProgressStats

sealed interface ProgressUiState {
    data object Loading : ProgressUiState

    data class Content(
        val period: ProgressPeriod,
        val stats: ProgressStats
    ) : ProgressUiState
}
