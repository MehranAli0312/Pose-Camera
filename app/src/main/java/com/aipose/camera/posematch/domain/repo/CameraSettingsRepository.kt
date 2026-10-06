package com.aipose.camera.posematch.domain.repo

import kotlinx.coroutines.flow.Flow

interface CameraSettingsRepository {

    fun getKeepPoseOverlay(): Flow<Boolean>

    suspend fun setKeepPoseOverlay(keepOverlay: Boolean)

    fun getCaptureTimerSeconds(): Flow<Int>

    suspend fun setCaptureTimerSeconds(seconds: Int)
}
