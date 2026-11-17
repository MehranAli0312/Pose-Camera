package com.example.ads.internal

import android.content.Context
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.BannerStyle
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal class BannerAdController(
    private val context: Context,
    scope: CoroutineScope,
    private val gate: AdGate,
    private val reporter: AdEventReporter,
    private val log: AdsLog,
) {

    private val pool = AdPool<BannerAd>(scope, BannerAd::destroy)

    suspend fun load(
        placement: AdPlacement,
        style: BannerStyle,
        widthDp: Int,
    ): BannerAd? {
        val adUnitId = gate.resolveConfiguredUnit(AdFormat.BANNER, placement) ?: return null
        val adSize = adSizeFor(style, widthDp)
        val key = key(adUnitId, style, adSize)
        val ad = pool.acquire(key) {
            val requestableId = gate.resolveRequestableUnit(AdFormat.BANNER, placement)
                ?: return@acquire null
            loadDirect(placement, requestableId, style, adSize)
        } ?: return null
        attachPlacement(ad, placement)
        return ad
    }

    fun preload(placement: AdPlacement, style: BannerStyle, widthDp: Int = screenWidthDp()) {
        val adUnitId = gate.resolveConfiguredUnit(AdFormat.BANNER, placement) ?: return
        val adSize = adSizeFor(style, widthDp)
        val key = key(adUnitId, style, adSize)
        pool.prefetch(key) {
            val requestableId = gate.resolveRequestableUnit(AdFormat.BANNER, placement)
                ?: return@prefetch null
            loadDirect(placement, requestableId, style, adSize)
        }
    }

    fun recycle(ad: BannerAd) {
        pool.recycle(ad)
    }

    fun loadedAt(ad: BannerAd): Long? = pool.loadedAt(ad)

    fun adSizeFor(style: BannerStyle, widthDp: Int): AdSize {
        val width = widthDp.coerceAtLeast(MIN_BANNER_WIDTH_DP)
        return when (style) {
            BannerStyle.INLINE ->
                AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, width)

            BannerStyle.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE

            BannerStyle.ADAPTIVE,
            BannerStyle.COLLAPSIBLE_TOP,
            BannerStyle.COLLAPSIBLE_BOTTOM,
                -> AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)
        }
    }

    fun screenWidthDp(): Int = context.resources.configuration.screenWidthDp

    fun destroy() {
        pool.clear()
    }

    private suspend fun loadDirect(
        placement: AdPlacement,
        adUnitId: String,
        style: BannerStyle,
        adSize: AdSize,
    ): BannerAd? = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            var settled = false
            fun settle(ad: BannerAd?) {
                if (!settled && continuation.isActive) {
                    settled = true
                    continuation.resume(ad)
                } else {
                    ad?.destroy()
                }
            }

            log.d("Banner request for ${placement.id}: $style ${adSize.width}x${adSize.height}")
            BannerAd.load(
                AdRequests.banner(adUnitId, adSize, style),
                object : AdLoadCallback<BannerAd> {
                    override fun onAdLoaded(ad: BannerAd) {
                        log.d("Banner loaded for ${placement.id}")
                        reporter.loadResult(loaded = true, AdFormat.BANNER, placement)
                        settle(ad)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        log.w("Banner failed for ${placement.id}: ${adError.message}")
                        reporter.loadResult(loaded = false, AdFormat.BANNER, placement)
                        settle(null)
                    }
                },
            )

            continuation.invokeOnCancellation { settled = true }
        }
    }

    private fun attachPlacement(ad: BannerAd, placement: AdPlacement) {
        ad.adEventCallback = reporter.bannerCallback(placement) { pool.markImpressed(ad) }
    }

    private fun key(adUnitId: String, style: BannerStyle, size: AdSize): String =
        "$adUnitId:${style.name}:${size.width}x${size.height}"

    private companion object {
        const val MIN_BANNER_WIDTH_DP = 320
    }
}
