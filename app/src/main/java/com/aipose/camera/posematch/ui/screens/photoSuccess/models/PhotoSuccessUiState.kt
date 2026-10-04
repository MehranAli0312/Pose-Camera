package com.aipose.camera.posematch.ui.screens.photoSuccess.models

import com.aipose.camera.posematch.domain.models.Capture

data class PhotoSuccessUiState(
    val capture: Capture? = null,
    val isPersonalBest: Boolean = false
)
