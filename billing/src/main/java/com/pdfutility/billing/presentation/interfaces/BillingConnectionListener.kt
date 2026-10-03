package com.pdfutility.billing.presentation.interfaces

interface BillingConnectionListener {
    fun onBillingClientConnected(isSuccess: Boolean, message: String)
}