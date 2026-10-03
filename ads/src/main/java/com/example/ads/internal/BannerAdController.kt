package com.example.ads.internal

import android.content.Context
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.BannerStyle
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class BannerAdController(
    private val context: Context,
    private val gate: AdGate,
    private val log: AdsLog,
) {

    fun buildRequest(
        placement: AdPlacement,
        style: BannerStyle,
        widthDp: Int,
    ): BannerAdRequest? {
        val adUnitId = gate.resolveRequestableUnit(AdFormat.BANNER, placement) ?: return null
        val adSize = adSizeFor(style, widthDp)
        log.d("Banner request for ${placement.id}: $style ${adSize.width}x${adSize.height}")
        return AdRequests.banner(adUnitId, adSize, style)
    }

    suspend fun loadOnce(placement: AdPlacement, style: BannerStyle): BannerAd? {
        val request = buildRequest(placement, style, screenWidthDp()) ?: return null
        return suspendCancellableCoroutine { continuation ->
            var settled = false
            fun settle(ad: BannerAd?) {
                if (!settled && continuation.isActive) {
                    settled = true
                    continuation.resume(ad)
                } else {
                    ad?.destroy()
                }
            }

            BannerAd.load(
                request,
                object : AdLoadCallback<BannerAd> {
                    override fun onAdLoaded(ad: BannerAd) {
                        onLoaded(placement)
                        settle(ad)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        onFailed(placement, adError.message)
                        settle(null)
                    }
                },
            )

            continuation.invokeOnCancellation { settled = true }
        }
    }

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

    fun onLoaded(placement: AdPlacement) = log.d("Banner loaded for ${placement.id}")

    fun onFailed(placement: AdPlacement, message: String) =
        log.w("Banner failed for ${placement.id}: $message")

    private fun screenWidthDp(): Int = context.resources.configuration.screenWidthDp

    private companion object {
        const val MIN_BANNER_WIDTH_DP = 320
    }
}
