package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.ads.ProStatusRefresher
import com.aipose.camera.posematch.ads.ProStatusStore
import com.aipose.camera.posematch.data.local.mapper.toPremiumPlans
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.PremiumPurchaseOutcome
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.domain.repo.PremiumRepository
import com.pdfutility.billing.BillingManager
import com.pdfutility.billing.presentation.states.BillingState
import com.pdfutility.billing.presentation.states.PurchaseResult
import com.pdfutility.billing.presentation.states.QueryResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val ConnectionOutcomes = setOf(
    BillingState.CONNECTED,
    BillingState.ALREADY_CONNECTED,
    BillingState.CONNECT_FAILED,
    BillingState.DISCONNECTED,
)

class PremiumRepositoryImpl(
    private val billingManager: BillingManager,
    private val proStatusRefresher: ProStatusRefresher,
    private val proStatusStore: ProStatusStore,
) : PremiumRepository {

    override val purchaseOutcomes: Flow<PremiumPurchaseOutcome> =
        billingManager.purchaseResults.map { it.toOutcome() }

    override fun observeFreeUser(): Flow<Boolean> =
        proStatusStore.status.map { it.isEligibleForAds }

    override suspend fun loadPlans(): Result<List<PremiumPlan>> {
        registerCatalog()
        if (!awaitConnection()) {
            return Result.failure(IllegalStateException(BillingState.CONNECT_FAILED.message))
        }
        return when (val response = billingManager.queryProducts()) {
            is QueryResponse.Success -> Result.success(response.data.toPremiumPlans())
            is QueryResponse.Error -> Result.failure(IllegalStateException(response.errorMessage))
            QueryResponse.Loading -> Result.success(emptyList())
        }
    }

    override suspend fun restorePurchases(): ProRestoreResult {
        if (!awaitConnection()) return ProRestoreResult.Failed
        return proStatusRefresher.refresh()
    }

    private fun registerCatalog() {
        if (billingManager.subscriptionIds != ProPlan.subscriptionProductIds) {
            billingManager.setSubscriptions(ProPlan.subscriptionProductIds)
        }
        if (billingManager.nonConsumableIds != ProPlan.oneTimeProductIds) {
            billingManager.setNonConsumables(ProPlan.oneTimeProductIds)
        }
    }

    private suspend fun awaitConnection(): Boolean {
        if (billingManager.isBillingConnected) return true
        billingManager.startConnection()
        val outcome = billingManager.connectionState.first { it in ConnectionOutcomes }
        return outcome == BillingState.CONNECTED || outcome == BillingState.ALREADY_CONNECTED
    }
}

private fun PurchaseResult.toOutcome(): PremiumPurchaseOutcome = when (this) {
    is PurchaseResult.Success -> PremiumPurchaseOutcome.Purchased
    PurchaseResult.AlreadyOwned -> PremiumPurchaseOutcome.AlreadyOwned
    PurchaseResult.Pending -> PremiumPurchaseOutcome.Pending
    PurchaseResult.Cancelled -> PremiumPurchaseOutcome.Cancelled
    is PurchaseResult.Error -> PremiumPurchaseOutcome.Failed(message)
}
