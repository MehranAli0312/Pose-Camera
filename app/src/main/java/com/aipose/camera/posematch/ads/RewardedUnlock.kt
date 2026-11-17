package com.aipose.camera.posematch.ads

import androidx.activity.ComponentActivity
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdPlacement
import com.example.ads.AdsManager

class RewardedUnlock(
    private val adsManager: AdsManager,
    private val store: AdsRemoteConfigStore,
) {

    suspend fun show(activity: ComponentActivity): Boolean {
        val rewarded = AdPlacement.PremiumRewarded
        if (!adsManager.canShowFullScreenNow(rewarded)) return false
        val loaded = adsManager.awaitOnDemand(rewarded, store.current.rewardedLoadWaitMs)
        if (loaded) {
            val result = adsManager.showOnDemand(activity, rewarded)
            if (result.wasRewarded) return true
            if (result.wasShown) return false
        }
        return showFallback(activity)
    }

    private suspend fun showFallback(activity: ComponentActivity): Boolean {
        val fallback = AdPlacement.RewardedFallbackInterstitial
        if (!adsManager.canShowFullScreenNow(fallback)) return false
        if (!adsManager.awaitOnDemand(fallback, store.current.rewardedFallbackWaitMs)) return false
        return adsManager.showOnDemand(activity, fallback).wasShown
    }
}
