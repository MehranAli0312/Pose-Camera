package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.CaptureProgress
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    captureUseCase: CaptureUseCase,
    captureProgressUseCase: CaptureProgressUseCase,
) : ViewModel() {

    val progress: StateFlow<CaptureProgress> = captureUseCase.observeCaptures()
        .map(captureProgressUseCase::progressOf)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), CaptureProgress.Empty)

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
