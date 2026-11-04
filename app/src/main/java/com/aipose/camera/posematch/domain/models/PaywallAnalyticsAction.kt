package com.aipose.camera.posematch.domain.models

enum class PaywallAnalyticsAction(val eventName: String) {
    Viewed("paywall_view"),
    PlanSelected("paywall_plan_selected"),
    PurchaseClicked("paywall_purchase_click"),
    Purchased("paywall_purchase_success"),
    PurchasePending("paywall_purchase_pending"),
    PurchaseCancelled("paywall_purchase_cancel"),
    PurchaseFailed("paywall_purchase_failed"),
    Restored("paywall_restore_success"),
    Closed("paywall_close"),
}
