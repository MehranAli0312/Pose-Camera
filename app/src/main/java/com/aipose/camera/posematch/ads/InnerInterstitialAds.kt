package com.aipose.camera.posematch.ads

import android.os.SystemClock
import androidx.activity.ComponentActivity
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdPlacement
import com.example.ads.AdsManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InnerInterstitialAds(
    private val adsManager: AdsManager,
    private val store: AdsRemoteConfigStore,
    private val commercialBreakAds: CommercialBreakAds,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var loadJob: Job? = null

    @Volatile
    private var cappingStartedAt = NOT_STARTED

    private val placement: AdPlacement get() = AdPlacement.InnerInterstitial

    private val isEnabled: Boolean
        get() = adsManager.fullscreenStyleFor(placement).isVisible

    private val cappingMs: Long
        get() = TimeUnit.SECONDS.toMillis(
            store.current.innerInterstitialCappingSeconds.coerceAtLeast(0L),
        )

    private val isCappingDone: Boolean
        get() = cappingStartedAt != NOT_STARTED &&
            SystemClock.elapsedRealtime() - cappingStartedAt >= cappingMs

    fun startCapping() {
        if (cappingStartedAt == NOT_STARTED) restartCapping()
    }

    fun onOtherFullScreenAdShown() {
        if (cappingStartedAt != NOT_STARTED) restartCapping()
    }

    fun showThen(activity: ComponentActivity?, onContinue: () -> Unit) {
        if (activity == null || !isEnabled || !isCappingDone) {
            onContinue()
            return
        }
        commercialBreakAds.showThen(
            activity = activity,
            placement = placement,
            onResult = { result -> if (result.wasShown) restartCapping() else requestLoad() },
            onContinue = onContinue,
        )
    }

    private fun restartCapping() {
        loadJob?.cancel()
        cappingStartedAt = SystemClock.elapsedRealtime()
        val loadDelayMs = (cappingMs - LOAD_BEFORE_CAPPING_END_MS).coerceAtLeast(0L)
        loadJob = scope.launch {
            delay(loadDelayMs)
            requestLoad()
        }
    }

    private fun requestLoad() {
        if (!isEnabled || adsManager.isLoadedOnDemand(placement)) return
        adsManager.loadOnDemand(placement)
    }

    private companion object {
        const val NOT_STARTED = -1L
        val LOAD_BEFORE_CAPPING_END_MS = TimeUnit.SECONDS.toMillis(10L)
    }
}
