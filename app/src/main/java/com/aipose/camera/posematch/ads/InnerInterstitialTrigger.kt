package com.aipose.camera.posematch.ads

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.koinInject

@Stable
class InnerInterstitialTrigger internal constructor(
    private val ads: InnerInterstitialAds,
    private val activity: ComponentActivity?,
) {
    fun showThen(onContinue: () -> Unit) = ads.showThen(activity, onContinue)
}

@Composable
fun rememberInnerInterstitial(): InnerInterstitialTrigger {
    val ads: InnerInterstitialAds = koinInject()
    val context = LocalContext.current

    return remember(ads, context) {
        InnerInterstitialTrigger(ads, context.findComponentActivity())
    }
}
