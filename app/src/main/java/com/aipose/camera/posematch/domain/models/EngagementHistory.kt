package com.aipose.camera.posematch.domain.models

data class EngagementHistory(
    val lastAppOpenAtMillis: Long = 0L,
    val lastSentAtMillis: Long = 0L,
    val unopenedInARow: Int = 0,
)
