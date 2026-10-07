package com.aipose.camera.posematch.data.local.mapper

import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.pdfutility.billing.data.entities.product.ProductDetail
import com.pdfutility.billing.utilities.extensions.freeTrialPhase
import com.pdfutility.billing.utilities.extensions.regularPhase
import com.pdfutility.billing.utilities.extensions.regularPrice

private const val YEARLY_BILLING_PERIOD = "P1Y"

fun List<ProductDetail>.toPremiumPlans(): List<PremiumPlan> =
    ProPlan.entries.mapNotNull { plan -> toPremiumPlan(plan) }

private fun List<ProductDetail>.toPremiumPlan(plan: ProPlan): PremiumPlan? {
    val productOffers = filter { it.productId == plan.productId }
    val basePlanId = productOffers.preferredBasePlanId() ?: return null
    val candidates = productOffers.filter { it.planId == basePlanId }
    val trialOffer = candidates.firstOrNull { it.offerId != null && it.freeTrialPhase != null }
    val product = trialOffer ?: candidates.firstOrNull { it.offerId == null } ?: return null
    val formattedPrice = product.regularPrice ?: return null
    return PremiumPlan(
        plan = plan,
        basePlanId = basePlanId.takeUnless { plan.isOneTime },
        offerId = product.offerId,
        formattedPrice = formattedPrice,
        trialDays = product.freeTrialPhase?.freeTrialPeriod?.coerceAtLeast(0) ?: 0,
    )
}

private fun List<ProductDetail>.preferredBasePlanId(): String? {
    val basePlans = filter { it.offerId == null }
    val yearly = basePlans.firstOrNull { it.regularPhase?.billingPeriod == YEARLY_BILLING_PERIOD }
    return (yearly ?: basePlans.firstOrNull())?.planId
}
