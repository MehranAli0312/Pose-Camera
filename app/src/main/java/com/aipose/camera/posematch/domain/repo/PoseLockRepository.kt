package com.aipose.camera.posematch.domain.repo

import kotlinx.coroutines.flow.Flow

interface PoseLockRepository {

    fun observeUnlockedPoses(): Flow<Set<Int>>

    suspend fun unlock(poseId: Int)
}
