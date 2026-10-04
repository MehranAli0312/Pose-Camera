package com.aipose.camera.posematch.admob_ads

import android.content.Context
import android.util.Log
import com.aipose.camera.posematch.AppClass
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig
import com.aipose.camera.posematch.ui.util.isNetworkAvailable
import com.google.android.libraries.ads.mobile.sdk.internal.signals.AppStats
import com.gpsmapcamera.location.timestamp.adsdemo.presentation.subscription.PremiumPrefs.isPremiumUser

fun Context.canShowAds(remoteConfig: Boolean): Boolean {
    val networkAvailable = isNetworkAvailable()
    val sdkInitialized = AppClass.instance.isMobileAdsInitialized.get()
    val allAdsEnabled = RemoteConfig.enabledAllAds

    Log.d("okayIsInitialized", "canShowAds: sdkInitialized = $sdkInitialized")
    Log.d("okayIsInitialized", "canShowAds: networkAvailable = $networkAvailable")
    Log.d("okayIsInitialized", "canShowAds: remoteConfig = $remoteConfig")
    Log.d("okayIsInitialized", "canShowAds: enabledAllAds = $allAdsEnabled")

    return networkAvailable &&
            remoteConfig &&
            allAdsEnabled &&  !isPremiumUser(AppClass.instance.applicationContext) &&
            sdkInitialized
}

fun Context.simpleCanShowAds(remoteConfig: Boolean): Boolean {
    val networkAvailable = isNetworkAvailable()
    val allAdsEnabled = RemoteConfig.enabledAllAds

    Log.d("simpleCanShowAds", "networkAvailable = $networkAvailable")
    Log.d("simpleCanShowAds", "remoteConfig = $remoteConfig")
    Log.d("simpleCanShowAds", "enabledAllAds = $allAdsEnabled")

    return networkAvailable &&
            remoteConfig && !isPremiumUser(AppClass.instance.applicationContext) &&
            allAdsEnabled
}

fun Context.ensureMobileAdsSdkInitialized(
    tag: String,
    caller: String,
    onInitialized: (() -> Unit)? = null
): Boolean {
    if (AppClass.instance.isMobileAdsInitialized.get()) {
        onInitialized?.invoke()
        return true
    }

    Log.d(tag, "$caller: SDK not initialized, starting initialization")

    AppClass.instance.initializeMobileAdsSdk {
        Log.d(tag, "$caller: SDK initialized callback received")
        onInitialized?.invoke()
    }

    return false
}
