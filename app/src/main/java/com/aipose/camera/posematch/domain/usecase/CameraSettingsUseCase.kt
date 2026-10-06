package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.repo.CameraSettingsRepository
import kotlinx.coroutines.flow.Flow

class CameraSettingsUseCase(
    private val cameraSettingsRepository: CameraSettingsRepository
) {

    fun getKeepPoseOverlay(): Flow<Boolean> = cameraSettingsRepository.getKeepPoseOverlay()

    suspend fun setKeepPoseOverlay(keepOverlay: Boolean) {
        cameraSettingsRepository.setKeepPoseOverlay(keepOverlay)
    }

    fun getCaptureTimerSeconds(): Flow<Int> = cameraSettingsRepository.getCaptureTimerSeconds()

    suspend fun setCaptureTimerSeconds(seconds: Int) {
        cameraSettingsRepository.setCaptureTimerSeconds(seconds)
    }
}
