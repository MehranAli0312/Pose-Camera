package com.aipose.camera.posematch.ads

import com.example.ads.AdPlacement
import java.util.concurrent.atomic.AtomicBoolean

class MissedSplashAd {

    private val pending = AtomicBoolean(false)

    private val isShowing = AtomicBoolean(false)

    fun onSplashAdResult(ads: ScreenAds, wasShown: Boolean, isFirstSession: Boolean) {
        val missed = isFirstSession &&
            !wasShown &&
            ads.isAdsAllowedNow() &&
            ads.styleFor(AdPlacement.SplashFullscreen).format != null
        SplashAdLog.d(
            "missed-check: wasShown=$wasShown isFirstSession=$isFirstSession " +
                "adsAllowed=${ads.isAdsAllowedNow()} " +
                "format=${ads.styleFor(AdPlacement.SplashFullscreen).format} -> pending=$missed",
        )
        pending.set(missed)
    }

    fun showThen(ads: ScreenAds, continueWhenShown: Boolean, onContinue: () -> Unit) {
        if (isShowing.get()) return
        val canShow = pending.get() &&
            ads.isAdsAllowedNow() &&
            ads.isLoadedOnDemand(AdPlacement.SplashFullscreen)
        SplashAdLog.d(
            "missed-retry: pending=${pending.get()} adsAllowed=${ads.isAdsAllowedNow()} " +
                "loaded=${ads.isLoadedOnDemand(AdPlacement.SplashFullscreen)} -> canShow=$canShow",
        )
        if (!canShow) {
            onContinue()
            return
        }
        if (!isShowing.compareAndSet(false, true)) return
        ads.fullscreen(
            placement = AdPlacement.SplashFullscreen,
            continueWhenShown = continueWhenShown,
            onResult = { result ->
                SplashAdLog.d("missed-retry result=$result")
                if (result.wasShown) clear()
            },
        ) {
            isShowing.set(false)
            onContinue()
        }
    }

    fun clear() {
        pending.set(false)
    }
}
