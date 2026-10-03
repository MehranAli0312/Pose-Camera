package com.aipose.camera.posematch.domain.models

data class Pose(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val difficulty: String,
    val tags: List<String>,
    val imagePath: String,
    val source: PoseSource,
    val landmarks: Map<PoseJoint, NormalizedPoint>
)
