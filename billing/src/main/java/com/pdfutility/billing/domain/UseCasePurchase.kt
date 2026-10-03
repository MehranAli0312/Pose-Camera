package com.pdfutility.billing.domain

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.pdfutility.billing.data.repository.BillingRepository
import com.pdfutility.billing.presentation.states.BillingState
import com.pdfutility.billing.presentation.states.QueryResponse
import com.pdfutility.billing.utilities.constants.Constants.TAG
import com.pdfutility.billing.utilities.extensions.isCompleted
import com.pdfutility.billing.utilities.extensions.needsAcknowledgement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class UseCasePurchase(private val repository: BillingRepository) {

    suspend fun purchaseInApp(activity: Activity?, productId: String): QueryResponse<String> = withContext(Dispatchers.Default) {
        if (activity == null) {
            repository.currentState = BillingState.ACTIVITY_REFERENCE_NOT_FOUND
            return@withContext QueryResponse.Error("Activity Ref is null")
        }

        if (productId.isEmpty()) {
            repository.currentState = BillingState.CONSOLE_BUY_PRODUCT_EMPTY_ID
            return@withContext QueryResponse.Error("Product Id can't be empty")
        }

        /* ─── Guard clauses ─────────────────────────── */
        if (!repository.isBillingClientReady) {
            repository.currentState = BillingState.CONNECTION_INVALID
            return@withContext QueryResponse.Error("Play Billing not ready. Try again.")
        }

        val response = repository.queryInAppProductDetails(listOf(productId))
        if (response.isNullOrEmpty()) {
            repository.currentState = BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST
            return@withContext QueryResponse.Error("Product Details are not found")
        }

        val productDetails = response[0]
        val offerToken = null
        //val offerToken = productDetails.oneTimePurchaseOfferDetailsList?.get(0)?.offerToken      // for Billing V8.0.0

        val productDetailsParamsList = listOf(
            when (offerToken != null) {
                true -> BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(offerToken).build()
                false -> BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build()
            }
        )

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        launchPurchaseFlow(activity, params)
    }

    suspend fun purchaseSubs(activity: Activity?, productId: String, planId: String, offerId: String? = null): QueryResponse<String> = withContext(Dispatchers.Default) {
        if (activity == null) {
            repository.currentState = BillingState.ACTIVITY_REFERENCE_NOT_FOUND
            return@withContext QueryResponse.Error("Activity Ref is null")
        }

        if (productId.isEmpty()) {
            repository.currentState = BillingState.CONSOLE_BUY_PRODUCT_EMPTY_ID
            return@withContext QueryResponse.Error("Product Id can't be empty")
        }

        /* ─── Guard clauses ─────────────────────────── */
        if (!repository.isBillingClientReady) {
            repository.currentState = BillingState.CONNECTION_INVALID
            return@withContext QueryResponse.Error("Play Billing not ready. Try again.")
        }

        val response = repository.querySubsProductDetails(listOf(productId))
        if (response.isNullOrEmpty()) {
            repository.currentState = BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST
            return@withContext QueryResponse.Error("Product Details are not found")
        }

        val productDetails = response[0]
        val offerToken = productDetails.subscriptionOfferDetails
            ?.find { it.basePlanId == planId && it.offerId == offerId }
            ?.offerToken

        if (offerToken == null) {
            repository.currentState = BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST
            return@withContext QueryResponse.Error("Product Details are not found")
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams
                .newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        launchPurchaseFlow(activity, params)
    }


//    suspend fun updateSubs(
//        activity: Activity?,
//        oldProductId: String,
//        productId: String,
//        planId: String
//    ): QueryResponse<String> = withContext(Dispatchers.Default) {
//
//        if (activity == null) return@withContext QueryResponse.Error("Activity is null")
//
//        if (!repository.isBillingClientReady) {
//            repository.currentState = BillingState.CONNECTION_INVALID
//            return@withContext QueryResponse.Error("Play Billing not ready. Try again.")
//        }
//
//        // 1️⃣ Get new product details
//        val productDetailsList = repository.querySubsProductDetails(listOf(productId))
//        if (productDetailsList.isNullOrEmpty()) {
//            return@withContext QueryResponse.Error("Product details not found for $productId")
//        }
//
//        val productDetails = productDetailsList.first()
//
//        // 2️⃣ Get existing purchase
//        val purchaseList = repository.querySubsPurchases()
//        val oldPurchase = purchaseList.find { it.products.contains(oldProductId) }
//
//        if (oldPurchase == null) {
//            return@withContext QueryResponse.Error("Old subscription not found for $oldProductId")
//        }
//
//        // 3️⃣ Get offer token for the new plan
//        val offerToken = productDetails.subscriptionOfferDetails
//            ?.find { it.basePlanId == planId }
//            ?.offerToken
//
//        if (offerToken == null) {
//            return@withContext QueryResponse.Error("Offer token not found for plan $planId")
//        }
//
//        // 4️⃣ Build flow params
//        val productDetailsParamsList = listOf(
//            BillingFlowParams.ProductDetailsParams
//                .newBuilder()
//                .setProductDetails(productDetails)
//                .setOfferToken(offerToken)
//                .build()
//        )
//
//        val productUpdateParams = BillingFlowParams.SubscriptionUpdateParams
//            .newBuilder()
//            .setOldPurchaseToken(oldPurchase.purchaseToken) // ✅ Use the real token!
//            .setSubscriptionReplacementMode(
//                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE
//            )
//            .build()
//
//        val billingFlowParams = BillingFlowParams.newBuilder()
//            .setProductDetailsParamsList(productDetailsParamsList)
//            .setSubscriptionUpdateParams(productUpdateParams)
//            .build()
//
//        repository.purchaseFlow(activity, billingFlowParams)
//
//        repository.currentState = BillingState.BILLING_FLOW_LAUNCHED_SUCCESSFULLY
//        QueryResponse.Success("Billing flow launched successfully")
//    }

    suspend fun updateSubs(activity: Activity?, oldProductId: String, productId: String, planId: String): QueryResponse<String> = withContext(Dispatchers.Default) {
        if (activity == null) {
            repository.currentState = BillingState.ACTIVITY_REFERENCE_NOT_FOUND
            return@withContext QueryResponse.Error("Activity Ref is null")
        }

        if (oldProductId.isEmpty()) {
            repository.currentState = BillingState.CONSOLE_PRODUCTS_OLD_SUB_NOT_FOUND
            return@withContext QueryResponse.Error("Old Product Id can't be empty")
        }

        if (productId.isEmpty()) {
            repository.currentState = BillingState.CONSOLE_BUY_PRODUCT_EMPTY_ID
            return@withContext QueryResponse.Error("Product Id can't be empty")
        }

        /* ─── Guard clauses ─────────────────────────── */
        if (!repository.isBillingClientReady) {
            repository.currentState = BillingState.CONNECTION_INVALID
            return@withContext QueryResponse.Error("Play Billing not ready. Try again.")
        }

        val response = repository.querySubsProductDetails(listOf(productId))
        if (response.isNullOrEmpty()) {
            repository.currentState = BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST
            return@withContext QueryResponse.Error("Product Details are not found")
        }

        val purchaseList = repository.querySubsPurchases()
        val oldPurchase = purchaseList.find { it.products.any { it == oldProductId } }

        if (oldPurchase == null) {
            repository.currentState = BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST
            return@withContext QueryResponse.Error("Product Details are not found")
        }

        val productDetails = response.first()


        val offerToken = productDetails.subscriptionOfferDetails?.find { it.basePlanId == planId }?.offerToken

        Log.d(TAG, "BillingService: updateSubs: Offer Token: $offerToken")

        if (offerToken == null) {
            repository.currentState = BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST
            return@withContext QueryResponse.Error("Product Details are not found")
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams
                .newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val productUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
            .setOldPurchaseToken(oldPurchase.purchaseToken)
            .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE)
            .build()

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setSubscriptionUpdateParams(productUpdateParams)
            .build()

        launchPurchaseFlow(activity, params)
    }

    private suspend fun launchPurchaseFlow(
        activity: Activity,
        params: BillingFlowParams,
    ): QueryResponse<String> {
        val billingResult = repository.purchaseFlow(activity, params)
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                repository.currentState = BillingState.BILLING_FLOW_LAUNCHED_SUCCESSFULLY
                QueryResponse.Success("Billing flow launched successfully")
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                repository.currentState = BillingState.BILLING_FLOW_USER_CANCELLED
                QueryResponse.Error(BillingState.BILLING_FLOW_USER_CANCELLED.message)
            }

            else -> {
                repository.currentState = BillingState.BILLING_FLOW_EXCEPTION
                val message = billingResult.debugMessage.ifBlank {
                    BillingState.BILLING_FLOW_EXCEPTION.message
                }
                Log.e(TAG, "launchPurchaseFlow failed: ${billingResult.responseCode} - $message")
                QueryResponse.Error(message)
            }
        }
    }

    suspend fun handlePurchase(purchases: List<Purchase>?, consumableIds: List<String>) = withContext(Dispatchers.Default) {
        if (purchases.isNullOrEmpty()) {
            repository.currentState = BillingState.PURCHASES_NOT_FOUND
            return@withContext
        }
        checkForAcknowledgedPurchases(purchases)
        checkForConsumablePurchases(purchases, consumableIds)
    }

    private suspend fun checkForAcknowledgedPurchases(purchases: List<Purchase>) {
        repository.currentState = BillingState.ACKNOWLEDGE_PURCHASE
        val unAcknowledgeList = purchases.filter { it.needsAcknowledgement() }
        Log.i(TAG, "BillingService: checkForAcknowledgedPurchases: ${unAcknowledgeList.size} purchase(s) needs to be acknowledge")

        if (unAcknowledgeList.isNotEmpty()) {
            repository.currentState = BillingState.ACKNOWLEDGE_PURCHASE
            repository.acknowledgePurchases(unAcknowledgeList)
            repository.currentState = BillingState.ACKNOWLEDGE_PURCHASE_SUCCESS
        } else {
            repository.currentState = BillingState.ACKNOWLEDGE_PURCHASE_FAILURE
        }
    }

    private suspend fun checkForConsumablePurchases(purchases: List<Purchase>, consumableIds: List<String>) {
        val consumablePurchases = purchases.filter { purchase ->
            purchase.isCompleted() && purchase.products.any(consumableIds::contains)
        }
        Log.i(TAG, "BillingService: checkForConsumablePurchases: ${consumablePurchases.size} purchase(s) needs to be consumed")

        if (consumablePurchases.isNotEmpty()) {
            repository.currentState = BillingState.CONSUME_PURCHASE
            repository.consumePurchases(consumablePurchases)
            repository.currentState = BillingState.CONSUME_PURCHASE_SUCCESS
        } else {
            repository.currentState = BillingState.CONSUME_PURCHASE_FAILURE
        }
    }
}