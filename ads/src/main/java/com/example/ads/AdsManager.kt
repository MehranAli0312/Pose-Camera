package com.example.ads

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AdsManager {

    val isReady: StateFlow<Boolean>

    val isFullScreenAdVisible: StateFlow<Boolean>

    val isPro: StateFlow<Boolean>

    val proStatus: StateFlow<ProStatus>

    fun initialize()

    suspend fun requestConsent(activity: Activity): ConsentResult

    fun preloadFor(placements: List<AdPlacement>)

    fun prepareSlot(placement: AdPlacement)

    fun isSlotPrepared(placement: AdPlacement): Flow<Boolean>

    fun releaseSlot(placement: AdPlacement)

    fun loadOnDemand(placement: AdPlacement)

    fun isLoadedOnDemand(placement: AdPlacement): Boolean

    fun isLoadingOnDemand(placement: AdPlacement): Boolean

    fun canShowFullScreenNow(placement: AdPlacement): Boolean

    suspend fun awaitOnDemand(placement: AdPlacement, timeoutMs: Long): Boolean

    suspend fun awaitLoadInFlight(placement: AdPlacement, timeoutMs: Long): Boolean

    suspend fun showOnDemand(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
    ): AdResult

    suspend fun loadAndShow(
        activity: Activity,
        placement: AdPlacement,
        onShown: (() -> Unit)? = null,
        loadTimeoutMs: Long? = null,
        onLoadStarted: (() -> Unit)? = null,
    ): AdResult

    fun styleFor(placement: AdPlacement): AdSlotStyle

    fun fullscreenStyleFor(placement: AdPlacement): FullscreenAdStyle

    fun nativeAdColors(): NativeAdColors

    fun destroy()
}
