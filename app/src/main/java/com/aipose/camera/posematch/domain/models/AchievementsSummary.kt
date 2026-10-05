package com.aipose.camera.posematch.domain.models

data class AchievementsSummary(
    val currentStreak: Int,
    val bestStreak: Int,
    val week: List<StreakDay>,
    val badges: List<BadgeStatus>,
    val nextStreakGoal: StreakGoal?
) {
    val unlockedCount: Int get() = badges.count { it.isUnlocked }
}
