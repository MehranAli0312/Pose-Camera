package com.pdfutility.billing.utilities.extensions

import com.android.billingclient.api.Purchase

internal fun Purchase.isCompleted(): Boolean =
    purchaseState == Purchase.PurchaseState.PURCHASED

internal fun Purchase.isPending(): Boolean =
    purchaseState == Purchase.PurchaseState.PENDING

internal fun Purchase.needsAcknowledgement(): Boolean =
    isCompleted() && !isAcknowledged
