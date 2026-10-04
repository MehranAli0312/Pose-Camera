package com.aipose.camera.posematch.admob_ads.native_ad

import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.aipose.camera.posematch.admob_ads.ensureMobileAdsSdkInitialized

const val nativeAdFlow = "nativeAdFlow"

fun loadNativeWithHighFloorFallback(
    context: Context,
    normalUnit: String,
    highFloorUnit: String,
    enableHighFloor: Boolean,
    cacheKey: String = normalUnit,
    consumeCached: Boolean = true,
    adResult: ((NativeAd?) -> Unit)
) {
    if (!enableHighFloor) {
        loadAndReturnAdNextGen(
            context = context,
            nativeId = normalUnit,
            cacheKey = cacheKey,
            consumeCached = consumeCached,
            adResult = adResult
        )
        return
    }

    loadAndReturnAdNextGen(
        context = context,
        nativeId = highFloorUnit,
        cacheKey = cacheKey,
        consumeCached = consumeCached,
    ) { highFloorAd ->
        if (highFloorAd != null) {
            adResult.invoke(highFloorAd)
        } else {
            loadAndReturnAdNextGen(
                context = context,
                nativeId = normalUnit,
                cacheKey = cacheKey,
                consumeCached = consumeCached,
                adResult = adResult
            )
        }
    }
}

fun loadAndReturnAdNextGen(
    context: Context,
    nativeId: String,
    cacheKey: String = nativeId,
    consumeCached: Boolean = true,
    adResult: ((NativeAd?) -> Unit)
) {
    if (consumeCached) {
        NativeAdCache.getOnce(cacheKey)?.let {
            adResult.invoke(it)
            return
        }
    }

    val adRequest = NativeAdRequest.Builder(nativeId, listOf(NativeAd.NativeAdType.NATIVE)).build()

    if (!context.ensureMobileAdsSdkInitialized(nativeAdFlow, "loadAndReturnAdNextGen")) {
        adResult.invoke(null)
        return
    }

    NativeAdLoader.load( adRequest, object : NativeAdLoaderCallback {
        override fun onNativeAdLoaded(nativeAd: NativeAd) {
            adResult.invoke(nativeAd)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
            Log.e(nativeAdFlow, "onAdFailedToLoad: ${adError.message}")
            adResult.invoke(null)
        }
    })
}
