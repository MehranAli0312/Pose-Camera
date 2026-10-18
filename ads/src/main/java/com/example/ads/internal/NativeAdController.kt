package com.example.ads.internal

import android.os.SystemClock
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine

internal class NativeAdController(
    private val gate: AdGate,
    private val log: AdsLog,
) {

    private val cachedAds = ConcurrentHashMap<String, CachedNativeAd>()

    private val preloading = ConcurrentHashMap<String, CompletableDeferred<Unit>>()

    suspend fun load(placement: AdPlacement): NativeAd? {
        val adUnitId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement) ?: return null

        takeCached(adUnitId)?.let { ad ->
            log.d("Native served from cache for ${placement.id}")
            return ad
        }

        preloading[adUnitId]?.let { inFlight ->
            log.d("Native waiting for in-flight preload for ${placement.id}")
            inFlight.await()
            takeCached(adUnitId)?.let { ad ->
                log.d("Native served from finished preload for ${placement.id}")
                return ad
            }
        }

        return loadDirect(adUnitId, placement)
    }

    suspend fun loadOnce(placement: AdPlacement): NativeAd? {
        val adUnitId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement) ?: return null
        return loadDirect(adUnitId, placement)
    }

    suspend fun preload(placement: AdPlacement) {
        val adUnitId = gate.resolveRequestableUnit(AdFormat.NATIVE, placement) ?: return
        if (hasFreshCached(adUnitId)) return
        val loading = CompletableDeferred<Unit>()
        if (preloading.putIfAbsent(adUnitId, loading) != null) return
        try {
            val ad = loadDirect(adUnitId, placement) ?: return
            cachedAds.put(adUnitId, CachedNativeAd(ad, SystemClock.elapsedRealtime()))
                ?.ad?.destroy()
            log.d("Native cached for ${placement.id}")
        } finally {
            preloading.remove(adUnitId, loading)
            loading.complete(Unit)
        }
    }

    private fun hasFreshCached(adUnitId: String): Boolean {
        val cached = cachedAds[adUnitId] ?: return false
        if (cached.isFresh()) return true
        if (cachedAds.remove(adUnitId, cached)) cached.ad.destroy()
        return false
    }

    private fun takeCached(adUnitId: String): NativeAd? {
        val cached = cachedAds.remove(adUnitId) ?: return null
        if (cached.isFresh()) return cached.ad
        cached.ad.destroy()
        log.d("Cached native expired for $adUnitId")
        return null
    }

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
        cachedAds.values.forEach { cached -> runCatching { cached.ad.destroy() } }
        cachedAds.clear()
    }

    private class CachedNativeAd(val ad: NativeAd, val loadedAt: Long) {
        fun isFresh(): Boolean =
            SystemClock.elapsedRealtime() - loadedAt < CACHED_NATIVE_AD_TTL_MS
    }

    private companion object {
        const val CACHED_NATIVE_AD_TTL_MS = 55L * 60L * 1000L
    }
}
