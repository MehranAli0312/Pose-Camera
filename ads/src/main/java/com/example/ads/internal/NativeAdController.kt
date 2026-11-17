package com.example.ads.internal

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal class NativeAdController(
    scope: CoroutineScope,
    private val gate: AdGate,
    private val reporter: AdEventReporter,
    private val log: AdsLog,
) {

    private val pool = AdPool<NativeAd>(scope, NativeAd::destroy)

    suspend fun load(placement: AdPlacement): NativeAd? {
        val adUnitId = gate.resolveConfiguredUnit(AdFormat.NATIVE, placement) ?: return null
        val ad = pool.acquire(adUnitId) {
            val requestableId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement)
                ?: return@acquire null
            loadDirect(requestableId, placement)
        } ?: return null
        attachPlacement(ad, placement)
        return ad
    }

    fun preload(placement: AdPlacement) {
        val adUnitId = gate.resolveConfiguredUnit(AdFormat.NATIVE, placement) ?: return
        pool.prefetch(adUnitId) {
            val requestableId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement)
                ?: return@prefetch null
            loadDirect(requestableId, placement)
        }
    }

    fun recycle(ad: NativeAd) {
        pool.recycle(ad)
    }

    fun loadedAt(ad: NativeAd): Long? = pool.loadedAt(ad)

    fun destroy() {
        pool.clear()
    }

    private suspend fun loadDirect(adUnitId: String, placement: AdPlacement): NativeAd? =
        withContext(Dispatchers.Main.immediate) {
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
                            reporter.loadResult(loaded = true, AdFormat.NATIVE, placement)
                            settle(nativeAd)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            log.w("Native failed for ${placement.id}: ${adError.message}")
                            reporter.loadResult(loaded = false, AdFormat.NATIVE, placement)
                            settle(null)
                        }
                    },
                )

                continuation.invokeOnCancellation { settled = true }
            }
        }

    private fun attachPlacement(ad: NativeAd, placement: AdPlacement) {
        ad.adEventCallback = reporter.nativeCallback(placement) { pool.markImpressed(ad) }
    }
}
