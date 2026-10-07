package com.aipose.camera.posematch.domain.models

data class PremiumPlan(
    val plan: ProPlan,
    val basePlanId: String?,
    val offerId: String?,
    val formattedPrice: String,
    val trialDays: Int,
) {
    val productId: String get() = plan.productId

    val isOneTime: Boolean get() = plan.isOneTime

    val hasTrial: Boolean get() = trialDays > 0
}
