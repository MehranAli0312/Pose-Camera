package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import com.aipose.camera.posematch.domain.usecase.SplashStatusUseCase

class SplashViewModel(private val getSplashStatusUseCase: SplashStatusUseCase) :
    ViewModel() {

    suspend fun getSplashStatus() = getSplashStatusUseCase.getSplashStatus()

    suspend fun writeSplashStatus() = getSplashStatusUseCase.writeSplashStatus()

}
