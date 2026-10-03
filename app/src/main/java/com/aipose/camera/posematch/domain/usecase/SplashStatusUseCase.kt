package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.repo.SplashStatusRepository
import kotlinx.coroutines.flow.Flow

class SplashStatusUseCase(private val splashStatusRepository: SplashStatusRepository) {

    suspend fun getSplashStatus(): Flow<Boolean> {
        return splashStatusRepository.getSplashStatus()
    }

    suspend fun writeSplashStatus() {
        splashStatusRepository.writeSplashStatus()
    }

}
