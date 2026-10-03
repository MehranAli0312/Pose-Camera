package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.repo.SplashStatusRepository
import kotlinx.coroutines.flow.Flow

class SplashStatusRepositoryImpl(
    private val dataStore: AppDataStore
) : SplashStatusRepository {
    override suspend fun getSplashStatus(): Flow<Boolean> {
        return dataStore.readOnBoardStatus()
    }

    override suspend fun writeSplashStatus() {
        dataStore.writeOnBoardStatus()
    }
}
