package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.usecase.CameraSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CameraSettingsViewModel(
    private val cameraSettingsUseCase: CameraSettingsUseCase
) : ViewModel() {

    val retainSkeleton: StateFlow<Boolean> = cameraSettingsUseCase.getRetainSkeleton()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), true)

    fun setRetainSkeleton(retain: Boolean) {
        viewModelScope.launch { cameraSettingsUseCase.setRetainSkeleton(retain) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
