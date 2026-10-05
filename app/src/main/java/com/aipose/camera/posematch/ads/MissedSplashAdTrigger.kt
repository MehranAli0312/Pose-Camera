package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject

@Stable
class MissedSplashAdTrigger internal constructor(
    private val missedSplashAd: MissedSplashAd,
    private val ads: ScreenAds,
) {
    fun showThen(continueWhenShown: Boolean = true, onContinue: () -> Unit) =
        missedSplashAd.showThen(ads, continueWhenShown, onContinue)

    fun clear() = missedSplashAd.clear(ads)
}

@Composable
fun rememberMissedSplashAd(): MissedSplashAdTrigger {
    val missedSplashAd: MissedSplashAd = koinInject()
    val ads = rememberScreenAds()

    return remember(missedSplashAd, ads) { MissedSplashAdTrigger(missedSplashAd, ads) }
}
