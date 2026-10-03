package com.example.ads.internal

import android.app.Activity
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdSlotStyle
import com.example.ads.AdSlotStyleProvider
import com.example.ads.AdsManager
import com.example.ads.ConsentResult
import com.example.ads.FullscreenAdStyle
import com.example.ads.NativeAdColors
import com.example.ads.ProStatus
import com.example.ads.ProStatusProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DefaultAdsManager(
    private val scope: CoroutineScope,
    private val state: AdsRuntimeState,
    private val initializer: SdkInitializer,
    private val consentManager: ConsentManager,
    private val fullScreenAds: FullScreenAdController,
    private val nativeAds: NativeAdController,
    private val preparedSlots: PreparedSlotController,
    private val slotStyles: AdSlotStyleProvider,
    proStatusProvider: ProStatusProvider,
    private val log: AdsLog,
) : AdsManager {

    override val proStatus: StateFlow<ProStatus> = proStatusProvider.proStatus()

    override val isPro: StateFlow<Boolean> =
        proStatus.map { it == ProStatus.PRO }.stateIn(scope, SharingStarted.Eagerly, false)

    override val isReady: StateFlow<Boolean> =
        combine(state.sdkInitialized, state.canRequestAds) { initialized, canRequest ->
            initialized && canRequest
        }.stateIn(scope, SharingStarted.Eagerly, false)

    override val isFullScreenAdVisible: StateFlow<Boolean> = state.fullScreenAdVisible

    private val initializationStarted = MutableStateFlow(false)

    private val warmUpGeneration = MutableStateFlow(0)

    override fun initialize() {
        if (initializationStarted.value) return
        initializationStarted.value = true
        scope.launch {
            val cachedConsentAllowsAds = withContext(Dispatchers.Main) {
                consentManager.canRequestAds()
            }
            if (cachedConsentAllowsAds) {
                state.setCanRequestAds(true)
                log.d("Cached consent already permits ad requests")
            }

            initializer.initialize()
        }
    }

    override suspend fun requestConsent(activity: Activity): ConsentResult {
        val result = consentManager.requestConsent(activity)
        if (result is ConsentResult.CanRequestAds) {
            initializer.initialize()
            state.setCanRequestAds(true)
            log.d("Consent resolved, ads enabled")
        } else {
            state.setCanRequestAds(false)
            log.d("Consent did not permit ad requests: $result")
        }
        return result
    }

    override fun preload(vararg formats: AdFormat, placement: AdPlacement) {
        scope.launch {
            formats.forEach { format ->
                when (format) {
                    AdFormat.NATIVE -> nativeAds.preload(placement)
                    AdFormat.BANNER -> Unit
                    else -> fullScreenAds.startPreloading(format, placement)
                }
            }
        }
    }

    override fun preloadFor(placements: List<AdPlacement>) {
        placements.forEach { placement ->
            val fullScreenFormat = fullscreenStyleFor(placement).format
            when {
                fullScreenFormat != null -> preload(fullScreenFormat, placement = placement)
                styleFor(placement) is AdSlotStyle.Native ->
                    preload(AdFormat.NATIVE, placement = placement)
                else -> Unit
            }
        }
    }

    override fun keepWarm(placements: List<AdPlacement>) {
        if (placements.isEmpty()) return
        scope.launch {
            combine(isReady, isPro, warmUpGeneration) { ready, _, _ -> ready }
                .collect { ready ->
                    if (!ready) return@collect
                    stopHiddenPreloads(placements)
                    preloadFor(placements)
                }
        }
        log.d("Warm-up registered for ${placements.joinToString { it.id }}")
    }

    // A placement turned off remotely, or a user who went pro, should not keep a preloader
    // refilling ads that will never be shown.
    private fun stopHiddenPreloads(placements: List<AdPlacement>) {
        placements
            .filter { placement -> fullscreenStyleFor(placement).format == null }
            .forEach(fullScreenAds::stopPreloading)
    }

    override fun revalidateWarmUp() {
        warmUpGeneration.update { it + 1 }
    }

    override fun isAvailable(format: AdFormat, placement: AdPlacement): Boolean =
        fullScreenAds.isAvailable(format, placement)

    override fun prepareSlot(placement: AdPlacement) {
        scope.launch {
            isReady.first { it }
            preparedSlots.prepare(placement, styleFor(placement))
        }
    }

    override fun loadOnDemand(placement: AdPlacement) {
        val format = fullscreenStyleFor(placement).format ?: return
        scope.launch { fullScreenAds.loadOnDemand(format, placement) }
    }

    override fun isLoadedOnDemand(placement: AdPlacement): Boolean {
        val format = fullscreenStyleFor(placement).format ?: return false
        return fullScreenAds.isLoadedOnDemand(format, placement)
    }

    override suspend fun showOnDemand(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)?,
    ): AdResult {
        val format = fullscreenStyleFor(placement).format ?: return AdResult.NotEligible
        return fullScreenAds.showOnDemand(activity, format, placement, onShown)
    }

    override suspend fun showPreloaded(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)?,
    ): AdResult {
        val format = fullscreenStyleFor(placement).format ?: return AdResult.NotEligible
        return fullScreenAds.showPreloaded(activity, format, placement, onShown)
    }

    override fun styleFor(placement: AdPlacement): AdSlotStyle =
        if (isPro.value) AdSlotStyle.Hidden else slotStyles.styleFor(placement)

    override fun fullscreenStyleFor(placement: AdPlacement): FullscreenAdStyle =
        if (isPro.value) FullscreenAdStyle.Hidden else slotStyles.fullscreenStyleFor(placement)

    override fun nativeAdColors(): NativeAdColors = slotStyles.nativeAdColors()

    override suspend fun showFullscreen(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)?,
    ): AdResult {
        val format = fullscreenStyleFor(placement).format ?: return AdResult.NotEligible
        return fullScreenAds.show(activity, format, placement, onShown)
    }

    override suspend fun showInterstitial(activity: Activity, placement: AdPlacement): AdResult =
        fullScreenAds.show(activity, AdFormat.INTERSTITIAL, placement)

    override suspend fun showRewarded(activity: Activity, placement: AdPlacement): AdResult =
        fullScreenAds.show(activity, AdFormat.REWARDED, placement)

    override suspend fun showRewardedInterstitial(
        activity: Activity,
        placement: AdPlacement,
    ): AdResult = fullScreenAds.show(activity, AdFormat.REWARDED_INTERSTITIAL, placement)

    override suspend fun showAppOpen(activity: Activity, placement: AdPlacement): AdResult =
        fullScreenAds.show(activity, AdFormat.APP_OPEN, placement)

    override fun destroy() {
        fullScreenAds.destroy()
        nativeAds.destroy()
        preparedSlots.destroy()
    }
}
