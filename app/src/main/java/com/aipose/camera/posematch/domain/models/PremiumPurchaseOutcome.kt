package com.aipose.camera.posematch.domain.models

sealed interface PremiumPurchaseOutcome {

    data object Purchased : PremiumPurchaseOutcome

    data object AlreadyOwned : PremiumPurchaseOutcome

    data object Pending : PremiumPurchaseOutcome

    data object Cancelled : PremiumPurchaseOutcome

    data class Failed(val reason: String) : PremiumPurchaseOutcome
}
