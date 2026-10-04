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
