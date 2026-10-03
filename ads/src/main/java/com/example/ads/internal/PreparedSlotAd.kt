package com.example.ads.internal

import com.example.ads.NativeAdDesign
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd

internal sealed interface PreparedSlotAd {

    data object Empty : PreparedSlotAd

    data class Loading(val heightDp: Int) : PreparedSlotAd

    class Banner(
        val ad: BannerAd,
        val widthDp: Int,
        val heightDp: Int,
        val loadedAt: Long,
    ) : PreparedSlotAd

    class Native(val ad: NativeAd, val design: NativeAdDesign, val loadedAt: Long) : PreparedSlotAd
}
