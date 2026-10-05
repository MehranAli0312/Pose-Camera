package com.aipose.camera.posematch.domain.models

data class ProgressStats(
    val averageMatch: Int,
    val averageDelta: Int?,
    val trend: List<TrendBucket>,
    val topCategories: List<CategoryScore>,
    val perfectShots: Int,
    val bestStreak: Int,
    val posesTried: Int
)
