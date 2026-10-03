package com.aipose.camera.posematch.ads

import com.example.ads.AdPlacement
import com.example.ads.AdsManager

private val STARTUP_PLACEMENTS = listOf(AdPlacement.SplashFullscreen)

fun splashAdsLoads(adsManager: AdsManager) {
    adsManager.keepWarm(STARTUP_PLACEMENTS)
}

fun onPlacementConfigChanged(adsManager: AdsManager) {
    adsManager.revalidateWarmUp()
}
