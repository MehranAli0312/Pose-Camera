package com.pdfutility.billing.presentation.interfaces

import com.pdfutility.billing.data.entities.product.ProductDetail

interface BillingProductDetailsListener {
    fun onSuccess(productDetails: List<ProductDetail>)
    fun onError(message: String) {}
}