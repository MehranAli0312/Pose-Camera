package com.aipose.camera.posematch.ui.screens.onboard.data

import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardStage
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardStep
import com.aipose.camera.posematch.ui.theme.PoseCyan
import com.aipose.camera.posematch.ui.theme.PosePink

internal const val ONBOARD_POSE_COUNT = 139
internal const val ONBOARD_EXTRA_CATEGORY_COUNT = 7
internal const val ONBOARD_OVERLAY_PERCENT = 45
internal const val ONBOARD_LIVE_MATCH_PERCENT = 87
internal const val ONBOARD_SAVED_MATCH_PERCENT = 92

internal val onboardSteps = listOf(
    OnboardStep(
        stage = OnboardStage.PickPose,
        titleRes = R.string.onboard_title_1,
        descriptionRes = R.string.onboard_desc_1,
        ctaRes = R.string.onboard_next,
        ambientAccent = PosePink,
        ambientAccentAlpha = 0.20f,
    ),
    OnboardStep(
        stage = OnboardStage.MatchOutline,
        titleRes = R.string.onboard_title_2,
        descriptionRes = R.string.onboard_desc_2,
        ctaRes = R.string.onboard_next,
        ambientAccent = PoseCyan,
        ambientAccentAlpha = 0.20f,
    ),
    OnboardStep(
        stage = OnboardStage.SnapSave,
        titleRes = R.string.onboard_title_3,
        descriptionRes = R.string.onboard_desc_3,
        ctaRes = R.string.onboard_lets_go,
        ambientAccent = PosePink,
        ambientAccentAlpha = 0.22f,
    ),
)
