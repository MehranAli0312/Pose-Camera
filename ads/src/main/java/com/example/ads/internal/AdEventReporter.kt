package com.example.ads.internal

import com.example.ads.AdEvent
import com.example.ads.AdEventListener
import com.example.ads.AdEventType
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback

internal class AdEventReporter(
    private val listener: AdEventListener,
    private val log: AdsLog,
) {

    fun report(type: AdEventType, format: AdFormat, placement: AdPlacement) {
        runCatching { listener.onAdEvent(AdEvent(type, format, placement)) }
            .onFailure { error -> log.w("Ad event $type failed for ${placement.id}", error) }
    }

    fun loadResult(loaded: Boolean, format: AdFormat, placement: AdPlacement) =
        report(if (loaded) AdEventType.LOADED else AdEventType.LOAD_FAILED, format, placement)

    fun bannerCallback(placement: AdPlacement): BannerAdEventCallback =
        object : BannerAdEventCallback {
            override fun onAdImpression() = report(AdEventType.SHOWN, AdFormat.BANNER, placement)
            override fun onAdClicked() = report(AdEventType.CLICKED, AdFormat.BANNER, placement)
        }

    fun nativeCallback(placement: AdPlacement): NativeAdEventCallback =
        object : NativeAdEventCallback {
            override fun onAdImpression() = report(AdEventType.SHOWN, AdFormat.NATIVE, placement)
            override fun onAdClicked() = report(AdEventType.CLICKED, AdFormat.NATIVE, placement)
        }
}
