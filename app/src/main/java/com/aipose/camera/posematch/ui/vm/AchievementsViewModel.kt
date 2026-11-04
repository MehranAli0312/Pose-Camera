package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.usecase.AchievementsUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import com.aipose.camera.posematch.ui.screens.achievements.models.AchievementsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AchievementsViewModel(
    captureUseCase: CaptureUseCase,
    favoritePoseUseCase: FavoritePoseUseCase,
    private val achievementsUseCase: AchievementsUseCase,
    private val trackEventUseCase: TrackEventUseCase,
) : ViewModel() {

    init {
        viewModelScope.launch { trackEventUseCase.track(AnalyticsEvent.Achievements) }
    }

    val uiState: StateFlow<AchievementsUiState> = combine(
        captureUseCase.observeCaptures(),
        favoritePoseUseCase.observeFavorites(),
    ) { captures, favorites ->
        AchievementsUiState.Content(
            summary = achievementsUseCase.summaryOf(captures, favorites.size),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        AchievementsUiState.Loading
    )

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
