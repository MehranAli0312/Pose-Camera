package com.aipose.camera.posematch.data.local.mapper

import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.utilities.extensions.freeTrialPhase
import com.pdfutility.billing.utilities.extensions.regularPhase

fun List<ProductDetail>.toPremiumPlans(): List<PremiumPlan> =
    ProPlan.entries.mapNotNull { plan -> toPremiumPlan(plan) }

private fun List<ProductDetail>.toPremiumPlan(plan: ProPlan): PremiumPlan? {
    val productOffers = filter { it.productId == plan.productId }
    val basePlanId = productOffers.preferredBasePlanId(plan.billingPeriod) ?: return null
    val candidates = productOffers.filter { it.planId == basePlanId }
    val trialOffer = candidates.firstOrNull { it.offerId != null && it.freeTrialPhase != null }
    val product = trialOffer ?: candidates.firstOrNull { it.offerId == null } ?: return null
    val regularPhase = product.regularPhase ?: return null
    val formattedPrice = regularPhase.price.takeIf { it.isNotBlank() } ?: return null
    return PremiumPlan(
        plan = plan,
        basePlanId = basePlanId.takeUnless { plan.isOneTime },
        offerId = product.offerId,
        formattedPrice = formattedPrice,
        priceMicros = regularPhase.priceAmountMicros,
        currencyCode = regularPhase.currencyCode,
        trialDays = product.freeTrialPhase?.freeTrialPeriod?.coerceAtLeast(0) ?: 0,
    )
}

private fun List<ProductDetail>.preferredBasePlanId(billingPeriod: String?): String? {
    val basePlans = filter { it.offerId == null }
    val matching = basePlans.firstOrNull { it.regularPhase?.billingPeriod == billingPeriod }
    return (matching ?: basePlans.firstOrNull())?.planId
}
