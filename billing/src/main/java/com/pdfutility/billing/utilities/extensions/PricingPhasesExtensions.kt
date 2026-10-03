package com.pdfutility.billing.utilities.extensions

import com.android.billingclient.api.ProductDetails.PricingPhase
import com.android.billingclient.api.ProductDetails.PricingPhases

internal fun PricingPhases?.originalPhase(): PricingPhase? = this?.pricingPhaseList?.firstOrNull { phase ->
    phase.priceAmountMicros > 0 && phase.billingPeriod != "P0D"
}