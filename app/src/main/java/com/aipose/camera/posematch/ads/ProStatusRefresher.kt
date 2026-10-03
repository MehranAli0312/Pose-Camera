package com.aipose.camera.posematch.ads

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.models.ProEntitlement
import com.pdfutility.billing.BillingManager
import com.pdfutility.billing.data.entities.purchase.PurchaseDetail
import com.pdfutility.billing.presentation.enums.ProductType
import com.pdfutility.billing.presentation.states.BillingState
import com.pdfutility.billing.presentation.states.QueryResponse
import kotlinx.coroutines.flow.first

class ProStatusRefresher(
    private val billingManager: BillingManager,
    private val proStatusStore: ProStatusStore,
    private val appDataStore: AppDataStore,
) {

    suspend fun applySavedStatus() {
        if (hasUsableSavedStatus()) proStatusStore.resolve(isPro = true)
    }

    suspend fun applySavedStatusOrFree() {
        if (hasUsableSavedStatus()) {
            proStatusStore.resolve(isPro = true)
        } else {
            proStatusStore.resolveUnknownAsFree()
        }
    }

    suspend fun connectAndRefresh() {
        if (!billingManager.isBillingConnected) {
            billingManager.startConnection()
            val connected = billingManager.connectionState.first { it in connectionOutcomes } ==
                BillingState.CONNECTED
            if (!connected) {
                applySavedStatusOrFree()
                return
            }
        }
        refresh()
    }

    suspend fun refresh(): ProRestoreResult =
        when (val response = billingManager.queryPurchases()) {
            is QueryResponse.Success -> {
                val entitlement = response.data.toEntitlement()
                proStatusStore.resolve(entitlement.isPro)
                appDataStore.setProEntitlement(entitlement)
                if (entitlement.isPro) ProRestoreResult.Restored else ProRestoreResult.NothingFound
            }

            else -> {
                applySavedStatusOrFree()
                ProRestoreResult.Failed
            }
        }

    private suspend fun hasUsableSavedStatus(): Boolean =
        appDataStore.getProEntitlement().isUsableOffline(System.currentTimeMillis())

    private val connectionOutcomes = setOf(
        BillingState.CONNECTED,
        BillingState.CONNECT_FAILED,
        BillingState.DISCONNECTED,
    )

    private fun List<PurchaseDetail>.toEntitlement(): ProEntitlement {
        val owned = filter { it.isPurchased }
        return ProEntitlement(
            isPro = owned.isNotEmpty(),
            isLifetime = owned.any { it.productType == ProductType.inapp },
            verifiedAtMillis = System.currentTimeMillis(),
        )
    }
}
