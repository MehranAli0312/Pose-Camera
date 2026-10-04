package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.CaptureProgress
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.domain.time.localDayIndex

class CaptureProgressUseCase {

    fun progressOf(captures: List<Capture>): CaptureProgress {
        if (captures.isEmpty()) return CaptureProgress.Empty
        return CaptureProgress(
            averageMatch = captures.sumOf { it.matchScore } / captures.size,
            shotsTaken = captures.size,
            perfectShots = captures.count { it.matchScore >= PERFECT_MATCH_SCORE },
            dayStreak = captures.dayStreak()
        )
    }

    fun bestMatchFor(captures: List<Capture>, poseId: Int): Int? =
        captures.filter { it.poseId == poseId }.maxOfOrNull { it.matchScore }

    fun daysSince(capture: Capture, nowMillis: Long = System.currentTimeMillis()): Int =
        (localDayIndex(nowMillis) - localDayIndex(capture.capturedAtMillis)).toInt().coerceAtLeast(0)

    fun isWithinDays(capture: Capture, days: Int, nowMillis: Long = System.currentTimeMillis()): Boolean =
        daysSince(capture, nowMillis) < days

    fun perfectShotsWithinDays(captures: List<Capture>, days: Int): Int =
        captures.count { it.matchScore >= PERFECT_MATCH_SCORE && isWithinDays(it, days) }

    fun isPersonalBest(captures: List<Capture>, capture: Capture): Boolean {
        val poseId = capture.poseId ?: return false
        if (capture.matchScore <= 0) return false
        val previousBest = captures
            .filter { it.poseId == poseId && it.id != capture.id }
            .maxOfOrNull { it.matchScore }
            ?: return true
        return capture.matchScore > previousBest
    }

    private fun List<Capture>.dayStreak(): Int {
        val days = map { localDayIndex(it.capturedAtMillis) }.distinct().sortedDescending()
        if (days.isEmpty()) return 0
        if (days.first() < localDayIndex(System.currentTimeMillis()) - 1) return 0
        var streak = 1
        for (index in 1 until days.size) {
            if (days[index] != days[index - 1] - 1) break
            streak++
        }
        return streak
    }
}
