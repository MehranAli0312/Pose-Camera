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
        pending.set(missed)
        if (!missed) ads.coolDown(AdPlacement.SplashFullscreen)
    }

    fun showThen(ads: ScreenAds, continueWhenShown: Boolean, onContinue: () -> Unit) {
        if (isShowing.get()) return
        val canShow = pending.get() &&
            ads.isAdsAllowedNow() &&
            ads.isAvailable(AdPlacement.SplashFullscreen)
        if (!canShow) {
            onContinue()
            return
        }
        if (!isShowing.compareAndSet(false, true)) return
        ads.fullscreen(
            placement = AdPlacement.SplashFullscreen,
            continueWhenShown = continueWhenShown,
            preloadedOnly = true,
            onResult = { result -> if (result.wasShown) clear(ads) },
        ) {
            isShowing.set(false)
            onContinue()
        }
    }

    fun clear(ads: ScreenAds) {
        if (pending.getAndSet(false)) ads.coolDown(AdPlacement.SplashFullscreen)
    }
}
