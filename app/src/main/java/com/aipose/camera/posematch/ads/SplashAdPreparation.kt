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
    SplashAdLog.d("prepare: waiting for pro status")
    val adsAllowed = ads.awaitAdsAllowed()
    SplashAdLog.d("prepare: pro status resolved, adsAllowed=$adsAllowed")
    if (!adsAllowed) {
        SplashAdLog.d("prepare: STOP -> user is pro / not eligible")
        return
    }

    val online = network.hasActiveInternet()
    SplashAdLog.d("prepare: hasActiveInternet=$online")
    if (!online) {
        SplashAdLog.d("prepare: STOP -> no internet")
        return
    }

    SplashAdLog.d(
        "prepare: waiting for remote config, current=${appFirebaseRemote.resolution.value}, " +
            "hasEverActivated=${appFirebaseRemote.hasEverActivatedRemoteConfig}",
    )
    val resolution = appFirebaseRemote.resolution.first { resolution ->
        resolution.isEnoughToDecide(appFirebaseRemote.hasEverActivatedRemoteConfig)
    }
    SplashAdLog.d("prepare: remote config ready, resolution=$resolution")

    val style = ads.styleFor(AdPlacement.SplashFullscreen)
    SplashAdLog.d("prepare: splash style=$style format=${style.format}")
    if (style.format == null) {
        SplashAdLog.d("prepare: STOP -> splash ad disabled by remote config")
        return
    }

    SplashAdLog.d("prepare: waiting for ads SDK ready (consent + init)")
    ads.awaitReady()
    SplashAdLog.d("prepare: ads SDK ready, loading splash ad on demand")

    ads.loadOnDemand(AdPlacement.SplashFullscreen)
    var polls = 0
    while (!ads.isLoadedOnDemand(AdPlacement.SplashFullscreen)) {
        polls++
        if (polls % POLL_LOG_EVERY == 0) SplashAdLog.d("prepare: still no splash ad after $polls polls")
        delay(SplashAdTiming.AD_READY_POLL_INTERVAL_MS.milliseconds)
    }
    SplashAdLog.d("prepare: splash ad AVAILABLE after $polls polls")
}

private const val POLL_LOG_EVERY = 10
