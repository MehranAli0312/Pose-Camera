package com.aipose.camera.posematch.ui.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AccessBenefit(
    @param:DrawableRes @get:DrawableRes val iconRes: Int,
    val palette: Icon3DPalette,
    @param:StringRes @get:StringRes val titleRes: Int,
    @param:StringRes @get:StringRes val bodyRes: Int? = null,
    @param:StringRes @get:StringRes val tagRes: Int? = null,
)
