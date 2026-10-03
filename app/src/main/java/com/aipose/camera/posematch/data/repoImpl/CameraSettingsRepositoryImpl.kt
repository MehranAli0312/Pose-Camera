package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.repo.CameraSettingsRepository
import kotlinx.coroutines.flow.Flow

class CameraSettingsRepositoryImpl(
    private val dataStore: AppDataStore
) : CameraSettingsRepository {

    override fun getRetainSkeleton(): Flow<Boolean> = dataStore.getRetainSkeleton()

    override suspend fun setRetainSkeleton(retain: Boolean) {
        dataStore.setRetainSkeleton(retain)
    }

    override fun isCameraCoachSeen(): Flow<Boolean> = dataStore.isCameraCoachSeen()

    override suspend fun markCameraCoachSeen() {
        dataStore.markCameraCoachSeen()
    }
}
