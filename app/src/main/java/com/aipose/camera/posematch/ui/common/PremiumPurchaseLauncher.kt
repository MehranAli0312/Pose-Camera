package com.aipose.camera.posematch.ui.common

import android.app.Activity
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.pdfutility.billing.BillingManager

class PremiumPurchaseLauncher(
    private val billingManager: BillingManager,
) {

    fun launch(activity: Activity?, plan: PremiumPlan): Boolean {
        if (activity == null) return false
        billingManager.purchase(
            activity = activity,
            productId = plan.productId,
            planId = plan.basePlanId,
            offerId = plan.offerId,
        )
        return true
    }
}
