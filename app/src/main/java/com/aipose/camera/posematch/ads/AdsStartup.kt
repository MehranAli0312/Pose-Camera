package com.aipose.camera.posematch.ads

import android.app.Application
import com.example.ads.AdPlacement
import com.example.ads.AdsManager
import com.example.ads.lifecycle.AdsAppLifecycleObserver
import com.example.common.Constants
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore

fun splashAdsLoads(adsManager: AdsManager) {
    adsManager.keepWarm(listOf(AdPlacement.SplashFullscreen))
}

fun onPlacementConfigChanged(adsManager: AdsManager) {
    adsManager.revalidateWarmUp()
}

fun registerAppOpenAds(
    application: Application,
    lifecycleObserver: AdsAppLifecycleObserver,
    remoteConfigStore: AdsRemoteConfigStore,
    loaderState: AppOpenLoaderState,
    routeGate: AppOpenRouteGate,
) {
    lifecycleObserver.appOpenPlacement = AdPlacement.AppOpenResume
    lifecycleObserver.appOpenEnabled = {
        remoteConfigStore.current.appOpenOnResume &&
            Constants.splashEnded.value &&
            routeGate.allowsAppOpen()
    }
    lifecycleObserver.appOpenLoadTimeoutMs = { remoteConfigStore.current.appOpenLoadTimeoutMs }
    lifecycleObserver.onAppOpenCoverChanged = loaderState::setVisible
    lifecycleObserver.register(application)
}
