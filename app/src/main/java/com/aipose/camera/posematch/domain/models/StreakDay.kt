package com.aipose.camera.posematch.domain.models

data class StreakDay(
    val dayMillis: Long,
    val hasCapture: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)
