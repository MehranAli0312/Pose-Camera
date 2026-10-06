package com.aipose.camera.posematch.ads

import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdsManager
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InnerInterstitialAds(
    private val adsManager: AdsManager,
    private val store: AdsRemoteConfigStore,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val isShowing = AtomicBoolean(false)

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
        if (isShowing.get()) return
        if (activity == null || !isEnabled || !isCappingDone) {
            onContinue()
            return
        }
        if (adsManager.isLoadedOnDemand(placement)) {
            present(activity, onContinue) { onShown ->
                adsManager.showOnDemand(activity, placement, onShown)
            }
            return
        }
        requestLoad()
        if (canUseSplashFallback()) {
            present(activity, onContinue) { onShown ->
                adsManager.showPreloaded(activity, AdPlacement.SplashFullscreen, onShown)
            }
            return
        }
        onContinue()
    }

    private fun canUseSplashFallback(): Boolean {
        if (!store.current.innerInterstitialSplashFallback) return false
        val format = adsManager.fullscreenStyleFor(AdPlacement.SplashFullscreen).format
            ?: return false
        return adsManager.isAvailable(format, AdPlacement.SplashFullscreen)
    }

    private fun present(
        activity: ComponentActivity,
        onContinue: () -> Unit,
        show: suspend (onShown: () -> Unit) -> AdResult,
    ) {
        if (!isShowing.compareAndSet(false, true)) return
        activity.lifecycleScope.launch {
            val continued = AtomicBoolean(false)
            fun continueOnce() {
                if (!continued.compareAndSet(false, true)) return
                activity.runOnUiThread(onContinue)
            }

            val result = try {
                runCatching { show { continueOnce() } }
                    .getOrElse { error -> AdResult.Failed(error.message.orEmpty()) }
            } finally {
                isShowing.set(false)
            }
            if (result.wasShown) restartCapping() else requestLoad()
            activity.lifecycle.withResumed { continueOnce() }
        }
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
