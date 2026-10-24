package com.aipose.camera.posematch.domain.engagement

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.EngagementHistory
import com.aipose.camera.posematch.domain.models.EngagementNotification
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.time.localDayIndex
import java.util.Calendar
import kotlin.random.Random

class EngagementNotificationPicker(private val random: Random = Random.Default) {

    fun pick(
        captures: List<Capture>,
        freePoses: List<Pose>,
        history: EngagementHistory,
        currentStreak: Int,
        nowMillis: Long,
    ): EngagementNotification? {
        val today = localDayIndex(nowMillis)
        if (history.wasSentOn(today) || history.isBackingOff(today)) return null
        val daysAway = history.daysAway(today)
        if (daysAway > LONG_ABSENCE_DAYS) return memoryOf(captures, nowMillis)
        return streakSaverOf(captures, currentStreak, today)
            ?: memoryOf(captures, nowMillis)
            ?: comebackOf(captures, freePoses, daysAway)
    }

    fun preferredHour(captures: List<Capture>): Int {
        if (captures.size < MIN_CAPTURES_FOR_HABIT) return DEFAULT_HOUR
        return captures
            .groupingBy { capture -> hourOf(capture.capturedAtMillis) }
            .eachCount()
            .maxBy { (_, count) -> count }
            .key
            .coerceIn(EARLIEST_HOUR, LATEST_HOUR)
    }

    private fun streakSaverOf(
        captures: List<Capture>,
        currentStreak: Int,
        today: Long,
    ): EngagementNotification? {
        val capturedToday = captures.any { localDayIndex(it.capturedAtMillis) == today }
        if (capturedToday || currentStreak < MIN_STREAK_TO_SAVE) return null
        return EngagementNotification.StreakSaver(currentStreak)
    }

    private fun memoryOf(captures: List<Capture>, nowMillis: Long): EngagementNotification? {
        val today = calendarAt(nowMillis)
        return capturesYearsAgo(captures, today)
            .ifEmpty { capturesMonthAgo(captures, today) }
            .ifEmpty { capturesWeekAgo(captures, nowMillis) }
            .maxWithOrNull(compareBy<Capture>({ it.isFavorite }, { it.matchScore }))
            ?.let(EngagementNotification::Memory)
    }

    private fun capturesYearsAgo(captures: List<Capture>, today: Calendar): List<Capture> =
        captures.filter { capture ->
            val day = calendarAt(capture.capturedAtMillis)
            day.get(Calendar.YEAR) < today.get(Calendar.YEAR) &&
                day.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                day.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
        }

    private fun capturesMonthAgo(captures: List<Capture>, today: Calendar): List<Capture> {
        val monthAgo = (today.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        if (monthAgo.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH)) return emptyList()
        val monthAgoDay = localDayIndex(monthAgo.timeInMillis)
        return captures.filter { localDayIndex(it.capturedAtMillis) == monthAgoDay }
    }

    private fun capturesWeekAgo(captures: List<Capture>, nowMillis: Long): List<Capture> {
        val weekAgoDay = localDayIndex(nowMillis) - DAYS_PER_WEEK
        return captures.filter { localDayIndex(it.capturedAtMillis) == weekAgoDay }
    }

    private fun comebackOf(
        captures: List<Capture>,
        freePoses: List<Pose>,
        daysAway: Long,
    ): EngagementNotification? {
        if (daysAway !in COMEBACK_DAYS) return null
        val pose = freePoses.randomOrNull(random)
        return if (captures.isEmpty()) {
            EngagementNotification.FirstPhoto(pose)
        } else {
            pose?.let(EngagementNotification::PoseIdea)
        }
    }

    private fun EngagementHistory.wasSentOn(day: Long): Boolean =
        lastSentAtMillis > 0L && localDayIndex(lastSentAtMillis) == day

    private fun EngagementHistory.isBackingOff(today: Long): Boolean =
        unopenedInARow >= BACK_OFF_AFTER_UNOPENED && wasSentOn(today - 1)

    private fun EngagementHistory.daysAway(today: Long): Long =
        if (lastAppOpenAtMillis > 0L) today - localDayIndex(lastAppOpenAtMillis) else 0L

    private fun hourOf(millis: Long): Int = calendarAt(millis).get(Calendar.HOUR_OF_DAY)

    private fun calendarAt(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }

    private companion object {
        const val DEFAULT_HOUR = 19
        const val EARLIEST_HOUR = 9
        const val LATEST_HOUR = 21
        const val MIN_CAPTURES_FOR_HABIT = 3
        const val MIN_STREAK_TO_SAVE = 2
        const val BACK_OFF_AFTER_UNOPENED = 3
        const val LONG_ABSENCE_DAYS = 30L
        const val DAYS_PER_WEEK = 7L
        val COMEBACK_DAYS = setOf(1L, 2L, 3L, 5L, 7L, 10L, 14L, 21L, 30L)
    }
}
