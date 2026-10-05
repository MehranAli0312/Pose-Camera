package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.AchievementBadge
import com.aipose.camera.posematch.domain.models.AchievementMetric
import com.aipose.camera.posematch.domain.models.AchievementsSummary
import com.aipose.camera.posematch.domain.models.BadgeStatus
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.domain.models.StreakDay
import com.aipose.camera.posematch.domain.models.StreakGoal
import com.aipose.camera.posematch.domain.time.localDayIndex
import java.util.Calendar
import java.util.Locale

private const val DAYS_PER_WEEK = 7

class AchievementsUseCase(private val captureProgressUseCase: CaptureProgressUseCase) {

    fun summaryOf(
        captures: List<Capture>,
        favoriteCount: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): AchievementsSummary {
        val currentStreak = captureProgressUseCase.currentStreak(captures, nowMillis)
        val bestStreak = captureProgressUseCase.bestStreak(captures)
        val metrics = mapOf(
            AchievementMetric.Shots to captures.size,
            AchievementMetric.PosesTried to captures.mapNotNull { it.poseId }.distinct().size,
            AchievementMetric.BestMatch to (captures.maxOfOrNull { it.matchScore } ?: 0),
            AchievementMetric.BestStreak to bestStreak,
            AchievementMetric.Favorites to favoriteCount + captures.count { it.isFavorite },
            AchievementMetric.Places to captures.distinctPlaces(),
            AchievementMetric.PerfectShots to captures.count { it.matchScore >= PERFECT_MATCH_SCORE }
        )
        val statuses = AchievementBadge.entries.map { badge ->
            BadgeStatus(badge, isUnlocked = (metrics[badge.metric] ?: 0) >= badge.target)
        }
        return AchievementsSummary(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            week = weekOf(captures, nowMillis),
            badges = statuses.sortedByDescending { it.isUnlocked },
            nextStreakGoal = statuses
                .filter { !it.isUnlocked && it.badge.metric == AchievementMetric.BestStreak }
                .map { it.badge.target }
                .filter { target -> target > currentStreak }
                .minOrNull()
                ?.let { target -> StreakGoal(current = currentStreak, target = target) }
        )
    }

    private fun weekOf(captures: List<Capture>, nowMillis: Long): List<StreakDay> {
        val capturedDays = captures.map { localDayIndex(it.capturedAtMillis) }.toSet()
        val today = localDayIndex(nowMillis)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            val offset = (get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + DAYS_PER_WEEK) % DAYS_PER_WEEK
            add(Calendar.DAY_OF_YEAR, -offset)
        }
        return List(DAYS_PER_WEEK) { index ->
            if (index > 0) calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayMillis = calendar.timeInMillis
            val day = localDayIndex(dayMillis)
            StreakDay(
                dayMillis = dayMillis,
                hasCapture = day in capturedDays,
                isToday = day == today,
                isFuture = day > today
            )
        }
    }

    private fun List<Capture>.distinctPlaces(): Int =
        mapNotNull { capture -> capture.location.name?.trim()?.takeIf { it.isNotEmpty() } }
            .map { it.lowercase(Locale.ROOT) }
            .distinct()
            .size
}
