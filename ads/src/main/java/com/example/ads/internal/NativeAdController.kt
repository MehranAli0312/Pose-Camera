package com.example.ads.internal

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoadResult
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdPreloader
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class NativeAdController(
    private val gate: AdGate,
    private val config: com.example.ads.AdsConfig,
    private val log: AdsLog,
) {

    private val preloadingUnits = ConcurrentHashMap.newKeySet<String>()

    suspend fun load(placement: AdPlacement): NativeAd? {
        val adUnitId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement) ?: return null

        startPreloadingIfNeeded(adUnitId)

        pollPreloaded(adUnitId)?.let { ad ->
            log.d("Native served from preload buffer for ${placement.id}")
            return ad
        }

        return loadDirect(adUnitId, placement)
    }

    suspend fun loadOnce(placement: AdPlacement): NativeAd? {
        val adUnitId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement) ?: return null
        return loadDirect(adUnitId, placement)
    }

    fun preload(placement: AdPlacement) {
        val adUnitId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement) ?: return
        startPreloadingIfNeeded(adUnitId)
    }

    fun startPreloadingIfNeeded(adUnitId: String) {
        if (!preloadingUnits.add(adUnitId)) return
        runCatching {
            NativeAdPreloader.start(
                adUnitId,
                PreloadConfiguration(AdRequests.native(adUnitId), config.preloadBufferSize),
            )
        }.onFailure {
            preloadingUnits.remove(adUnitId)
            log.w("Native preload could not start for $adUnitId", it)
        }
    }

    private fun pollPreloaded(adUnitId: String): NativeAd? = runCatching {
        (NativeAdPreloader.pollAd(adUnitId) as? NativeAdLoadResult.NativeAdSuccess)?.ad
    }.getOrNull()

    private suspend fun loadDirect(adUnitId: String, placement: AdPlacement): NativeAd? =
        suspendCancellableCoroutine { continuation ->
            var settled = false
            fun settle(ad: NativeAd?) {
                if (!settled && continuation.isActive) {
                    settled = true
                    continuation.resume(ad)
                } else {
                    ad?.destroy()
                }
            }

            NativeAdLoader.load(
                AdRequests.native(adUnitId),
                object : NativeAdLoaderCallback {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        log.d("Native loaded for ${placement.id}")
                        settle(nativeAd)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        log.w("Native failed for ${placement.id}: ${adError.message}")
                        settle(null)
                    }
                },
            )

            continuation.invokeOnCancellation { settled = true }
        }

    fun destroy() {
        runCatching { NativeAdPreloader.destroyAll() }
        preloadingUnits.clear()
    }
}
