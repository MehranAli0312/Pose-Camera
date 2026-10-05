package com.aipose.camera.posematch.domain.models

data class TrendBucket(
    val startMillis: Long,
    val averageMatch: Int,
    val shotCount: Int
)
