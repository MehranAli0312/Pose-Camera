package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.ui.screens.photoSuccess.models.PhotoSuccessUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotoSuccessViewModel(
    private val captureUseCase: CaptureUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoSuccessUiState>(PhotoSuccessUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var requestedCaptureId: Long? = null
    private var captureJob: Job? = null

    fun onCaptureRequested(captureId: Long) {
        if (requestedCaptureId == captureId) return
        requestedCaptureId = captureId
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            captureUseCase.observeCaptures().collect { captures ->
                val capture = captures.firstOrNull { it.id == captureId } ?: return@collect
                _uiState.value = PhotoSuccessUiState.Content(
                    capture = capture,
                    isPersonalBest = captureProgressUseCase.isPersonalBest(captures, capture),
                    savedShotCount = captures.size,
                )
            }
        }
    }
}
