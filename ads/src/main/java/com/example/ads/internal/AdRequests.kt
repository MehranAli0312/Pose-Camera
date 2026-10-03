package com.example.ads.internal

import android.os.Bundle
import com.example.ads.BannerStyle
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.VideoOptions
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest

internal object AdRequests {

    private const val COLLAPSIBLE_KEY = "collapsible"

    fun fullScreen(adUnitId: String): AdRequest = AdRequest.Builder(adUnitId).build()

    fun banner(adUnitId: String, adSize: AdSize, style: BannerStyle): BannerAdRequest {
        val builder = BannerAdRequest.Builder(adUnitId, adSize)
        when (style) {
            BannerStyle.COLLAPSIBLE_TOP ->
                builder.setGoogleExtrasBundle(Bundle().apply { putString(COLLAPSIBLE_KEY, "top") })

            BannerStyle.COLLAPSIBLE_BOTTOM ->
                builder.setGoogleExtrasBundle(Bundle().apply { putString(COLLAPSIBLE_KEY, "bottom") })

            BannerStyle.ADAPTIVE, BannerStyle.INLINE, BannerStyle.MEDIUM_RECTANGLE -> Unit
        }
        return builder.build()
    }

    fun native(adUnitId: String): NativeAdRequest =
        NativeAdRequest.Builder(adUnitId, listOf(NativeAd.NativeAdType.NATIVE))
            .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
            .build()
}
