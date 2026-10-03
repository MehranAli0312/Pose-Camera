package com.aipose.camera.posematch.ui.screens.pro.models

import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.utilities.extensions.freeTrialPhase
import com.pdfutility.billing.utilities.extensions.regularPhase
import com.pdfutility.billing.utilities.extensions.regularPrice
import com.aipose.camera.posematch.domain.models.ProPlan

data class ProPlanOption(
    val plan: ProPlan,
    val product: ProductDetail?,
) {
    val price: String? get() = product?.regularPrice

    val isAvailable: Boolean get() = price != null

    val offerId: String? get() = product?.offerId

    val priceMicros: Long?
        get() = product?.regularPhase?.priceAmountMicros?.takeIf { it > 0 }

    val currencyCode: String?
        get() = product?.regularPhase?.currencyCode?.takeIf { it.isNotBlank() }

    val monthlyPriceMicros: Long?
        get() = priceMicros?.div(plan.billingMonths)

    val freeTrialDays: Int?
        get() = product?.freeTrialPhase?.freeTrialPeriod?.takeIf { it > 0 }

    companion object {
        fun from(plan: ProPlan, products: List<ProductDetail>): ProPlanOption {
            val candidates = products.filter {
                it.productId == plan.productId && it.planId == plan.basePlanId
            }
            val trialOffer = candidates.firstOrNull { it.offerId != null && it.freeTrialPhase != null }
            val basePlan = candidates.firstOrNull { it.offerId == null }
            return ProPlanOption(plan = plan, product = trialOffer ?: basePlan)
        }
    }
}
