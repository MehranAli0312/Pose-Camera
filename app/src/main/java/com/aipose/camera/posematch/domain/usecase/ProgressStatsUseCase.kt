package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CategoryScore
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.domain.models.ProgressPeriod
import com.aipose.camera.posematch.domain.models.ProgressStats
import com.aipose.camera.posematch.domain.models.TrendBucket
import com.aipose.camera.posematch.domain.time.localDayIndex
import java.util.Calendar

private const val MILLIS_PER_DAY = 86_400_000L
private const val WEEK_DAYS = 7
private const val MONTH_WEEKS = 4
private const val ALL_TIME_MONTHS = 6
private const val TOP_CATEGORY_COUNT = 4

class ProgressStatsUseCase(private val captureProgressUseCase: CaptureProgressUseCase) {

    fun statsFor(
        captures: List<Capture>,
        period: ProgressPeriod,
        nowMillis: Long = System.currentTimeMillis()
    ): ProgressStats {
        val today = localDayIndex(nowMillis)
        val windowDays = period.windowDays()
        val current = windowDays?.let { days -> captures.withinDays(today, 0, days) } ?: captures
        val previous = windowDays?.let { days -> captures.withinDays(today, days, days * 2) }
        val averageMatch = current.averageMatch()
        return ProgressStats(
            averageMatch = averageMatch,
            averageDelta = previous
                ?.takeIf { it.isNotEmpty() && current.isNotEmpty() }
                ?.let { averageMatch - it.averageMatch() },
            trend = trendFor(captures, period, today, nowMillis),
            topCategories = current.topCategories(),
            perfectShots = current.count { it.matchScore >= PERFECT_MATCH_SCORE },
            bestStreak = captureProgressUseCase.bestStreak(captures),
            posesTried = current.mapNotNull { it.poseId }.distinct().size
        )
    }

    private fun trendFor(
        captures: List<Capture>,
        period: ProgressPeriod,
        today: Long,
        nowMillis: Long
    ): List<TrendBucket> = when (period) {
        ProgressPeriod.Week -> (WEEK_DAYS - 1 downTo 0).map { daysAgo ->
            captures.withinDays(today, daysAgo, daysAgo + 1)
                .toBucket(nowMillis - daysAgo * MILLIS_PER_DAY)
        }
        ProgressPeriod.Month -> (MONTH_WEEKS - 1 downTo 0).map { weeksAgo ->
            val fromDaysAgo = weeksAgo * WEEK_DAYS
            captures.withinDays(today, fromDaysAgo, fromDaysAgo + WEEK_DAYS)
                .toBucket(nowMillis - (fromDaysAgo + WEEK_DAYS - 1) * MILLIS_PER_DAY)
        }
        ProgressPeriod.AllTime -> (ALL_TIME_MONTHS - 1 downTo 0).map { monthsAgo ->
            val monthStart = monthStartMillis(nowMillis, monthsAgo)
            val monthEnd = monthStartMillis(nowMillis, monthsAgo - 1)
            captures.filter { it.capturedAtMillis in monthStart until monthEnd }.toBucket(monthStart)
        }
    }

    private fun ProgressPeriod.windowDays(): Int? = when (this) {
        ProgressPeriod.Week -> WEEK_DAYS
        ProgressPeriod.Month -> WEEK_DAYS * MONTH_WEEKS
        ProgressPeriod.AllTime -> null
    }

    private fun List<Capture>.withinDays(today: Long, fromDaysAgo: Int, untilDaysAgo: Int): List<Capture> =
        filter { capture ->
            val daysAgo = today - localDayIndex(capture.capturedAtMillis)
            daysAgo >= fromDaysAgo && daysAgo < untilDaysAgo
        }

    private fun List<Capture>.averageMatch(): Int =
        if (isEmpty()) 0 else sumOf { it.matchScore } / size

    private fun List<Capture>.toBucket(startMillis: Long): TrendBucket =
        TrendBucket(startMillis = startMillis, averageMatch = averageMatch(), shotCount = size)

    private fun List<Capture>.topCategories(): List<CategoryScore> =
        filter { it.category.isNotBlank() }
            .groupBy { it.category }
            .map { (category, items) -> CategoryScore(category, items.averageMatch()) }
            .sortedByDescending { it.averageMatch }
            .take(TOP_CATEGORY_COUNT)

    private fun monthStartMillis(nowMillis: Long, monthsAgo: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, -monthsAgo)
        }.timeInMillis
}
