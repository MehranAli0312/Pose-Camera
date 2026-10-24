package com.example.ads.internal

import android.app.Activity
import android.os.SystemClock
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal class FullScreenAdController(
    private val scope: CoroutineScope,
    private val gate: AdGate,
    private val state: AdsRuntimeState,
    private val log: AdsLog,
) {

    private val onDemandAds = ConcurrentHashMap<String, OnDemandAd>()

    private val onDemandLoads = ConcurrentHashMap<String, Deferred<Boolean>>()

    suspend fun loadOnDemand(format: AdFormat, placement: AdPlacement) {
        awaitOnDemand(format, placement)
    }

    suspend fun awaitOnDemand(format: AdFormat, placement: AdPlacement): Boolean {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return false
        val key = key(format, adUnitId)
        if (freshOnDemandAd(key) != null) return true
        return sharedOnDemandLoad(key, format, adUnitId, placement).await()
    }

    private fun sharedOnDemandLoad(
        key: String,
        format: AdFormat,
        adUnitId: String,
        placement: AdPlacement,
    ): Deferred<Boolean> {
        onDemandLoads[key]?.let { inFlight ->
            log.d("$format on-demand load already running for ${placement.id}, joining it")
            return inFlight
        }
        val load = scope.async(start = CoroutineStart.LAZY) {
            loadAndKeepOnDemand(key, format, adUnitId, placement)
        }
        val inFlight = onDemandLoads.putIfAbsent(key, load)
        if (inFlight != null) {
            load.cancel()
            return inFlight
        }
        load.invokeOnCompletion { onDemandLoads.remove(key, load) }
        load.start()
        return load
    }

    private suspend fun loadAndKeepOnDemand(
        key: String,
        format: AdFormat,
        adUnitId: String,
        placement: AdPlacement,
    ): Boolean = try {
        val ad = loadDirect(format, adUnitId)
        if (ad != null) {
            onDemandAds[key] = OnDemandAd(ad, SystemClock.elapsedRealtime())
            log.d("$format loaded on demand for ${placement.id}")
        }
        ad != null
    } catch (error: Throwable) {
        log.w("$format on-demand load failed for ${placement.id}", error)
        false
    }

    fun isLoadedOnDemand(format: AdFormat, placement: AdPlacement): Boolean {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return false
        return freshOnDemandAd(key(format, adUnitId)) != null
    }

    fun isLoadingOnDemand(format: AdFormat, placement: AdPlacement): Boolean {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return false
        return onDemandLoads[key(format, adUnitId)]?.isActive == true
    }

    suspend fun awaitLoadInFlight(format: AdFormat, placement: AdPlacement): Boolean {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return false
        val key = key(format, adUnitId)
        if (freshOnDemandAd(key) != null) return true
        val inFlight = onDemandLoads[key] ?: return false
        return inFlight.await()
    }

    fun canShowNow(format: AdFormat): Boolean = gate.canShowFullScreenNow(format)

    suspend fun showOnDemand(
        activity: Activity,
        format: AdFormat,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
    ): AdResult {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return AdResult.NotEligible

        val key = key(format, adUnitId)
        if (freshOnDemandAd(key) == null) return AdResult.NotAvailable

        if (format == AdFormat.INTERSTITIAL && !gate.passesInterstitialFrequency()) {
            return AdResult.NotEligible
        }

        if (!gate.tryAcquireFullScreen(format)) return AdResult.NotEligible

        return try {
            val ad = onDemandAds.remove(key)?.ad ?: return AdResult.NotAvailable
            present(ad, activity, format, onShown)
        } catch (error: Throwable) {
            log.w("Showing on-demand $format failed", error)
            AdResult.Failed(error.message ?: "unknown error")
        } finally {
            state.releaseFullScreen()
        }
    }

    suspend fun loadAndShow(
        activity: Activity,
        format: AdFormat,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
        loadTimeoutMs: Long? = null,
        onLoadStarted: (() -> Unit)? = null,
    ): AdResult {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return AdResult.NotEligible
        val key = key(format, adUnitId)
        if (freshOnDemandAd(key) == null) {
            onLoadStarted?.invoke()
            val load = sharedOnDemandLoad(key, format, adUnitId, placement)
            val loaded = if (loadTimeoutMs == null) {
                load.await()
            } else {
                withTimeoutOrNull(loadTimeoutMs) { load.await() } ?: false
            }
            if (!loaded) return AdResult.NotAvailable
        }
        return showOnDemand(activity, format, placement, onShown)
    }

    fun destroy() {
        onDemandAds.clear()
        onDemandLoads.values.forEach { load -> load.cancel() }
        onDemandLoads.clear()
    }

    private suspend fun loadDirect(format: AdFormat, adUnitId: String): Any? {
        val request = AdRequests.fullScreen(adUnitId)
        val result: AdLoadResult<*> = when (format) {
            AdFormat.INTERSTITIAL -> InterstitialAd.load(request)
            AdFormat.REWARDED -> RewardedAd.load(request)
            AdFormat.APP_OPEN -> AppOpenAd.load(request)
            AdFormat.BANNER, AdFormat.NATIVE -> return null
        }

        return when (result) {
            is AdLoadResult.Success -> result.ad
            is AdLoadResult.Failure -> {
                log.w("$format direct load failed: ${result.error.message}")
                null
            }
        }
    }

    private suspend fun present(
        ad: Any,
        activity: Activity,
        format: AdFormat,
        onShown: (() -> Unit)? = null,
    ): AdResult {
        if (!withContext(Dispatchers.Main.immediate) { canPresentOn(activity) }) {
            log.d("$format blocked: app is not in foreground")
            return AdResult.NotEligible
        }

        val presentation = AdPresentation(
            onShownCallback = {
                state.markFullScreenVisible()
                onShown?.invoke()
            },
        )

        when (ad) {
            is InterstitialAd -> {
                ad.adEventCallback = object : InterstitialAdEventCallback {
                    override fun onAdShowedFullScreenContent() = presentation.onShown()
                    override fun onAdDismissedFullScreenContent() = presentation.onDismissed()
                    override fun onAdFailedToShowFullScreenContent(
                        fullScreenContentError: FullScreenContentError,
                    ) = presentation.onFailed(fullScreenContentError.message)
                }
                withContext(Dispatchers.Main.immediate) { ad.show(activity) }
            }

            is RewardedAd -> {
                ad.adEventCallback = object : RewardedAdEventCallback {
                    override fun onAdShowedFullScreenContent() = presentation.onShown()
                    override fun onAdDismissedFullScreenContent() = presentation.onDismissed()
                    override fun onAdFailedToShowFullScreenContent(
                        fullScreenContentError: FullScreenContentError,
                    ) = presentation.onFailed(fullScreenContentError.message)
                }
                withContext(Dispatchers.Main.immediate) {
                    ad.show(activity) { item -> presentation.onReward(item.type, item.amount) }
                }
            }

            is AppOpenAd -> {
                ad.adEventCallback = object : AppOpenAdEventCallback {
                    override fun onAdShowedFullScreenContent() = presentation.onShown()
                    override fun onAdDismissedFullScreenContent() = presentation.onDismissed()
                    override fun onAdFailedToShowFullScreenContent(
                        fullScreenContentError: FullScreenContentError,
                    ) = presentation.onFailed(fullScreenContentError.message)
                }
                withContext(Dispatchers.Main.immediate) { ad.show(activity) }
            }

            else -> return AdResult.NotAvailable
        }

        val result = presentation.await()
        log.d("$format finished with $result")
        return result
    }

    private fun canPresentOn(activity: Activity): Boolean =
        state.foreground.value && !activity.isFinishing && !activity.isDestroyed

    private fun freshOnDemandAd(key: String): OnDemandAd? {
        val loaded = onDemandAds[key] ?: return null
        if (SystemClock.elapsedRealtime() - loaded.loadedAt < ON_DEMAND_AD_TTL_MS) return loaded
        onDemandAds.remove(key, loaded)
        log.d("On-demand ad expired for $key")
        return null
    }

    private fun key(format: AdFormat, id: String) = "${format.name}:$id"

    private class OnDemandAd(val ad: Any, val loadedAt: Long)

    private companion object {
        const val ON_DEMAND_AD_TTL_MS = 55L * 60L * 1000L
    }
}
