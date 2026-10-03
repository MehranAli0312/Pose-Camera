package com.pdfutility.billing.presentation.interfaces

interface BillingPurchaseListener {
    fun onPurchaseResult(message: String, billingResponse: Boolean)
    fun onError(message: String)
}