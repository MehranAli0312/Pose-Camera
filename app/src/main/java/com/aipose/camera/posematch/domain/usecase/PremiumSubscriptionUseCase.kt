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

    suspend fun loadPlans(): Result<List<PremiumPlan>> =
        repository.loadPlans().map { plans -> plans.forDisplay() }

    suspend fun restorePurchases(): ProRestoreResult = repository.restorePurchases()

    fun defaultSelection(plans: List<PremiumPlan>): PremiumPlan? =
        plans.firstOrNull { it.plan == ProPlan.recommended } ?: plans.firstOrNull()

    fun isRecommended(plan: PremiumPlan, plans: List<PremiumPlan>): Boolean =
        plans.size > 1 && plan.plan == ProPlan.recommended

    fun savingsPercent(plan: PremiumPlan, plans: List<PremiumPlan>): Int? {
        val weeks = plan.plan.weeksPerPeriod ?: return null
        val baseline = plans.shortestSubscription(plan.currencyCode) ?: return null
        val baselineWeeks = baseline.plan.weeksPerPeriod ?: return null
        if (baselineWeeks >= weeks || baseline.priceMicros <= 0) return null
        val undiscountedMicros = baseline.priceMicros * weeks / baselineWeeks
        val percent = (undiscountedMicros - plan.priceMicros) * PERCENT / undiscountedMicros
        return percent.toInt().takeIf { it >= MIN_SAVINGS_PERCENT }
    }

    private fun List<PremiumPlan>.shortestSubscription(currencyCode: String): PremiumPlan? =
        filter { it.plan.weeksPerPeriod != null && it.currencyCode == currencyCode }
            .minByOrNull { it.plan.weeksPerPeriod ?: Int.MAX_VALUE }

    private fun List<PremiumPlan>.forDisplay(): List<PremiumPlan> =
        distinctBy { it.plan }
            .sortedBy { it.plan.ordinal }

    private companion object {
        const val PERCENT = 100L
        const val MIN_SAVINGS_PERCENT = 5
    }
}
