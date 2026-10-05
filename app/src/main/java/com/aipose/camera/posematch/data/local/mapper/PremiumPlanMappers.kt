package com.aipose.camera.posematch.data.local.mapper

import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.utilities.extensions.freeTrialPhase
import com.pdfutility.billing.utilities.extensions.regularPhase
import com.pdfutility.billing.utilities.extensions.regularPrice

fun List<ProductDetail>.toPremiumPlans(): List<PremiumPlan> =
    ProPlan.entries.mapNotNull { plan -> toPremiumPlan(plan) }

private fun List<ProductDetail>.toPremiumPlan(plan: ProPlan): PremiumPlan? {
    val candidates = filter { it.productId == plan.productId && it.planId == plan.basePlanId }
    val trialOffer = candidates.firstOrNull { it.offerId != null && it.freeTrialPhase != null }
    val basePlan = candidates.firstOrNull { it.offerId == null }
    val product = trialOffer ?: basePlan ?: return null
    val regularPhase = product.regularPhase ?: return null
    val formattedPrice = product.regularPrice ?: return null
    return PremiumPlan(
        plan = plan,
        offerId = product.offerId,
        formattedPrice = formattedPrice,
        priceMicros = regularPhase.priceAmountMicros,
        currencyCode = regularPhase.currencyCode,
        trialDays = product.freeTrialPhase?.freeTrialPeriod?.coerceAtLeast(0) ?: 0,
    )
}
