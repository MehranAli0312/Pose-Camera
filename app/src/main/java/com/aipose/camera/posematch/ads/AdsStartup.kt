package com.aipose.camera.posematch.ads

import android.app.Application
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdsManager
import com.example.ads.lifecycle.AdsAppLifecycleObserver
import com.example.common.Constants
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun loadSplashAdOnSdkReady(
    adsManager: AdsManager,
    remoteConfigStore: AdsRemoteConfigStore,
    scope: CoroutineScope,
) {
    scope.launch {
        remoteConfigStore.restore()
        if (!adsManager.proStatus.first { it.isResolved }.isEligibleForAds) return@launch
        adsManager.isReady.first { it }
        SplashAdLog.d("sdk ready: loading splash ad on demand from app start")
        adsManager.loadOnDemand(AdPlacement.SplashFullscreen)
    }
}

fun registerAppOpenAds(
    application: Application,
    lifecycleObserver: AdsAppLifecycleObserver,
    remoteConfigStore: AdsRemoteConfigStore,
    coverState: AdLoadingCoverState,
    routeGate: AppOpenRouteGate,
    innerInterstitialAds: InnerInterstitialAds,
) {
    lifecycleObserver.appOpenPlacement = AdPlacement.AppOpenResume
    lifecycleObserver.appOpenEnabled = {
        remoteConfigStore.current.appOpenOnResume &&
            Constants.splashEnded.value &&
            routeGate.allowsAppOpen()
    }
    lifecycleObserver.appOpenLoadTimeoutMs = { remoteConfigStore.current.appOpenLoadTimeoutMs }
    lifecycleObserver.onAppOpenCoverChanged = { covered ->
        if (covered) {
            coverState.show(AdLoadingCover.WelcomeBack)
        } else {
            coverState.hide(AdLoadingCover.WelcomeBack)
        }
    }
    lifecycleObserver.onAppOpenFinished = { result ->
        if (result == AdResult.Shown) innerInterstitialAds.onOtherFullScreenAdShown()
    }
    lifecycleObserver.register(application)
}
