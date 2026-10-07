package com.aipose.camera.posematch.domain.models

private const val YEARLY_PRODUCT_ID = "yearly_pro"
private const val LIFETIME_PRODUCT_ID = "lifetime_pro"

enum class ProPlan(val productId: String, val isOneTime: Boolean) {
    LIFETIME(productId = LIFETIME_PRODUCT_ID, isOneTime = true),
    YEARLY(productId = YEARLY_PRODUCT_ID, isOneTime = false);

    companion object {
        val subscriptionProductIds: List<String> = listOf(YEARLY_PRODUCT_ID)

        val oneTimeProductIds: List<String> = listOf(LIFETIME_PRODUCT_ID)

        val recommended: ProPlan = LIFETIME
    }
}
