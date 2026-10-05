package com.aipose.camera.posematch.ui.screens.pro.models

import com.aipose.camera.posematch.domain.models.PremiumPlan

sealed interface ProPlansState {

    data object Loading : ProPlansState

    data class Content(
        val plans: List<PremiumPlan>,
        val selectedPlan: PremiumPlan,
        val recommendedPlan: PremiumPlan?,
        val yearlySavePercent: Int?,
        val isPurchasing: Boolean,
    ) : ProPlansState {
        val startsWithTrial: Boolean get() = selectedPlan.hasTrial
    }

    data object Unavailable : ProPlansState
}
