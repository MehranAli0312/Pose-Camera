package com.aipose.camera.posematch.domain.models

enum class ProPlan(
    val basePlanId: String,
    val billingMonths: Int,
) {
    YEARLY(basePlanId = "yearly", billingMonths = 12),
    MONTHLY(basePlanId = "monthly", billingMonths = 1);

    val productId: String get() = PRODUCT_ID

    companion object {
        const val PRODUCT_ID = "premium"

        val subscriptionProductIds: List<String> = listOf(PRODUCT_ID)

        val recommended: ProPlan = YEARLY
    }
}
