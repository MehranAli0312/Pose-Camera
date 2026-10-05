package com.aipose.camera.posematch.domain.models

data class PremiumPlan(
    val plan: ProPlan,
    val offerId: String?,
    val formattedPrice: String,
    val priceMicros: Long,
    val currencyCode: String,
    val trialDays: Int,
) {
    val productId: String get() = plan.productId

    val basePlanId: String get() = plan.basePlanId

    val hasTrial: Boolean get() = trialDays > 0

    val monthlyPriceMicros: Long get() = priceMicros / plan.billingMonths
}
