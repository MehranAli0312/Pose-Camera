package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.models.CaptureProgress
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.PremiumSubscriptionUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    captureUseCase: CaptureUseCase,
    captureProgressUseCase: CaptureProgressUseCase,
    premiumSubscriptionUseCase: PremiumSubscriptionUseCase,
    private val trackEventUseCase: TrackEventUseCase,
) : ViewModel() {

    init {
        viewModelScope.launch { trackEventUseCase.track(AnalyticsEvent.Settings) }
    }

    val progress: StateFlow<CaptureProgress> = captureUseCase.observeCaptures()
        .map(captureProgressUseCase::progressOf)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), CaptureProgress.Empty)

    val showProCard: StateFlow<Boolean> = premiumSubscriptionUseCase.observeFreeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), false)

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
