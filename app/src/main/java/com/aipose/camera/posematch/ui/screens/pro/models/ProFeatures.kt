package com.aipose.camera.posematch.ui.screens.pro.models

import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.models.AccessBenefit
import com.aipose.camera.posematch.ui.models.Icon3DPalette

val proIncludedFeatures: List<AccessBenefit> = listOf(
    AccessBenefit(
        iconRes = R.drawable.ic_hero_glyph_cross,
        palette = Icon3DPalette.Rose,
        titleRes = R.string.pro_feature_remove_ads,
        bodyRes = R.string.pro_feature_remove_ads_desc,
    ),
)
