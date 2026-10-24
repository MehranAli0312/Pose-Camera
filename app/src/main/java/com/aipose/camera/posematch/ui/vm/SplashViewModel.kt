package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import com.aipose.camera.posematch.domain.usecase.SplashStatusUseCase
import com.aipose.camera.posematch.domain.usecase.StartPremiumUseCase

class SplashViewModel(
    private val getSplashStatusUseCase: SplashStatusUseCase,
    private val startPremiumUseCase: StartPremiumUseCase,
) : ViewModel() {

    suspend fun getSplashStatus() = getSplashStatusUseCase.getSplashStatus()

    suspend fun writeSplashStatus() = getSplashStatusUseCase.writeSplashStatus()

    suspend fun showPremiumAfterSplash(isFirstSession: Boolean) =
        startPremiumUseCase.showAfterSplash(isFirstSession)

    suspend fun showPremiumAfterLanguage() = startPremiumUseCase.showAfterLanguage()

    suspend fun showPremiumAfterOnboarding() = startPremiumUseCase.showAfterOnboarding()

}
