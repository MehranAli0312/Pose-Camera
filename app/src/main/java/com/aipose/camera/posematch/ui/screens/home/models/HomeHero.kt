package com.aipose.camera.posematch.ui.screens.home.models

import androidx.compose.runtime.Immutable
import com.aipose.camera.posematch.domain.models.Pose

@Immutable
data class HomeHero(
    val pose: Pose,
    val categoryCount: Int,
    val bestMatch: Int?
)
