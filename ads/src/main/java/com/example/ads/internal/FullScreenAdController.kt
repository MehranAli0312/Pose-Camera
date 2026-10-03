package com.example.ads.internal

import android.app.Activity
import android.os.SystemClock
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdsConfig
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdPreloader
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdPreloader
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FullScreenAdController(
    private val config: AdsConfig,
    private val gate: AdGate,
    private val state: AdsRuntimeState,
    private val log: AdsLog,
) {

    private val preloadingUnits = ConcurrentHashMap.newKeySet<String>()

    private val onDemandAds = ConcurrentHashMap<String, OnDemandAd>()

    private val onDemandLoading = ConcurrentHashMap.newKeySet<String>()

    // Which preloader each placement relies on, so it can be released when the placement turns off.
    private val preloadTargets = ConcurrentHashMap<String, PreloadTarget>()

    fun startPreloading(format: AdFormat, placement: AdPlacement) {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return
        val target = PreloadTarget(format, adUnitId)
        val previous = preloadTargets.put(placement.id, target)
        if (previous != null && previous != target) releasePreloader(previous)
        if (!preloadingUnits.add(key(format, adUnitId))) return

        val configuration =
            PreloadConfiguration(AdRequests.fullScreen(adUnitId), config.preloadBufferSize)

        val started = runCatching {
            when (format) {
                AdFormat.INTERSTITIAL -> InterstitialAdPreloader.start(adUnitId, configuration)
                AdFormat.REWARDED -> RewardedAdPreloader.start(adUnitId, configuration)
                AdFormat.REWARDED_INTERSTITIAL ->
                    RewardedInterstitialAdPreloader.start(adUnitId, configuration)

                AdFormat.APP_OPEN -> AppOpenAdPreloader.start(adUnitId, configuration)
                AdFormat.BANNER, AdFormat.NATIVE -> false
            }
        }.getOrElse { error ->
            log.w("Preload failed to start for $format", error)
            false
        }

        if (!started) {
            preloadingUnits.remove(key(format, adUnitId))
            preloadTargets.remove(placement.id, target)
        }
        log.d("Preloading $format on $adUnitId: $started")
    }

    fun stopPreloading(placement: AdPlacement) {
        val target = preloadTargets.remove(placement.id) ?: return
        releasePreloader(target)
    }

    // Destroying a preloader drops its buffered ads, so it only happens once no placement uses it.
    private fun releasePreloader(target: PreloadTarget) {
        if (preloadTargets.containsValue(target)) return
        if (!preloadingUnits.remove(key(target.format, target.adUnitId))) return
        val destroyed = runCatching {
            when (target.format) {
                AdFormat.INTERSTITIAL -> InterstitialAdPreloader.destroy(target.adUnitId)
                AdFormat.REWARDED -> RewardedAdPreloader.destroy(target.adUnitId)
                AdFormat.REWARDED_INTERSTITIAL ->
                    RewardedInterstitialAdPreloader.destroy(target.adUnitId)

                AdFormat.APP_OPEN -> AppOpenAdPreloader.destroy(target.adUnitId)
                AdFormat.BANNER, AdFormat.NATIVE -> false
            }
        }.getOrElse { error ->
            log.w("Preload failed to stop for ${target.format}", error)
            false
        }
        log.d("Stopped preloading ${target.format} on ${target.adUnitId}: $destroyed")
    }

    fun isAvailable(format: AdFormat, placement: AdPlacement): Boolean {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return false
        return runCatching {
            when (format) {
                AdFormat.INTERSTITIAL -> InterstitialAdPreloader.isAdAvailable(adUnitId)
                AdFormat.REWARDED -> RewardedAdPreloader.isAdAvailable(adUnitId)
                AdFormat.REWARDED_INTERSTITIAL ->
                    RewardedInterstitialAdPreloader.isAdAvailable(adUnitId)

                AdFormat.APP_OPEN -> AppOpenAdPreloader.isAdAvailable(adUnitId)
                AdFormat.BANNER, AdFormat.NATIVE -> false
            }
        }.getOrDefault(false)
    }

    suspend fun loadOnDemand(format: AdFormat, placement: AdPlacement) {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return
        // Keyed by ad unit so placements sharing a unit share one loaded ad.
        val key = key(format, adUnitId)
        if (freshOnDemandAd(key) != null) return
        if (!onDemandLoading.add(key)) return
        try {
            val ad = loadDirect(format, adUnitId) ?: return
            onDemandAds[key] = OnDemandAd(ad, SystemClock.elapsedRealtime())
            log.d("$format loaded on demand for ${placement.id}")
        } catch (error: Throwable) {
            log.w("$format on-demand load failed for ${placement.id}", error)
        } finally {
            onDemandLoading.remove(key)
        }
    }

    fun isLoadedOnDemand(format: AdFormat, placement: AdPlacement): Boolean {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return false
        return freshOnDemandAd(key(format, adUnitId)) != null
    }

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

    suspend fun showPreloaded(
        activity: Activity,
        format: AdFormat,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
    ): AdResult {
        val adUnitId = gate.resolveRequestableUnit(format, placement) ?: return AdResult.NotEligible
        if (!isAvailable(format, placement)) return AdResult.NotAvailable

        if (format == AdFormat.INTERSTITIAL && !gate.passesInterstitialFrequency()) {
            return AdResult.NotEligible
        }

        if (!gate.tryAcquireFullScreen(format)) return AdResult.NotEligible

        return try {
            val ad = pollPreloaded(format, adUnitId) ?: return AdResult.NotAvailable
            present(ad, activity, format, onShown)
        } catch (error: Throwable) {
            log.w("Showing preloaded $format failed", error)
            AdResult.Failed(error.message ?: "unknown error")
        } finally {
            state.releaseFullScreen()
        }
    }

    fun destroy() {
        onDemandAds.clear()
        onDemandLoading.clear()
        runCatching { InterstitialAdPreloader.destroyAll() }
        runCatching { RewardedAdPreloader.destroyAll() }
        runCatching { RewardedInterstitialAdPreloader.destroyAll() }
        runCatching { AppOpenAdPreloader.destroyAll() }
        preloadingUnits.clear()
        preloadTargets.clear()
    }

    suspend fun show(
        activity: Activity,
        format: AdFormat,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
    ): AdResult {
        gate.resolveRequestableUnit(format, placement) ?: return AdResult.NotEligible

        if (format == AdFormat.INTERSTITIAL && !gate.passesInterstitialFrequency()) {
            return AdResult.NotEligible
        }

        if (!gate.tryAcquireFullScreen(format)) return AdResult.NotEligible

        return try {
            startPreloading(format, placement)
            val adUnitId = gate.resolveRequestableUnit(format, placement)
                ?: return AdResult.NotEligible
            val ad = obtainAd(format, adUnitId) ?: return AdResult.NotAvailable
            present(ad, activity, format, onShown)
        } catch (error: Throwable) {
            log.w("Showing $format failed", error)
            AdResult.Failed(error.message ?: "unknown error")
        } finally {
            state.releaseFullScreen()
        }
    }

    private suspend fun obtainAd(format: AdFormat, adUnitId: String): Any? {
        pollPreloaded(format, adUnitId)?.let { return it }
        return loadDirect(format, adUnitId)
    }

    private suspend fun loadDirect(format: AdFormat, adUnitId: String): Any? {
        val request = AdRequests.fullScreen(adUnitId)
        val result: AdLoadResult<*> = when (format) {
            AdFormat.INTERSTITIAL -> InterstitialAd.load(request)
            AdFormat.REWARDED -> RewardedAd.load(request)
            AdFormat.REWARDED_INTERSTITIAL -> RewardedInterstitialAd.load(request)
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

    private fun pollPreloaded(format: AdFormat, adUnitId: String): Any? = runCatching {
        when (format) {
            AdFormat.INTERSTITIAL -> InterstitialAdPreloader.pollAd(adUnitId)
            AdFormat.REWARDED -> RewardedAdPreloader.pollAd(adUnitId)
            AdFormat.REWARDED_INTERSTITIAL -> RewardedInterstitialAdPreloader.pollAd(adUnitId)
            AdFormat.APP_OPEN -> AppOpenAdPreloader.pollAd(adUnitId)
            AdFormat.BANNER, AdFormat.NATIVE -> null
        }
    }.getOrNull()

    private suspend fun present(
        ad: Any,
        activity: Activity,
        format: AdFormat,
        onShown: (() -> Unit)? = null,
    ): AdResult {
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

            is RewardedInterstitialAd -> {
                ad.adEventCallback = object : RewardedInterstitialAdEventCallback {
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

    private fun freshOnDemandAd(key: String): OnDemandAd? {
        val loaded = onDemandAds[key] ?: return null
        if (SystemClock.elapsedRealtime() - loaded.loadedAt < ON_DEMAND_AD_TTL_MS) return loaded
        onDemandAds.remove(key, loaded)
        log.d("On-demand ad expired for $key")
        return null
    }

    private fun key(format: AdFormat, id: String) = "${format.name}:$id"

    private class OnDemandAd(val ad: Any, val loadedAt: Long)

    private data class PreloadTarget(val format: AdFormat, val adUnitId: String)

    private companion object {
        const val ON_DEMAND_AD_TTL_MS = 55L * 60L * 1000L
    }
}
