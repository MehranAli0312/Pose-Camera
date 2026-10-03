package com.aipose.camera.posematch.domain.repo

import kotlinx.coroutines.flow.Flow

interface SplashStatusRepository {

    suspend fun getSplashStatus(): Flow<Boolean>
    suspend fun writeSplashStatus()

}
