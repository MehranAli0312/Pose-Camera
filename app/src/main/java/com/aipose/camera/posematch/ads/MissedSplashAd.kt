package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import java.util.concurrent.atomic.AtomicBoolean
import org.koin.compose.koinInject

class MissedSplashAd {

    private val pending = AtomicBoolean(false)

    private val isShowing = AtomicBoolean(false)

    fun onSplashAdResult(result: AdResult) {
        pending.set(result is AdResult.NotAvailable || result is AdResult.Failed)
    }

    fun clear() {
        pending.set(false)
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
        pending.set(false)
        ads.fullscreen(AdPlacement.SplashFullscreen, continueWhenShown = continueWhenShown) {
            isShowing.set(false)
            onContinue()
        }
    }
}

@Stable
class MissedSplashAdTrigger internal constructor(
    private val missedSplashAd: MissedSplashAd,
    private val ads: ScreenAds,
) {
    fun showThen(continueWhenShown: Boolean = true, onContinue: () -> Unit) =
        missedSplashAd.showThen(ads, continueWhenShown, onContinue)

    fun clear() = missedSplashAd.clear()
}

@Composable
fun rememberMissedSplashAd(): MissedSplashAdTrigger {
    val missedSplashAd: MissedSplashAd = koinInject()
    val ads = rememberScreenAds()

    return remember(missedSplashAd, ads) { MissedSplashAdTrigger(missedSplashAd, ads) }
}
