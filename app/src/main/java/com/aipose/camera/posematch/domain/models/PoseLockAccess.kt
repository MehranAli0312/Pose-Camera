package com.aipose.camera.posematch.domain.models

data class PoseLockAccess(
    val isLockingEnabled: Boolean = false,
    val unlockedPoseIds: Set<Int> = emptySet(),
) {

    fun lockedIdsIn(poses: List<Pose>): Set<Int> = if (isLockingEnabled) {
        LockedPoseCatalog.lockedIds(poses) - unlockedPoseIds
    } else {
        emptySet()
    }
}
