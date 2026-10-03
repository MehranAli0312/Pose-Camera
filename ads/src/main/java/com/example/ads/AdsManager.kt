package com.example.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface AdsManager {

    val isReady: StateFlow<Boolean>

    val isFullScreenAdVisible: StateFlow<Boolean>

    val isPro: StateFlow<Boolean>

    val proStatus: StateFlow<ProStatus>

    fun initialize()

    suspend fun requestConsent(activity: Activity): ConsentResult

    fun preload(vararg formats: AdFormat, placement: AdPlacement = AdPlacement.Default)

    fun preloadFor(placements: List<AdPlacement>)

    fun keepWarm(placements: List<AdPlacement>)

    fun revalidateWarmUp()

    fun isAvailable(format: AdFormat, placement: AdPlacement = AdPlacement.Default): Boolean

    fun prepareSlot(placement: AdPlacement)

    fun loadOnDemand(placement: AdPlacement)

    fun isLoadedOnDemand(placement: AdPlacement): Boolean

    suspend fun showOnDemand(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
    ): AdResult

    // Shows an ad already held by the placement's preloader; never waits on a network load.
    suspend fun showPreloaded(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
    ): AdResult

    fun styleFor(placement: AdPlacement): AdSlotStyle

    fun fullscreenStyleFor(placement: AdPlacement): FullscreenAdStyle

    fun nativeAdColors(): NativeAdColors

    suspend fun showFullscreen(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
    ): AdResult

    suspend fun showInterstitial(
        activity: Activity,
        placement: AdPlacement = AdPlacement.Default,
    ): AdResult

    suspend fun showRewarded(
        activity: Activity,
        placement: AdPlacement = AdPlacement.Default,
    ): AdResult

    suspend fun showRewardedInterstitial(
        activity: Activity,
        placement: AdPlacement = AdPlacement.Default,
    ): AdResult

    suspend fun showAppOpen(
        activity: Activity,
        placement: AdPlacement = AdPlacement.Default,
    ): AdResult

    fun destroy()
}
