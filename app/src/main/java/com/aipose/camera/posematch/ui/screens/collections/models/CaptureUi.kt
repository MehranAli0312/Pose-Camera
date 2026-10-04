package com.aipose.camera.posematch.ui.screens.collections.models

import com.aipose.camera.posematch.domain.models.Capture

data class CaptureUi(
    val capture: Capture,
    val formattedDate: String,
    val capturedLabel: String,
    val locationLabel: String,
    val titleLabel: String
) {
    val id: Long get() = capture.id

    fun matches(query: String): Boolean {
        val normalized = query.trim()
        if (normalized.isEmpty()) return true
        return titleLabel.contains(normalized, ignoreCase = true) ||
            capture.category.contains(normalized, ignoreCase = true) ||
            locationLabel.contains(normalized, ignoreCase = true) ||
            capture.matchScore.toString().contains(normalized) ||
            formattedDate.contains(normalized, ignoreCase = true)
    }
}
