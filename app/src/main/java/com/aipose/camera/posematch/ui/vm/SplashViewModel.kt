package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.usecase.SplashStatusUseCase
import com.aipose.camera.posematch.domain.usecase.StartPremiumUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getSplashStatusUseCase: SplashStatusUseCase,
    private val startPremiumUseCase: StartPremiumUseCase,
    private val trackEventUseCase: TrackEventUseCase,
) : ViewModel() {

    fun trackScreen(event: AnalyticsEvent) {
        viewModelScope.launch { trackEventUseCase.track(event) }
    }

    suspend fun getSplashStatus() = getSplashStatusUseCase.getSplashStatus()

    suspend fun writeSplashStatus() = getSplashStatusUseCase.writeSplashStatus()

    suspend fun showPremiumAfterSplash(isFirstSession: Boolean) =
        startPremiumUseCase.showAfterSplash(isFirstSession)

    suspend fun showPremiumAfterLanguage() = startPremiumUseCase.showAfterLanguage()

    suspend fun showPremiumAfterOnboarding() = startPremiumUseCase.showAfterOnboarding()

}
