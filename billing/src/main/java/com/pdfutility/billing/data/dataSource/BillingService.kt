package com.pdfutility.billing.data.dataSource

import android.app.Activity
import android.os.Build
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.pdfutility.billing.presentation.states.BillingState
import com.pdfutility.billing.utilities.constants.Constants.TAG
import com.pdfutility.billing.utilities.responses.BillingResponse

internal class BillingService(private val billingClient: BillingClient) {

    private val inApp = BillingClient.ProductType.INAPP
    private val subs = BillingClient.ProductType.SUBS

    val isBillingClientReady get() = billingClient.isReady

    var currentState: BillingState = BillingState.NONE
        internal set(value) {
            field = value
            Log.d(TAG, "BillingService: updateState: $value")
        }

    fun startConnection(onResult: (Boolean, String?) -> Unit) {
        currentState = BillingState.CONNECTING

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                currentState = BillingState.DISCONNECTED
                onResult(false, null)
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val isSuccess = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                currentState = when (isSuccess) {
                    true -> BillingState.CONNECTED
                    false -> BillingState.CONNECT_FAILED
                }
                onResult(isSuccess, billingResult.debugMessage)
            }
        })
    }

    /* ------------------------------- Purchase History ------------------------------- */

    suspend fun queryInAppPurchases(): List<Purchase> {
        currentState = BillingState.FETCHING_INAPP_PURCHASES

        val params = QueryPurchasesParams.newBuilder().setProductType(inApp).build()
        val result = billingClient.queryPurchasesAsync(params)

        currentState = when (BillingResponse(result.billingResult.responseCode).isOk) {
            true -> BillingState.FETCHING_INAPP_PURCHASES_SUCCESS
            false -> BillingState.FETCHING_INAPP_PURCHASES_FAILED
        }

        Log.i(TAG, "BillingService: queryInAppPurchases: productType = $inApp, PurchaseList: ${result.purchasesList}, Error: ${result.billingResult.debugMessage}")
        return result.purchasesList
    }

    suspend fun querySubsPurchases(): List<Purchase> {
        currentState = BillingState.FETCHING_SUBSCRIPTION_PURCHASES

        val params = QueryPurchasesParams.newBuilder().setProductType(subs).build()
        val result = billingClient.queryPurchasesAsync(params)

        currentState = when (BillingResponse(result.billingResult.responseCode).isOk) {
            true -> BillingState.FETCHING_SUBSCRIPTION_PURCHASES_SUCCESS
            false -> BillingState.FETCHING_SUBSCRIPTION_PURCHASES_FAILED
        }

        Log.i(TAG, "BillingService: querySubsPurchases: productType = $subs, PurchaseList: ${result.purchasesList}, Error: ${result.billingResult.debugMessage}")
        return result.purchasesList
    }

    /* ----------------------------------- Products ----------------------------------- */

    suspend fun queryInAppProductDetails(productIds: List<String>): List<ProductDetails>? {
        currentState = BillingState.FETCHING_INAPP_PRODUCTS

        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(inApp).build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        val result = billingClient.queryProductDetails(params.build())

        currentState = when (BillingResponse(result.billingResult.responseCode).isOk) {
            true -> BillingState.FETCHING_INAPP_PRODUCTS_SUCCESS
            false -> BillingState.FETCHING_INAPP_PRODUCTS_FAILED
        }

        Log.i(TAG, "BillingService: queryInAppProductDetails: productType = $inApp, ProductDetailsList: ${result.productDetailsList}, Error: ${result.billingResult.debugMessage}")
        return result.productDetailsList
    }

    suspend fun querySubsProductDetails(productIds: List<String>): List<ProductDetails>? {
        currentState = BillingState.FETCHING_SUBSCRIPTION_PRODUCTS

        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(subs).build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        val result = billingClient.queryProductDetails(params.build())

        currentState = when (BillingResponse(result.billingResult.responseCode).isOk) {
            true -> BillingState.FETCHING_SUBSCRIPTION_PRODUCTS_SUCCESS
            false -> BillingState.FETCHING_SUBSCRIPTION_PRODUCTS_FAILED
        }

        Log.i(TAG, "BillingService: querySubsProductDetails: productType = $subs, ProductDetailsList: ${result.productDetailsList}, Error: ${result.billingResult.debugMessage}")
        return result.productDetailsList
    }

    /* ----------------------------------- Purchases ----------------------------------- */

    fun purchaseFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        if (!activity.isValidForBilling()) {
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.ERROR)
                .setDebugMessage("Activity is no longer valid for billing")
                .build()
        }
        return billingClient.launchBillingFlow(activity, params)
    }

    private fun Activity.isValidForBilling(): Boolean {
        if (isFinishing) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !isDestroyed
    }


    /* ------------------------------- Consume Products ------------------------------- */

    fun consumePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.consumeAsync(consumeParams) { billingResult, response ->
                when (BillingResponse(billingResult.responseCode).isOk) {
                    true -> Log.d(TAG, "BillingService: consumePurchases: Purchases has been successfully consumed for these products: ${purchase.products}, response: $response")
                    false -> Log.e(TAG, "BillingService: consumePurchases: Purchases has been failed to consume for these products: ${purchase.products}, response: $response")
                }
            }
        }
    }

    /* ------------------------------- Acknowledgements ------------------------------- */

    fun acknowledgePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                when (BillingResponse(billingResult.responseCode).isOk) {
                    true -> Log.d(TAG, "BillingService: acknowledgePurchases: Purchases has been successfully acknowledged for these products: ${purchase.products}")
                    false -> Log.e(TAG, "BillingService: acknowledgePurchases: Purchases has been failed to acknowledge for these products: ${purchase.products}")
                }
            }
        }
    }
}