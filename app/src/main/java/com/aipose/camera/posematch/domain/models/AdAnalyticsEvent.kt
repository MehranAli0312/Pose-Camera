package com.aipose.camera.posematch.domain.models

data class AdAnalyticsEvent(
    val action: AdAnalyticsAction,
    val format: String,
    val placement: String,
)
