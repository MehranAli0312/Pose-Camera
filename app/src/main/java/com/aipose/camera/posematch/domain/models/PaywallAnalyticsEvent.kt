package com.aipose.camera.posematch.domain.models

data class PaywallAnalyticsEvent(
    val action: PaywallAnalyticsAction,
    val plan: ProPlan?,
)
