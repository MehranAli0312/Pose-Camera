package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.ads.RewardedUnlockSession
import com.aipose.camera.posematch.domain.models.PoseLockAccess
import com.aipose.camera.posematch.domain.repo.PoseLockRepository
import com.aipose.camera.posematch.domain.repo.PremiumRepository
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.PremiumFeatureDialogMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class PoseLockUseCase(
    private val poseLockRepository: PoseLockRepository,
    private val premiumRepository: PremiumRepository,
    private val remoteConfigStore: AdsRemoteConfigStore,
    private val rewardedUnlockSession: RewardedUnlockSession,
) {

    fun observeAccess(): Flow<PoseLockAccess> = combine(
        remoteConfigStore.config,
        premiumRepository.observeFreeUser(),
        poseLockRepository.observeUnlockedPoses(),
    ) { config, isFreeUser, unlockedPoseIds ->
        PoseLockAccess(
            isLockingEnabled = isFreeUser &&
                config.premiumFeatureDialog == PremiumFeatureDialogMode.On,
            unlockedPoseIds = unlockedPoseIds,
        )
    }.distinctUntilChanged()

    suspend fun unlockWithRewardedAd(poseId: Int) {
        poseLockRepository.unlock(poseId)
        rewardedUnlockSession.onPoseUnlockedByRewardedAd(poseId)
    }
}
