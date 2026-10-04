package com.aipose.camera.posematch.admob_ads.inter

import android.app.Activity
import android.util.Log
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig

// Running count of eligible clicks; the threshold is read LIVE from Remote Config each check.
var interstitialAdCounter = 0

fun interCounterCheck(): Boolean {
    // Frequency cap threshold comes from Remote Config (interAdsCounter) and is read live so a
    // remote change takes effect without an app restart. 0 (or less) disables interstitials.
    val threshold = RemoteConfig.interAdsCounter
    interstitialAdCounter++
    Log.d("interchecks", "interCounterCheck: count=$interstitialAdCounter threshold=$threshold")
    return if (threshold <= 0) false else interstitialAdCounter % threshold == 0
}

fun Activity.showInterWithCounter(
    closeListener: (() -> Unit)? = null,
    failListener: (() -> Unit)? = null,
    showListener: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null
) {
    if (interCounterCheck()) {
        Log.d("interchecks", "interCounterCheck: showSimpleInterstitialAdNew.........")
        InterstitialAdClassNextGen.getInstance()
            .showSimpleInterstitialAdNew(this, closeListener, failListener, showListener, onNext)
    } else closeListener?.invoke()
}

fun Activity.loadSimpleInterstitialAd(adId: String) {
    InterstitialAdClassNextGen.getInstance().loadSimpleInterstitialAd(this, adId)
}

fun Activity.loadSplashInterstitialAd(onResult: (Boolean) -> Unit) {
    InterstitialAdClassNextGen.getInstance().loadSplashInterstitialAd(this, onResult)
}

fun Activity.showSplashInterstitial(
    closeListener: (() -> Unit)? = null,
    failListener: (() -> Unit)? = null,
    showListener: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null
) {
    InterstitialAdClassNextGen.getInstance()
        .showSplashInter(this, closeListener, failListener, showListener, onNext)
}
