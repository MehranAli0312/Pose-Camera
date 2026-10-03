package com.aipose.camera.posematch.domain.models

data class CaptureDraft(
    val imagePath: String,
    val poseId: Int?,
    val poseTitle: String,
    val category: String,
    val matchScore: Int,
    val referenceImagePath: String?
)
