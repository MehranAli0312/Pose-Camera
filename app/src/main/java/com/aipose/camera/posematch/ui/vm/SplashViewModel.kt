package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.usecase.SplashStatusUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import com.aipose.camera.posematch.ui.screens.launch.LaunchFlowPlanner
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getSplashStatusUseCase: SplashStatusUseCase,
    private val launchFlowPlanner: LaunchFlowPlanner,
    private val trackEventUseCase: TrackEventUseCase,
) : ViewModel() {

    private var cachedLaunchFlow: LaunchFlow? = null

    fun trackScreen(event: AnalyticsEvent) {
        viewModelScope.launch { trackEventUseCase.track(event) }
    }

    suspend fun getSplashStatus() = getSplashStatusUseCase.getSplashStatus()

    suspend fun writeSplashStatus() = getSplashStatusUseCase.writeSplashStatus()

    suspend fun launchFlow(): LaunchFlow {
        cachedLaunchFlow?.let { return it }
        val isFirstSession = getSplashStatusUseCase.getSplashStatus().first()
        val returningLaunchCount = if (isFirstSession) {
            0
        } else {
            getSplashStatusUseCase.recordReturningLaunch()
        }
        return launchFlowPlanner.plan(isFirstSession, returningLaunchCount)
            .also { flow -> cachedLaunchFlow = flow }
    }

}
