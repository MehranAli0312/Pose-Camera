package com.aipose.camera.posematch.ads

import com.example.ads.AdPlacement
import com.aipose.camera.posematch.data.local.NetworkConnectivityChecker
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

suspend fun prepareSplashAd(
    ads: ScreenAds,
    appFirebaseRemote: AppFirebaseRemote,
    network: NetworkConnectivityChecker,
) {
    if (!ads.awaitAdsAllowed()) return

    if (!network.hasActiveInternet()) return

    appFirebaseRemote.resolution.first { resolution ->
        resolution.isEnoughToDecide(appFirebaseRemote.hasEverActivatedRemoteConfig)
    }

    if (ads.styleFor(AdPlacement.SplashFullscreen).format == null) return

    ads.awaitReady()

    while (!ads.isAvailable(AdPlacement.SplashFullscreen)) {
        delay(SplashAdTiming.AD_READY_POLL_INTERVAL_MS.milliseconds)
    }
}
