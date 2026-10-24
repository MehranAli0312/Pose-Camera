package com.example.ads.internal

import android.app.Activity
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

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

    override fun initialize() {
        if (initializationStarted.value) return
        initializationStarted.value = true
        scope.launch {
            val cachedConsentAllowsAds = withContext(Dispatchers.Main) {
                consentManager.canRequestAds()
            }
            if (!cachedConsentAllowsAds) {
                log.d("SDK initialisation deferred until consent permits ad requests")
                return@launch
            }

            enableAds("Cached consent already permits ad requests")
        }
    }

    override suspend fun requestConsent(activity: Activity): ConsentResult {
        var cachedConsentStart: Job? = null
        val result = consentManager.requestConsent(activity) {
            cachedConsentStart = scope.launch {
                enableAds("Cached consent permits ad requests, starting before consent update")
            }
        }
        if (result is ConsentResult.CanRequestAds) {
            enableAds("Consent resolved, ads enabled")
        } else {
            cachedConsentStart?.cancelAndJoin()
            state.setCanRequestAds(false)
            log.d("Consent did not permit ad requests: $result")
        }
        return result
    }

    private suspend fun enableAds(reason: String) {
        initializer.initialize()
        state.setCanRequestAds(true)
        log.d(reason)
    }

    override fun preloadFor(placements: List<AdPlacement>) {
        placements
            .filter { placement -> styleFor(placement) is AdSlotStyle.Native }
            .forEach { placement -> scope.launch { nativeAds.preload(placement) } }
    }

    override fun prepareSlot(placement: AdPlacement) {
        scope.launch {
            isReady.first { it }
            preparedSlots.prepare(placement, styleFor(placement))
        }
    }

    override fun isSlotPrepared(placement: AdPlacement): Flow<Boolean> =
        preparedSlots.state(placement)
            .map { prepared -> prepared is PreparedSlotAd.Banner || prepared is PreparedSlotAd.Native }
            .distinctUntilChanged()

    override fun releaseSlot(placement: AdPlacement) {
        preparedSlots.abandon(placement)
    }

    override fun loadOnDemand(placement: AdPlacement) {
        val format = fullscreenStyleFor(placement).format ?: return
        scope.launch { fullScreenAds.loadOnDemand(format, placement) }
    }

    override fun isLoadedOnDemand(placement: AdPlacement): Boolean {
        val format = fullscreenStyleFor(placement).format ?: return false
        return fullScreenAds.isLoadedOnDemand(format, placement)
    }

    override fun isLoadingOnDemand(placement: AdPlacement): Boolean {
        val format = fullscreenStyleFor(placement).format ?: return false
        return fullScreenAds.isLoadingOnDemand(format, placement)
    }

    override fun canShowFullScreenNow(placement: AdPlacement): Boolean {
        val format = fullscreenStyleFor(placement).format ?: return false
        return fullScreenAds.canShowNow(format)
    }

    override suspend fun awaitOnDemand(placement: AdPlacement, timeoutMs: Long): Boolean {
        val format = fullscreenStyleFor(placement).format ?: return false
        return withTimeoutOrNull(timeoutMs) {
            fullScreenAds.awaitOnDemand(format, placement)
        } ?: false
    }

    override suspend fun awaitLoadInFlight(placement: AdPlacement, timeoutMs: Long): Boolean {
        val format = fullscreenStyleFor(placement).format ?: return false
        return withTimeoutOrNull(timeoutMs) {
            fullScreenAds.awaitLoadInFlight(format, placement)
        } ?: false
    }

    override suspend fun showOnDemand(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)?,
    ): AdResult {
        val format = fullscreenStyleFor(placement).format ?: return AdResult.NotEligible
        return fullScreenAds.showOnDemand(activity, format, placement, onShown)
    }

    override suspend fun loadAndShow(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)?,
        loadTimeoutMs: Long?,
        onLoadStarted: (() -> Unit)?,
    ): AdResult {
        val format = fullscreenStyleFor(placement).format ?: return AdResult.NotEligible
        return fullScreenAds.loadAndShow(
            activity = activity,
            format = format,
            placement = placement,
            onShown = onShown,
            loadTimeoutMs = loadTimeoutMs,
            onLoadStarted = onLoadStarted,
        )
    }

    override fun styleFor(placement: AdPlacement): AdSlotStyle =
        if (isPro.value) AdSlotStyle.Hidden else slotStyles.styleFor(placement)

    override fun fullscreenStyleFor(placement: AdPlacement): FullscreenAdStyle =
        if (isPro.value) FullscreenAdStyle.Hidden else slotStyles.fullscreenStyleFor(placement)

    override fun nativeAdColors(): NativeAdColors = slotStyles.nativeAdColors()

    override fun destroy() {
        fullScreenAds.destroy()
        nativeAds.destroy()
        preparedSlots.destroy()
    }
}
