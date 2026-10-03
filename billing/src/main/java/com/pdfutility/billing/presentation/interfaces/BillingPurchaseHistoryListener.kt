package com.pdfutility.billing.presentation.interfaces

import com.pdfutility.billing.data.entities.purchase.PurchaseDetail

interface BillingPurchaseHistoryListener {
    fun onSuccess(purchaseDetails: List<PurchaseDetail>)
    fun onError(message: String) {}
}