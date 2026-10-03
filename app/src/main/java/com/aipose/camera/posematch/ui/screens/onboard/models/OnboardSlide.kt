package com.aipose.camera.posematch.ui.screens.onboard.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class OnboardSlide(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val imageRes: Int,
)
