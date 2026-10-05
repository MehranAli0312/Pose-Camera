package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.repo.CameraSettingsRepository
import kotlinx.coroutines.flow.Flow

class CameraSettingsRepositoryImpl(
    private val dataStore: AppDataStore
) : CameraSettingsRepository {

    override fun getKeepPoseOverlay(): Flow<Boolean> = dataStore.getKeepPoseOverlay()

    override suspend fun setKeepPoseOverlay(keepOverlay: Boolean) {
        dataStore.setKeepPoseOverlay(keepOverlay)
    }

    override fun isCameraCoachSeen(): Flow<Boolean> = dataStore.isCameraCoachSeen()

    override suspend fun markCameraCoachSeen() {
        dataStore.markCameraCoachSeen()
    }

    override fun getCaptureTimerSeconds(): Flow<Int> = dataStore.getCaptureTimerSeconds()

    override suspend fun setCaptureTimerSeconds(seconds: Int) {
        dataStore.setCaptureTimerSeconds(seconds)
    }
}
