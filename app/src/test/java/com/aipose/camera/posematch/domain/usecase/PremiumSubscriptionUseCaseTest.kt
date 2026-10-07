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
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumSubscriptionUseCaseTest {

    private val lifetime = PremiumPlan(ProPlan.LIFETIME, null, null, "Rs 4,500", 0)
    private val yearly = PremiumPlan(ProPlan.YEARLY, "yearly", null, "Rs 2,900", 0)

    @Test
    fun showsBothPlansWithLifetimeSelectedAndRecommended() = runBlocking {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(listOf(yearly, lifetime)))

        val plans = useCase.loadPlans(includeYearly = true).getOrThrow()

        assertEquals(listOf(lifetime, yearly), plans)
        assertEquals(lifetime, useCase.defaultSelection(plans))
        assertTrue(useCase.isRecommended(lifetime, plans))
        assertFalse(useCase.isRecommended(yearly, plans))
    }

    @Test
    fun hidesYearlyWhenRemoteConfigTurnsItOff() = runBlocking {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(listOf(yearly, lifetime)))

        val plans = useCase.loadPlans(includeYearly = false).getOrThrow()

        assertEquals(listOf(lifetime), plans)
        assertFalse(useCase.isRecommended(lifetime, plans))
    }

    @Test
    fun selectsYearlyWhenLifetimeIsMissing() = runBlocking {
        val useCase = PremiumSubscriptionUseCase(FakeRepository(listOf(yearly)))

        val plans = useCase.loadPlans(includeYearly = true).getOrThrow()

        assertEquals(yearly, useCase.defaultSelection(plans))
    }

    private class FakeRepository(private val plans: List<PremiumPlan>) : PremiumRepository {
        override val purchaseOutcomes: Flow<PremiumPurchaseOutcome> = emptyFlow()
        override fun observeFreeUser(): Flow<Boolean> = flowOf(true)
        override suspend fun loadPlans(): Result<List<PremiumPlan>> = Result.success(plans)
        override suspend fun restorePurchases(): ProRestoreResult = ProRestoreResult.NothingFound
    }
}
