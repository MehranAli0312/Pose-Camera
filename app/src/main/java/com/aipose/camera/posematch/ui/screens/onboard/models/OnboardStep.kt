package com.aipose.camera.posematch.ui.screens.onboard.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

internal enum class OnboardStage {
    PickPose,
    MatchOutline,
    SnapSave,
}

@Immutable
internal data class OnboardStep(
    val stage: OnboardStage,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val ctaRes: Int,
    val ambientAccent: Color,
    val ambientAccentAlpha: Float,
)
