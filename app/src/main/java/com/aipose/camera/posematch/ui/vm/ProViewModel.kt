package com.aipose.camera.posematch.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfutility.billing.BillingManager
import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.presentation.states.BillingState
import com.pdfutility.billing.presentation.states.PurchaseResult
import com.pdfutility.billing.presentation.states.QueryResponse
import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.ads.ProStatusRefresher
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.PremiumCloseButtonPosition
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlanOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProUiState(
    val selectedPlan: ProPlan,
    val isLoadingProducts: Boolean = true,
    val products: List<ProductDetail> = emptyList(),
    val isPurchasing: Boolean = false,
    val purchaseResult: PurchaseResult? = null,
    val isRestoring: Boolean = false,
    val restoreResult: ProRestoreResult? = null,
    val closeSecondsRemaining: Int = 0,
    val closePosition: PremiumCloseButtonPosition = PremiumCloseButtonPosition.Right,
) {
    val planOptions: List<ProPlanOption>
        get() = ProPlan.entries.map { ProPlanOption.from(it, products) }

    val selectedOption: ProPlanOption
        get() = ProPlanOption.from(selectedPlan, products)

    val yearlySavePercent: Int?
        get() {
            val yearly = ProPlanOption.from(ProPlan.YEARLY, products)
            val monthly = ProPlanOption.from(ProPlan.MONTHLY, products)
            val yearlyMicros = yearly.priceMicros ?: return null
            val monthlyMicros = monthly.priceMicros ?: return null
            if (yearly.currencyCode != monthly.currencyCode) return null
            val fullYearMicros = monthlyMicros * ProPlan.YEARLY.billingMonths
            val percent = ((fullYearMicros - yearlyMicros) * PERCENT / fullYearMicros).toInt()
            return percent.takeIf { it >= MIN_SAVE_PERCENT }
        }

    val isStoreUnavailable: Boolean
        get() = !isLoadingProducts && planOptions.none { it.isAvailable }

    val canPurchase: Boolean
        get() = selectedOption.isAvailable && !isPurchasing && !isRestoring

    val canRestore: Boolean get() = !isPurchasing && !isRestoring
    val canClose: Boolean get() = closeSecondsRemaining <= 0

    private companion object {
        const val PERCENT = 100
        const val MIN_SAVE_PERCENT = 5
    }
}

class ProViewModel(
    private val billingManager: BillingManager,
    private val remoteConfigStore: AdsRemoteConfigStore,
    private val proStatusRefresher: ProStatusRefresher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

    init {
        startCloseCountdown()
        observeBillingConnection()
        observePurchaseResults()
        billingManager.startConnection()
    }

    fun selectPlan(plan: ProPlan) {
        val state = _uiState.value
        if (state.isPurchasing) return
        val option = ProPlanOption.from(plan, state.products)
        if (!state.isLoadingProducts && !option.isAvailable) return
        _uiState.update { it.copy(selectedPlan = plan) }
    }

    fun purchaseSelectedPlan(activity: Activity?) {
        val state = _uiState.value
        if (!state.canPurchase) return
        val option = state.selectedOption
        _uiState.update { it.copy(isPurchasing = true) }
        billingManager.purchase(
            activity = activity,
            productId = option.plan.productId,
            planId = option.plan.basePlanId,
            offerId = option.offerId,
        )
    }

    fun retryLoadProducts() {
        if (_uiState.value.isLoadingProducts) return
        _uiState.update { it.copy(isLoadingProducts = true) }
        if (billingManager.isBillingConnected) {
            viewModelScope.launch { loadProducts() }
        } else {
            billingManager.startConnection()
        }
    }

    fun consumePurchaseResult() {
        _uiState.update { it.copy(purchaseResult = null) }
    }

    fun restorePurchases() {
        if (!_uiState.value.canRestore) return
        _uiState.update { it.copy(isRestoring = true) }
        viewModelScope.launch {
            val result = proStatusRefresher.refresh()
            _uiState.update { it.copy(isRestoring = false, restoreResult = result) }
        }
    }

    fun consumeRestoreResult() {
        _uiState.update { it.copy(restoreResult = null) }
    }

    private fun startCloseCountdown() {
        viewModelScope.launch {
            while (_uiState.value.closeSecondsRemaining > 0) {
                delay(CLOSE_COUNTDOWN_TICK_MILLIS)
                _uiState.update { it.copy(closeSecondsRemaining = it.closeSecondsRemaining - 1) }
            }
        }
    }

    private fun observeBillingConnection() {
        viewModelScope.launch {
            billingManager.connectionState.collect { state ->
                when (state) {
                    BillingState.CONNECTED -> loadProducts()
                    BillingState.CONNECT_FAILED,
                    BillingState.DISCONNECTED -> _uiState.update {
                        it.copy(isLoadingProducts = false, products = emptyList())
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun observePurchaseResults() {
        viewModelScope.launch {
            billingManager.purchaseResults.collect { result ->
                _uiState.update { it.copy(isPurchasing = false) }
                if (result == PurchaseResult.AlreadyOwned) {
                    restorePurchases()
                } else {
                    _uiState.update { it.copy(purchaseResult = result) }
                }
            }
        }
    }

    private suspend fun loadProducts() {
        _uiState.update { it.copy(isLoadingProducts = true) }
        when (val response = billingManager.queryProducts()) {
            is QueryResponse.Loading -> Unit
            is QueryResponse.Success -> _uiState.update {
                it.copy(
                    isLoadingProducts = false,
                    products = response.data,
                    selectedPlan = availablePlan(it.selectedPlan, response.data),
                )
            }

            is QueryResponse.Error -> _uiState.update {
                it.copy(isLoadingProducts = false, products = emptyList())
            }
        }
    }

    private fun availablePlan(preferred: ProPlan, products: List<ProductDetail>): ProPlan {
        if (ProPlanOption.from(preferred, products).isAvailable) return preferred
        return ProPlan.entries.firstOrNull { ProPlanOption.from(it, products).isAvailable } ?: preferred
    }

    private fun initialState() = ProUiState(
        selectedPlan = remoteConfigStore.current.premiumDefaultPlan,
        closeSecondsRemaining = remoteConfigStore.current.premiumCloseDelay,
        closePosition = remoteConfigStore.current.premiumClosePosition,
    )

    private companion object {
        const val CLOSE_COUNTDOWN_TICK_MILLIS = 1_000L
    }
}
