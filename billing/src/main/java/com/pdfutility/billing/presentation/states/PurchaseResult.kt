package com.pdfutility.billing.presentation.states

sealed class PurchaseResult {
    data class Success(val productIds: List<String>) : PurchaseResult()
    data object AlreadyOwned : PurchaseResult()
    data object Pending : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}
