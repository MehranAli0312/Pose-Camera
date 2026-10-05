package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.ProgressPeriod
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.ProgressStatsUseCase
import com.aipose.camera.posematch.ui.screens.progress.models.ProgressUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProgressViewModel(
    captureUseCase: CaptureUseCase,
    private val progressStatsUseCase: ProgressStatsUseCase
) : ViewModel() {

    private val _period = MutableStateFlow(ProgressPeriod.Week)

    val uiState: StateFlow<ProgressUiState> = combine(
        captureUseCase.observeCaptures(),
        _period,
    ) { captures, period ->
        ProgressUiState.Content(
            period = period,
            stats = progressStatsUseCase.statsFor(captures, period),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        ProgressUiState.Loading
    )

    fun selectPeriod(period: ProgressPeriod) {
        _period.value = period
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
