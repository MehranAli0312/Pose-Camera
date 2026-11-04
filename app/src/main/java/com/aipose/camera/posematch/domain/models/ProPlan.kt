package com.aipose.camera.posematch.domain.models

private const val YEARLY_PRODUCT_ID = "yearly_pro"
private const val WEEKLY_PRODUCT_ID = "weekly_plan"
private const val LIFETIME_PRODUCT_ID = "lifetime_pro"
private const val YEARLY_BILLING_PERIOD = "P1Y"
private const val WEEKLY_BILLING_PERIOD = "P1W"
private const val WEEKS_PER_YEAR = 52
private const val WEEKS_PER_WEEK = 1

enum class ProPlan(
    val productId: String,
    val billingPeriod: String?,
    val weeksPerPeriod: Int?,
) {
    YEARLY(
        productId = YEARLY_PRODUCT_ID,
        billingPeriod = YEARLY_BILLING_PERIOD,
        weeksPerPeriod = WEEKS_PER_YEAR,
    ),
    WEEKLY(
        productId = WEEKLY_PRODUCT_ID,
        billingPeriod = WEEKLY_BILLING_PERIOD,
        weeksPerPeriod = WEEKS_PER_WEEK,
    ),
    LIFETIME(
        productId = LIFETIME_PRODUCT_ID,
        billingPeriod = null,
        weeksPerPeriod = null,
    );

    val isOneTime: Boolean get() = billingPeriod == null

    companion object {
        val subscriptionProductIds: List<String> =
            entries.filterNot { it.isOneTime }.map { it.productId }

        val oneTimeProductIds: List<String> =
            entries.filter { it.isOneTime }.map { it.productId }

        val recommended: ProPlan = YEARLY
    }
}
