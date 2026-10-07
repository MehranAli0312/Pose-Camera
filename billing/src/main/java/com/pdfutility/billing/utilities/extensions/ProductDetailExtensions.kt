package com.pdfutility.billing.utilities.extensions

import com.pdfutility.billing.data.entities.product.PricingPhase
import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.data.entities.product.RecurringMode

val ProductDetail.regularPhase: PricingPhase?
    get() = pricingDetails.firstOrNull { it.recurringMode == RecurringMode.ORIGINAL }
        ?: pricingDetails.lastOrNull()

val ProductDetail.freeTrialPhase: PricingPhase?
    get() = pricingDetails.firstOrNull { it.recurringMode == RecurringMode.FREE }

val ProductDetail.regularPrice: String?
    get() = regularPhase?.price?.takeIf { it.isNotBlank() }