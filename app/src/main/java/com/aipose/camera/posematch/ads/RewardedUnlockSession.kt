package com.aipose.camera.posematch.ads

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class RewardedUnlockSession {

    private val rewardedPoseIds: MutableSet<Int> =
        Collections.newSetFromMap(ConcurrentHashMap())

    fun onPoseUnlockedByRewardedAd(poseId: Int) {
        rewardedPoseIds.add(poseId)
    }

    fun hasWatchedRewardedAdFor(poseId: Int?): Boolean =
        poseId != null && poseId in rewardedPoseIds
}
