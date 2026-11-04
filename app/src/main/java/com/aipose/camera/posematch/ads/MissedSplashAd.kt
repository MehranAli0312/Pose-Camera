package com.aipose.camera.posematch.ads

import androidx.activity.ComponentActivity
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdPlacement
import java.util.concurrent.atomic.AtomicBoolean

class MissedSplashAd(
    private val store: AdsRemoteConfigStore,
    private val commercialBreakAds: CommercialBreakAds,
) {

    private val pending = AtomicBoolean(false)

    private val isShowing = AtomicBoolean(false)

    private val replayArmed = AtomicBoolean(false)

    private val replayPending = AtomicBoolean(false)

    fun onSplashAdResult(
        ads: ScreenAds,
        wasShown: Boolean,
        hasRetryScreen: Boolean,
        isFirstSession: Boolean,
    ) {
        val missed = hasRetryScreen &&
            !wasShown &&
            ads.isAdsAllowedNow() &&
            ads.styleFor(AdPlacement.SplashFullscreen).format != null
        SplashAdLog.d(
            "missed-check: wasShown=$wasShown hasRetryScreen=$hasRetryScreen " +
                "adsAllowed=${ads.isAdsAllowedNow()} " +
                "format=${ads.styleFor(AdPlacement.SplashFullscreen).format} -> pending=$missed",
        )
        pending.set(missed)
        val config = store.current
        replayArmed.set(isFirstSession && config.showOnboardingScreen && config.onboardInterAd)
        replayPending.set(false)
        if (wasShown) reloadForOnboardEnd(ads)
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
                if (result.wasShown) {
                    clear()
                    reloadForOnboardEnd(ads)
                }
            },
        ) {
            isShowing.set(false)
            onContinue()
        }
    }

    fun onOnboardStarted() {
        replayArmed.set(false)
    }

    fun showAtOnboardEndThen(
        ads: ScreenAds,
        activity: ComponentActivity?,
        onContinue: () -> Unit,
    ) {
        if (pending.get()) {
            showThen(ads, continueWhenShown = true) {
                clear()
                onContinue()
            }
            return
        }
        val canReplay = replayPending.getAndSet(false) &&
            store.current.onboardInterAd &&
            ads.isAdsAllowedNow()
        SplashAdLog.d("onboard-replay: canReplay=$canReplay")
        if (!canReplay || activity == null) {
            onContinue()
            return
        }
        commercialBreakAds.showThen(
            activity = activity,
            placement = AdPlacement.SplashFullscreen,
            onResult = { result -> SplashAdLog.d("onboard-replay result=$result") },
            onContinue = onContinue,
        )
    }

    fun clear() {
        pending.set(false)
    }

    private fun reloadForOnboardEnd(ads: ScreenAds) {
        if (!replayArmed.get()) return
        replayPending.set(true)
        ads.loadOnDemand(AdPlacement.SplashFullscreen)
        SplashAdLog.d("onboard-replay: reloading splash ad for onboarding end")
    }
}
