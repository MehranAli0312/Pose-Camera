package com.pdfutility.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.pdfutility.billing.data.dataSource.BillingService
import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.data.entities.purchase.PurchaseDetail
import com.pdfutility.billing.data.repository.BillingRepository
import com.pdfutility.billing.domain.UseCaseConnection
import com.pdfutility.billing.domain.UseCasePurchase
import com.pdfutility.billing.domain.UseCaseQueryProducts
import com.pdfutility.billing.domain.UseCaseQueryPurchases
import com.pdfutility.billing.presentation.interfaces.BillingConnectionListener
import com.pdfutility.billing.presentation.interfaces.BillingProductDetailsListener
import com.pdfutility.billing.presentation.interfaces.BillingPurchaseHistoryListener
import com.pdfutility.billing.presentation.interfaces.BillingPurchaseListener
import com.pdfutility.billing.presentation.states.BillingState
import com.pdfutility.billing.presentation.states.PurchaseResult
import com.pdfutility.billing.presentation.states.QueryResponse
import com.pdfutility.billing.utilities.constants.Constants.TAG
import com.pdfutility.billing.utilities.extensions.isCompleted
import com.pdfutility.billing.utilities.extensions.isPending
import com.pdfutility.billing.utilities.extensions.setAll
import com.pdfutility.billing.utilities.responses.BillingResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class BillingManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases: List<Purchase>? ->
        Log.d(TAG, "BillingManager: purchasesUpdatedListener: ${billingResult.responseCode} -- ${billingResult.debugMessage}")
        purchaseUpdateListener(billingResult, purchases)
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
    }

    private val billingService by lazy { BillingService(billingClient) }
    private val billingRepository by lazy { BillingRepository(billingService) }
    private val useCaseConnection by lazy { UseCaseConnection(billingRepository) }
    private val useCaseQueryPurchases by lazy { UseCaseQueryPurchases(billingRepository) }
    private val useCaseQueryProducts by lazy { UseCaseQueryProducts(billingRepository) }
    private val useCasePurchase by lazy { UseCasePurchase(billingRepository) }

    private val _nonConsumables = mutableListOf<String>()
    private val _consumables = mutableListOf<String>()
    private val _subscriptions = mutableListOf<String>()
    private var connectionListener: BillingConnectionListener? = null

    val nonConsumableIds: List<String> get() = _nonConsumables
    val consumableIds: List<String> get() = _consumables
    val subscriptionIds: List<String> get() = _subscriptions

    fun setNonConsumables(ids: List<String>) = apply { _nonConsumables.setAll(ids) }
    fun setConsumables(ids: List<String>) = apply { _consumables.setAll(ids) }
    fun setSubscriptions(ids: List<String>) = apply { _subscriptions.setAll(ids) }
    fun setListener(listener: BillingConnectionListener?) = apply { connectionListener = listener }

    private var billingPurchaseListener: BillingPurchaseListener? = null
    private val isPurchaseInProgress = AtomicBoolean(false)

    private val _connectionState = MutableStateFlow(BillingState.NONE)
    val connectionState: StateFlow<BillingState> = _connectionState.asStateFlow()

    private val _purchaseResults = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 1)
    val purchaseResults: SharedFlow<PurchaseResult> = _purchaseResults.asSharedFlow()

    val isBillingConnected: Boolean get() = billingService.isBillingClientReady

    fun startConnection() {
        _connectionState.value = BillingState.CONNECTING
        useCaseConnection.startConnection { isSuccess, message ->
            _connectionState.value = if (isSuccess) BillingState.CONNECTED else billingService.currentState
            connectionListener?.onBillingClientConnected(isSuccess, message ?: billingService.currentState.message)
        }
    }

    suspend fun queryProducts(): QueryResponse<List<ProductDetail>> =
        useCaseQueryProducts.queryProducts(nonConsumableIds, consumableIds, subscriptionIds)

    suspend fun queryProduct(productId: String, planId: String?): QueryResponse<List<ProductDetail>> =
        useCaseQueryProducts.queryProducts(productId, planId)

    suspend fun queryPurchases(): QueryResponse<List<PurchaseDetail>> =
        useCaseQueryPurchases.queryPurchases()

    fun purchase(activity: Activity?, productId: String, planId: String? = null, offerId: String? = null) {
        val preflightError = startPurchaseFlow()
        if (preflightError != null) {
            _purchaseResults.tryEmit(PurchaseResult.Error(preflightError))
            return
        }
        billingPurchaseListener = null

        scope.launch {
            val isSubscription = planId != null || productId in subscriptionIds
            val launchError = if (isSubscription) {
                launchSubscriptionFlow(activity, productId, planId.orEmpty(), offerId)
            } else {
                launchInAppFlow(activity, productId)
            }
            if (launchError != null) {
                finishPurchaseFlow()
                _purchaseResults.tryEmit(PurchaseResult.Error(launchError))
            }
        }
    }

    fun fetchPurchaseHistory(listener: BillingPurchaseHistoryListener) {
        scope.launch {
            when (val response = useCaseQueryPurchases.queryPurchases()) {
                is QueryResponse.Loading -> {}
                is QueryResponse.Success -> listener.onSuccess(response.data)
                is QueryResponse.Error -> listener.onError(response.errorMessage)
            }
        }
    }

    fun fetchProductDetails(listener: BillingProductDetailsListener) {
        scope.launch {
            when (val response = useCaseQueryProducts.queryProducts(nonConsumableIds, consumableIds, subscriptionIds)) {
                is QueryResponse.Loading -> {}
                is QueryResponse.Success -> listener.onSuccess(response.data)
                is QueryResponse.Error -> listener.onError(response.errorMessage)
            }
        }
    }

    fun getProductDetail(productId: String, planId: String?, listener: BillingProductDetailsListener) {
        scope.launch {
            when (val response = useCaseQueryProducts.queryProducts(productId, planId)) {
                is QueryResponse.Loading -> {}
                is QueryResponse.Success -> listener.onSuccess(response.data)
                is QueryResponse.Error -> listener.onError(response.errorMessage)
            }
        }
    }

    fun purchaseInApp(activity: Activity?, productId: String, listener: BillingPurchaseListener) {
        startPurchaseFlow()?.let { error ->
            listener.onError(error)
            return
        }
        this.billingPurchaseListener = listener

        scope.launch {
            launchInAppFlow(activity, productId)?.let { error ->
                finishPurchaseFlow()
                listener.onError(error)
            }
        }
    }

    fun purchaseSubs(activity: Activity?, productId: String, planId: String, listener: BillingPurchaseListener) {
        startPurchaseFlow()?.let { error ->
            listener.onError(error)
            return
        }
        this.billingPurchaseListener = listener

        scope.launch {
            launchSubscriptionFlow(activity, productId, planId)?.let { error ->
                finishPurchaseFlow()
                listener.onError(error)
            }
        }
    }

    fun updateSubs(activity: Activity?, oldProductId: String, productId: String, planId: String, listener: BillingPurchaseListener) {
        startPurchaseFlow()?.let { error ->
            listener.onError(error)
            return
        }
        this.billingPurchaseListener = listener

        scope.launch {
            launchFlow { useCasePurchase.updateSubs(activity, oldProductId, productId, planId) }?.let { error ->
                finishPurchaseFlow()
                listener.onError(error)
            }
        }
    }

    private suspend fun launchInAppFlow(activity: Activity?, productId: String): String? =
        launchFlow { useCasePurchase.purchaseInApp(activity, productId) }

    private suspend fun launchSubscriptionFlow(activity: Activity?, productId: String, planId: String, offerId: String? = null): String? =
        launchFlow { useCasePurchase.purchaseSubs(activity, productId, planId, offerId) }

    private suspend fun launchFlow(launch: suspend () -> QueryResponse<String>): String? = try {
        when (val response = launch()) {
            is QueryResponse.Loading -> null
            is QueryResponse.Success -> null
            is QueryResponse.Error -> response.errorMessage
        }
    } catch (e: Exception) {
        e.message ?: BillingState.BILLING_FLOW_EXCEPTION.message
    }

    private fun startPurchaseFlow(): String? {
        if (!billingService.isBillingClientReady) {
            return BillingState.CONNECTION_INVALID.message
        }
        if (!isPurchaseInProgress.compareAndSet(false, true)) {
            return BillingState.BILLING_FLOW_EXCEPTION.message
        }
        return null
    }

    private fun finishPurchaseFlow() {
        isPurchaseInProgress.set(false)
    }

    private fun purchaseUpdateListener(billingResult: BillingResult, purchases: List<Purchase>?) {
        scope.launch {
            try {
                val response = BillingResponse(billingResult.responseCode)
                _purchaseResults.tryEmit(response.toPurchaseResult(purchases))
                when {
                    response.isOk -> {
                        billingService.currentState = BillingState.PURCHASE_SUCCESS
                        billingPurchaseListener?.onPurchaseResult(BillingState.PURCHASE_SUCCESS.message, true)
                        useCasePurchase.handlePurchase(purchases, consumableIds)
                        return@launch
                    }

                    response.isAlreadyOwned -> {
                        billingService.currentState = BillingState.PURCHASE_ALREADY_OWNED
                        billingPurchaseListener?.onPurchaseResult(BillingState.PURCHASE_ALREADY_OWNED.message, false)
                        useCasePurchase.handlePurchase(purchases, consumableIds)
                        return@launch
                    }

                    response.isUserCancelled -> billingService.currentState = BillingState.BILLING_FLOW_USER_CANCELLED
                    response.isTerribleFailure -> billingService.currentState = BillingState.BILLING_FLOW_EXCEPTION
                    response.isRecoverableError -> billingService.currentState = BillingState.BILLING_FLOW_EXCEPTION
                    response.isNonrecoverableError -> billingService.currentState = BillingState.BILLING_FLOW_EXCEPTION
                }
                billingService.currentState = BillingState.PURCHASE_FAILED
                billingPurchaseListener?.onError(billingService.currentState.message)
            } finally {
                finishPurchaseFlow()
            }
        }
    }

    private fun BillingResponse.toPurchaseResult(purchases: List<Purchase>?): PurchaseResult = when {
        isOk -> purchases.orEmpty().toPurchaseResult()
        isAlreadyOwned -> PurchaseResult.AlreadyOwned
        isUserCancelled -> PurchaseResult.Cancelled
        else -> PurchaseResult.Error(BillingState.PURCHASE_FAILED.message)
    }

    private fun List<Purchase>.toPurchaseResult(): PurchaseResult {
        val completed = filter { it.isCompleted() }
        return when {
            completed.isNotEmpty() -> PurchaseResult.Success(completed.flatMap { it.products })
            any { it.isPending() } -> PurchaseResult.Pending
            else -> PurchaseResult.Error(BillingState.PURCHASE_FAILED.message)
        }
    }
}
