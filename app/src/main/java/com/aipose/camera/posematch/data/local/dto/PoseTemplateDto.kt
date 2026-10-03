package com.aipose.camera.posematch.data.local.dto

import kotlinx.serialization.Serializable

@Serializable
data class PoseTemplateDto(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val difficulty: String,
    val tags: List<String> = emptyList(),
    val image: String,
    val landmarks: Map<String, Float> = emptyMap()
)
