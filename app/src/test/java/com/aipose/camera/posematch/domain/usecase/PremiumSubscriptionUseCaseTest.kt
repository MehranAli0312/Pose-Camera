package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.PremiumPurchaseOutcome
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.domain.repo.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumSubscriptionUseCaseTest {

    private val lifetime = plan(ProPlan.LIFETIME, null, "Rs 4,500", 4_500_000_000L)
    private val yearly = plan(ProPlan.YEARLY, "yearly-pro", "Rs 2,900", 2_900_000_000L)
    private val weekly = plan(ProPlan.WEEKLY, "weekly-pro-plan", "Rs 300", 300_000_000L)

    @Test
    fun showsYearlyFirstSelectedAndRecommended() = runBlocking {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(listOf(lifetime, weekly, yearly)))

        val plans = useCase.loadPlans().getOrThrow()

        assertEquals(listOf(yearly, weekly, lifetime), plans)
        assertEquals(yearly, useCase.defaultSelection(plans))
        assertTrue(useCase.isRecommended(yearly, plans))
        assertFalse(useCase.isRecommended(weekly, plans))
        assertFalse(useCase.isRecommended(lifetime, plans))
    }

    @Test
    fun selectsFirstPlanWhenYearlyIsMissing() = runBlocking {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(listOf(lifetime, weekly)))

        val plans = useCase.loadPlans().getOrThrow()

        assertEquals(weekly, useCase.defaultSelection(plans))
    }

    @Test
    fun yearlySavingsAreMeasuredAgainstWeeklyAndRoundedDown() {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(emptyList()))
        val plans = listOf(yearly, weekly, lifetime)

        assertEquals(81, useCase.savingsPercent(yearly, plans))
        assertNull(useCase.savingsPercent(weekly, plans))
        assertNull(useCase.savingsPercent(lifetime, plans))
    }

    @Test
    fun noSavingsWithoutWeeklyPlanOrWithDifferentCurrencies() {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(emptyList()))
        val weeklyInUsd = weekly.copy(currencyCode = "USD")

        assertNull(useCase.savingsPercent(yearly, listOf(yearly, lifetime)))
        assertNull(useCase.savingsPercent(yearly, listOf(yearly, weeklyInUsd)))
    }

    @Test
    fun hidesSavingsBelowMinimumOrWhenYearlyCostsMore() {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(emptyList()))
        val almostFullYearly = yearly.copy(priceMicros = 15_000_000_000L)
        val overpricedYearly = yearly.copy(priceMicros = 20_000_000_000L)

        assertNull(useCase.savingsPercent(almostFullYearly, listOf(almostFullYearly, weekly)))
        assertNull(useCase.savingsPercent(overpricedYearly, listOf(overpricedYearly, weekly)))
    }

    private fun plan(plan: ProPlan, basePlanId: String?, price: String, micros: Long) = PremiumPlan(
        plan = plan,
        basePlanId = basePlanId,
        offerId = null,
        formattedPrice = price,
        priceMicros = micros,
        currencyCode = "PKR",
        trialDays = 0,
    )

    private class FakeRepository(private val plans: List<PremiumPlan>) : PremiumRepository {
        override val purchaseOutcomes: Flow<PremiumPurchaseOutcome> = emptyFlow()
        override fun observeFreeUser(): Flow<Boolean> = flowOf(true)
        override suspend fun loadPlans(): Result<List<PremiumPlan>> = Result.success(plans)
        override suspend fun restorePurchases(): ProRestoreResult = ProRestoreResult.NothingFound
    }
}
