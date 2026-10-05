package com.aipose.camera.posematch.ui.screens.achievements.models

import com.aipose.camera.posematch.domain.models.AchievementsSummary

sealed interface AchievementsUiState {
    data object Loading : AchievementsUiState

    data class Content(val summary: AchievementsSummary) : AchievementsUiState
}
