package com.example.ads

data class AdEvent(
    val type: AdEventType,
    val format: AdFormat,
    val placement: AdPlacement,
)
