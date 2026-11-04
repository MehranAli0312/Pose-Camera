package com.aipose.camera.posematch.ui.screens.pro.models

import com.aipose.camera.posematch.domain.models.PremiumPlan

data class ProPlanOption(
    val details: PremiumPlan,
    val savingsPercent: Int?,
)
