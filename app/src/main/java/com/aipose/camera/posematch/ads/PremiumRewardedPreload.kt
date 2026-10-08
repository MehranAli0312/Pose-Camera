package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.ads.AdPlacement

@Composable
fun PremiumRewardedPreloadEffect(hasLockedPoses: Boolean) {
    val ads = rememberScreenAds()

    LaunchedEffect(ads, hasLockedPoses) {
        if (!hasLockedPoses) return@LaunchedEffect
        if (!ads.awaitAdsAllowed()) return@LaunchedEffect
        ads.awaitReady()
        ads.preload(AdPlacement.PremiumRewarded)
    }
}
