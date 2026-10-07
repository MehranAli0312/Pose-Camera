package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.repo.PoseLockRepository
import kotlinx.coroutines.flow.Flow

class PoseLockRepositoryImpl(
    private val appDataStore: AppDataStore,
) : PoseLockRepository {

    override fun observeUnlockedPoses(): Flow<Set<Int>> = appDataStore.getUnlockedPoses()

    override suspend fun unlock(poseId: Int) {
        appDataStore.unlockPose(poseId)
    }
}
