package com.pdfutility.billing.domain

import com.pdfutility.billing.data.repository.BillingRepository
import com.pdfutility.billing.presentation.states.BillingState

internal class UseCaseConnection(private val repository: BillingRepository) {

    private var isConnecting = false

    fun startConnection(onResult: (Boolean, String?) -> Unit) {
        if (repository.isBillingClientReady) {
            repository.currentState = BillingState.ALREADY_CONNECTED
            onResult(true, null)
            return
        }

        if (isConnecting) {
            repository.currentState = BillingState.CONNECTING_IN_PROGRESS
            onResult(false, null)
            return
        }

        isConnecting = true
        try {
            repository.startConnection(onResult)
        } finally {
            isConnecting = false
        }
    }
}