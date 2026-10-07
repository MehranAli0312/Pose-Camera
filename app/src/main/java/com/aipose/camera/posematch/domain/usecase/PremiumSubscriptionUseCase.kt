package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.PremiumPurchaseOutcome
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.domain.repo.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class PremiumSubscriptionUseCase(
    private val repository: PremiumRepository,
) {

    val purchaseOutcomes: Flow<PremiumPurchaseOutcome> = repository.purchaseOutcomes

    fun observeFreeUser(): Flow<Boolean> =
        repository.observeFreeUser().distinctUntilChanged()

    suspend fun loadPlans(includeYearly: Boolean): Result<List<PremiumPlan>> =
        repository.loadPlans().map { plans -> plans.forDisplay(includeYearly) }

    suspend fun restorePurchases(): ProRestoreResult = repository.restorePurchases()

    fun defaultSelection(plans: List<PremiumPlan>): PremiumPlan? =
        plans.firstOrNull { it.plan == ProPlan.recommended } ?: plans.firstOrNull()

    fun isRecommended(plan: PremiumPlan, plans: List<PremiumPlan>): Boolean =
        plans.size > 1 && plan.plan == ProPlan.recommended

    private fun List<PremiumPlan>.forDisplay(includeYearly: Boolean): List<PremiumPlan> =
        filter { includeYearly || it.plan != ProPlan.YEARLY }
            .distinctBy { it.plan }
            .sortedBy { it.plan.ordinal }
}
