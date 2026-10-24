package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.example.ads.AdPlacement

@Stable
class HomeInlineNativeAd internal constructor(
    private val ads: ScreenAds,
) {

    val isEnabled: Boolean
        get() = ads.isSlotEnabled(AdPlacement.HomeInlineNative)

    fun release() = ads.releaseSlot(AdPlacement.HomeInlineNative)
}

@Composable
fun rememberHomeInlineNativeAd(): HomeInlineNativeAd {
    val ads = rememberScreenAds()
    val nativeAd = remember(ads) { HomeInlineNativeAd(ads) }

    DisposableEffect(nativeAd) { onDispose { nativeAd.release() } }

    return nativeAd
}
