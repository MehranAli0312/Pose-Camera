package com.aipose.camera.posematch.domain.models

data class Capture(
    val id: Long = 0,
    val imagePath: String,
    val capturedAtMillis: Long,
    val poseId: Int?,
    val matchScore: Int,
    val category: String,
    val title: String,
    val isFavorite: Boolean = false,
    val location: CaptureLocation
)
