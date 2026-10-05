package com.aipose.camera.posematch.domain.models

data class StreakGoal(
    val current: Int,
    val target: Int
) {
    val remainingDays: Int get() = (target - current).coerceAtLeast(0)
}
