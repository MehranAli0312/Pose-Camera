package com.aipose.camera.posematch.ads

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.koinInject

@Stable
class MissedSplashAdTrigger internal constructor(
    private val missedSplashAd: MissedSplashAd,
    private val ads: ScreenAds,
    private val activity: ComponentActivity?,
) {
    fun showThen(continueWhenShown: Boolean = true, onContinue: () -> Unit) =
        missedSplashAd.showThen(ads, continueWhenShown, onContinue)

    fun onOnboardStarted() = missedSplashAd.onOnboardStarted()

    fun showAtOnboardEndThen(onContinue: () -> Unit) =
        missedSplashAd.showAtOnboardEndThen(ads, activity, onContinue)

    fun clear() = missedSplashAd.clear()
}

@Composable
fun rememberMissedSplashAd(): MissedSplashAdTrigger {
    val missedSplashAd: MissedSplashAd = koinInject()
    val ads = rememberScreenAds()
    val context = LocalContext.current

    return remember(missedSplashAd, ads, context) {
        MissedSplashAdTrigger(missedSplashAd, ads, context.findComponentActivity())
    }
}
