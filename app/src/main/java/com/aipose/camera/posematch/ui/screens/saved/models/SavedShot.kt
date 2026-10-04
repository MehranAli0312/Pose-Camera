package com.aipose.camera.posematch.ui.screens.saved.models

import androidx.compose.runtime.Immutable
import com.aipose.camera.posematch.domain.models.Capture

@Immutable
data class SavedShot(
    val capture: Capture,
    val locationLabel: String
)
