package com.aipose.camera.posematch.domain.models

data class CaptureProgress(
    val averageMatch: Int,
    val shotsTaken: Int,
    val perfectShots: Int,
    val dayStreak: Int
) {
    companion object {
        val Empty = CaptureProgress(
            averageMatch = 0,
            shotsTaken = 0,
            perfectShots = 0,
            dayStreak = 0
        )
    }
}
