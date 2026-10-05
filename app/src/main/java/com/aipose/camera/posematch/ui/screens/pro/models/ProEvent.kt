package com.aipose.camera.posematch.ui.screens.pro.models

sealed interface ProEvent {

    data object Purchased : ProEvent

    data object AlreadyOwned : ProEvent

    data object Restored : ProEvent

    data object NothingToRestore : ProEvent

    data object RestoreFailed : ProEvent

    data object PurchasePending : ProEvent

    data object PurchaseCancelled : ProEvent

    data class PurchaseFailed(val reason: String) : ProEvent

    val closesPaywall: Boolean
        get() = this == Purchased || this == AlreadyOwned || this == Restored || this == PurchasePending
}
