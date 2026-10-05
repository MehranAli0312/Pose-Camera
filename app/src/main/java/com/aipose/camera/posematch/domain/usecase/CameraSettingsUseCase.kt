package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.repo.CameraSettingsRepository
import kotlinx.coroutines.flow.Flow

class CameraSettingsUseCase(
    private val cameraSettingsRepository: CameraSettingsRepository
) {

    fun getRetainSkeleton(): Flow<Boolean> = cameraSettingsRepository.getRetainSkeleton()

    suspend fun setRetainSkeleton(retain: Boolean) {
        cameraSettingsRepository.setRetainSkeleton(retain)
    }

    fun isCameraCoachSeen(): Flow<Boolean> = cameraSettingsRepository.isCameraCoachSeen()

    suspend fun markCameraCoachSeen() {
        cameraSettingsRepository.markCameraCoachSeen()
    }

    fun getCaptureTimerSeconds(): Flow<Int> = cameraSettingsRepository.getCaptureTimerSeconds()

    suspend fun setCaptureTimerSeconds(seconds: Int) {
        cameraSettingsRepository.setCaptureTimerSeconds(seconds)
    }
}
