package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.usecase.CameraSettingsUseCase
import com.aipose.camera.posematch.ui.models.CaptureTimer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CameraSettingsViewModel(
    private val cameraSettingsUseCase: CameraSettingsUseCase
) : ViewModel() {

    val retainSkeleton: StateFlow<Boolean> = cameraSettingsUseCase.getRetainSkeleton()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), true)

    val captureTimer: StateFlow<CaptureTimer> = cameraSettingsUseCase.getCaptureTimerSeconds()
        .map(CaptureTimer::fromSeconds)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), CaptureTimer.Off)

    fun setRetainSkeleton(retain: Boolean) {
        viewModelScope.launch { cameraSettingsUseCase.setRetainSkeleton(retain) }
    }

    fun setCaptureTimer(timer: CaptureTimer) {
        viewModelScope.launch { cameraSettingsUseCase.setCaptureTimerSeconds(timer.seconds) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
