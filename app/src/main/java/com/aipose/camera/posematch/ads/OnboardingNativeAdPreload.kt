package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdPlacement
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun OnboardingNativeAdPreloadEffect(isFirstSession: Boolean) {
    val ads = rememberScreenAds()
    val store: AdsRemoteConfigStore = koinInject()

    LaunchedEffect(ads, store, isFirstSession) {
        if (!isFirstSession) return@LaunchedEffect
        store.config.first { config ->
            config.showOnboardingScreen && config.onboardingNativeAd.afterStepCount != null
        }
        if (!ads.awaitAdsAllowed()) return@LaunchedEffect
        ads.prepareSlot(AdPlacement.OnboardingFullScreenNative)
    }
}
