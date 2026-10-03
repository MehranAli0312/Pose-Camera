package com.example.ads

data class AdsConfig(
    val isDebugBuild: Boolean,
    val testDeviceIds: List<String> = emptyList(),
    val preloadBufferSize: Int = 1,
    val interstitialFrequency: Int = 1,
    val minIntervalBetweenFullScreenAdsMs: Long = 0L,
    val enableLogging: Boolean = isDebugBuild,
)
