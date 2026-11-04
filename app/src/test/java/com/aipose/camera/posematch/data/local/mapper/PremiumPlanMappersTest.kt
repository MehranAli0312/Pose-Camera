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
        assertEquals(listOf("yearly_pro", "weekly_plan"), ProPlan.subscriptionProductIds)
        assertEquals(listOf("lifetime_pro"), ProPlan.oneTimeProductIds)
        assertEquals("yearly_pro", ProPlan.YEARLY.productId)
        assertEquals("weekly_plan", ProPlan.WEEKLY.productId)
        assertEquals("lifetime_pro", ProPlan.LIFETIME.productId)
    }

    @Test
    fun mapsLifetimeYearlyAndWeeklyWithPriceDetails() {
        val plans = listOf(
            lifetime("Rs 4,500", micros = 4_500_000_000L),
            subscriptionBasePlan("yearly-pro", "Rs 2,900", micros = 2_900_000_000L),
            subscriptionBasePlan(
                "weekly-pro-plan",
                "Rs 300",
                micros = 300_000_000L,
                billingPeriod = "P1W",
                productId = "weekly_plan",
            ),
        ).toPremiumPlans()

        assertEquals(listOf(ProPlan.YEARLY, ProPlan.WEEKLY, ProPlan.LIFETIME), plans.map { it.plan })

        val lifetime = plans.single { it.plan == ProPlan.LIFETIME }
        assertEquals("lifetime_pro", lifetime.productId)
        assertNull(lifetime.basePlanId)
        assertNull(lifetime.offerId)
        assertTrue(lifetime.isOneTime)
        assertEquals("Rs 4,500", lifetime.formattedPrice)
        assertEquals(4_500_000_000L, lifetime.priceMicros)

        val yearly = plans.single { it.plan == ProPlan.YEARLY }
        assertEquals("yearly_pro", yearly.productId)
        assertEquals("yearly-pro", yearly.basePlanId)
        assertNull(yearly.offerId)
        assertEquals("Rs 2,900", yearly.formattedPrice)
        assertEquals(2_900_000_000L, yearly.priceMicros)
        assertEquals("PKR", yearly.currencyCode)
        assertEquals(0, yearly.trialDays)

        val weekly = plans.single { it.plan == ProPlan.WEEKLY }
        assertEquals("weekly_plan", weekly.productId)
        assertEquals("weekly-pro-plan", weekly.basePlanId)
        assertEquals("Rs 300", weekly.formattedPrice)
        assertEquals(300_000_000L, weekly.priceMicros)
    }

    @Test
    fun prefersFreeTrialOfferOnYearlyBasePlan() {
        val plans = listOf(
            subscriptionBasePlan("yearly", "Rs 2,900"),
            yearlyTrialOffer("yearly", "free-trial", trialDays = 3, price = "Rs 2,900"),
        ).toPremiumPlans()

        val yearly = plans.single()
        assertEquals("yearly", yearly.basePlanId)
        assertEquals("free-trial", yearly.offerId)
        assertEquals(3, yearly.trialDays)
        assertEquals("Rs 2,900", yearly.formattedPrice)
        assertEquals(1_000_000L, yearly.priceMicros)
    }

    @Test
    fun prefersBasePlanMatchingThePlanBillingPeriod() {
        val plans = listOf(
            subscriptionBasePlan("monthly", "Rs 500", billingPeriod = "P1M"),
            subscriptionBasePlan("yearly", "Rs 2,900"),
            subscriptionBasePlan("weekly-monthly", "Rs 900", billingPeriod = "P1M", productId = "weekly_plan"),
            subscriptionBasePlan("weekly-pro-plan", "Rs 300", billingPeriod = "P1W", productId = "weekly_plan"),
        ).toPremiumPlans()

        assertEquals("yearly", plans.single { it.plan == ProPlan.YEARLY }.basePlanId)
        assertEquals("weekly-pro-plan", plans.single { it.plan == ProPlan.WEEKLY }.basePlanId)
    }

    @Test
    fun ignoresUnknownProductsAndMissingPrices() {
        val plans = listOf(
            lifetime("", productId = "lifetime_pro"),
            subscriptionBasePlan("yearly", "Rs 2,900", productId = "premium"),
            lifetime("Rs 4,500", productId = "premium_lifetime"),
        ).toPremiumPlans()

        assertTrue(plans.isEmpty())
    }

    private fun lifetime(
        price: String,
        micros: Long = 1_000_000L,
        productId: String = "lifetime_pro",
    ) = ProductDetail(
        productId = productId,
        planId = "",
        productTitle = "Lifetime Pro",
        productType = ProductType.inapp,
        pricingDetails = listOf(phase(RecurringMode.ORIGINAL, price, billingPeriod = "", micros = micros)),
    )

    private fun subscriptionBasePlan(
        basePlanId: String,
        price: String,
        micros: Long = 1_000_000L,
        billingPeriod: String = "P1Y",
        productId: String = "yearly_pro",
    ) = ProductDetail(
        productId = productId,
        planId = basePlanId,
        productTitle = "Pro",
        productType = ProductType.subs,
        pricingDetails = listOf(phase(RecurringMode.ORIGINAL, price, billingPeriod, micros = micros)),
    )

    private fun yearlyTrialOffer(basePlanId: String, offerId: String, trialDays: Int, price: String) =
        ProductDetail(
            productId = "yearly_pro",
            planId = basePlanId,
            productTitle = "Yearly Pro",
            productType = ProductType.subs,
            pricingDetails = listOf(
                phase(RecurringMode.FREE, "Free", "P${trialDays}D", trialDays = trialDays, micros = 0L),
                phase(RecurringMode.ORIGINAL, price, "P1Y"),
            ),
            offerId = offerId,
        )

    private fun phase(
        mode: RecurringMode,
        price: String,
        billingPeriod: String,
        trialDays: Int = 0,
        micros: Long = 1_000_000L,
    ) = PricingPhase(
        recurringMode = mode,
        price = price,
        currencyCode = "PKR",
        planTitle = "",
        billingCycleCount = 0,
        billingPeriod = billingPeriod,
        priceAmountMicros = micros,
        freeTrialPeriod = trialDays,
    )
}
