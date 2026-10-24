package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.example.ads.AdPlacement

@Stable
class PoseAlbumNativeAds internal constructor(
    private val ads: ScreenAds,
) {

    val isEnabled: Boolean
        get() = ads.isSlotEnabled(AdPlacement.PoseAlbumInlineFirst)

    fun prepareSecond() = ads.prepareSlot(AdPlacement.PoseAlbumInlineSecond)

    fun release() {
        ads.releaseSlot(AdPlacement.PoseAlbumInlineFirst)
        ads.releaseSlot(AdPlacement.PoseAlbumInlineSecond)
    }
}

@Composable
fun rememberPoseAlbumNativeAds(): PoseAlbumNativeAds {
    val ads = rememberScreenAds()
    val nativeAds = remember(ads) { PoseAlbumNativeAds(ads) }

    DisposableEffect(nativeAds) { onDispose { nativeAds.release() } }

    return nativeAds
}
