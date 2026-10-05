package com.aipose.camera.posematch.domain.repo

import kotlinx.coroutines.flow.Flow

interface CameraSettingsRepository {

    fun getRetainSkeleton(): Flow<Boolean>

    suspend fun setRetainSkeleton(retain: Boolean)

    fun isCameraCoachSeen(): Flow<Boolean>

    suspend fun markCameraCoachSeen()

    fun getCaptureTimerSeconds(): Flow<Int>

    suspend fun setCaptureTimerSeconds(seconds: Int)
}
