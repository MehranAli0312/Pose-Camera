package com.aipose.camera.posematch.domain.models

data class PoseMatch(
    val score: Int = 0,
    val userSkeleton: List<SkeletonPoint> = emptyList(),
    val referenceSkeleton: List<SkeletonPoint> = emptyList()
)
