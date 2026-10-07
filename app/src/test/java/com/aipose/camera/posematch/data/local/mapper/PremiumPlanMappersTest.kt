package com.aipose.camera.posematch.data.local.mapper

import com.aipose.camera.posematch.domain.models.ProPlan
import com.pdfutility.billing.data.entities.product.PricingPhase
import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.data.entities.product.RecurringMode
import com.pdfutility.billing.presentation.enums.ProductType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumPlanMappersTest {

    @Test
    fun catalogUsesPlayConsoleProductIds() {
        assertEquals(listOf("yearly_pro"), ProPlan.subscriptionProductIds)
        assertEquals(listOf("lifetime_pro"), ProPlan.oneTimeProductIds)
        assertEquals("yearly_pro", ProPlan.YEARLY.productId)
        assertEquals("lifetime_pro", ProPlan.LIFETIME.productId)
    }

    @Test
    fun mapsLifetimeAndYearlyWithAnyBasePlanId() {
        val plans = listOf(lifetime("Rs 4,500"), yearlyBasePlan("yearly-pro", "Rs 2,900")).toPremiumPlans()

        val lifetime = plans.single { it.plan == ProPlan.LIFETIME }
        assertEquals("lifetime_pro", lifetime.productId)
        assertNull(lifetime.basePlanId)
        assertNull(lifetime.offerId)
        assertTrue(lifetime.isOneTime)
        assertEquals("Rs 4,500", lifetime.formattedPrice)

        val yearly = plans.single { it.plan == ProPlan.YEARLY }
        assertEquals("yearly_pro", yearly.productId)
        assertEquals("yearly-pro", yearly.basePlanId)
        assertNull(yearly.offerId)
        assertEquals("Rs 2,900", yearly.formattedPrice)
        assertEquals(0, yearly.trialDays)
    }

    @Test
    fun prefersFreeTrialOfferOnYearlyBasePlan() {
        val plans = listOf(
            yearlyBasePlan("yearly", "Rs 2,900"),
            yearlyTrialOffer("yearly", "free-trial", trialDays = 3, price = "Rs 2,900"),
        ).toPremiumPlans()

        val yearly = plans.single()
        assertEquals("yearly", yearly.basePlanId)
        assertEquals("free-trial", yearly.offerId)
        assertEquals(3, yearly.trialDays)
        assertEquals("Rs 2,900", yearly.formattedPrice)
    }

    @Test
    fun prefersYearlyBasePlanWhenSeveralExist() {
        val plans = listOf(
            yearlyBasePlan("monthly", "Rs 500", billingPeriod = "P1M"),
            yearlyBasePlan("yearly", "Rs 2,900"),
        ).toPremiumPlans()

        assertEquals("yearly", plans.single().basePlanId)
        assertEquals("Rs 2,900", plans.single().formattedPrice)
    }

    @Test
    fun ignoresUnknownProductsAndMissingPrices() {
        val plans = listOf(
            lifetime("", productId = "lifetime_pro"),
            yearlyBasePlan("yearly", "Rs 2,900", productId = "premium"),
            lifetime("Rs 4,500", productId = "premium_lifetime"),
        ).toPremiumPlans()

        assertTrue(plans.isEmpty())
    }

    private fun lifetime(price: String, productId: String = "lifetime_pro") = ProductDetail(
        productId = productId,
        planId = "",
        productTitle = "Lifetime Pro",
        productType = ProductType.inapp,
        pricingDetails = listOf(phase(RecurringMode.ORIGINAL, price, billingPeriod = "")),
    )

    private fun yearlyBasePlan(
        basePlanId: String,
        price: String,
        billingPeriod: String = "P1Y",
        productId: String = "yearly_pro",
    ) = ProductDetail(
        productId = productId,
        planId = basePlanId,
        productTitle = "Yearly Pro",
        productType = ProductType.subs,
        pricingDetails = listOf(phase(RecurringMode.ORIGINAL, price, billingPeriod)),
    )

    private fun yearlyTrialOffer(basePlanId: String, offerId: String, trialDays: Int, price: String) =
        ProductDetail(
            productId = "yearly_pro",
            planId = basePlanId,
            productTitle = "Yearly Pro",
            productType = ProductType.subs,
            pricingDetails = listOf(
                phase(RecurringMode.FREE, "Free", "P${trialDays}D", trialDays = trialDays),
                phase(RecurringMode.ORIGINAL, price, "P1Y"),
            ),
            offerId = offerId,
        )

    private fun phase(mode: RecurringMode, price: String, billingPeriod: String, trialDays: Int = 0) =
        PricingPhase(
            recurringMode = mode,
            price = price,
            currencyCode = "PKR",
            planTitle = "",
            billingCycleCount = 0,
            billingPeriod = billingPeriod,
            priceAmountMicros = if (mode == RecurringMode.FREE) 0L else 1_000_000L,
            freeTrialPeriod = trialDays,
        )
}
