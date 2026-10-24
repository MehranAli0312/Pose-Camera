package com.aipose.camera.posematch.ads

import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdsManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class CommercialBreakAds(
    private val adsManager: AdsManager,
    private val store: AdsRemoteConfigStore,
    private val coverState: AdLoadingCoverState,
) {

    private val isRunning = AtomicBoolean(false)

    fun showThen(
        activity: ComponentActivity,
        placement: AdPlacement,
        onResult: (AdResult) -> Unit,
        onContinue: () -> Unit,
    ) {
        if (!isRunning.compareAndSet(false, true)) return
        activity.lifecycleScope.launch {
            val continued = AtomicBoolean(false)
            fun continueOnce() {
                if (!continued.compareAndSet(false, true)) return
                activity.runOnUiThread(onContinue)
            }

            val result = try {
                runCatching { runBreak(activity, placement) { continueOnce() } }
                    .getOrElse { error -> AdResult.Failed(error.message.orEmpty()) }
            } finally {
                coverState.hide(AdLoadingCover.CommercialBreak)
                isRunning.set(false)
            }
            onResult(result)
            activity.lifecycle.withResumed { continueOnce() }
        }
    }

    private suspend fun runBreak(
        activity: ComponentActivity,
        placement: AdPlacement,
        onShown: () -> Unit,
    ): AdResult {
        readyPlacement(placement)?.let { ready -> return show(activity, ready, onShown) }
        val canWait = adsManager.isLoadingOnDemand(placement) &&
            adsManager.canShowFullScreenNow(placement)
        if (!canWait) return AdResult.NotAvailable
        return waitThenShow(activity, placement, onShown)
    }

    private suspend fun waitThenShow(
        activity: ComponentActivity,
        placement: AdPlacement,
        onShown: () -> Unit,
    ): AdResult {
        coverState.show(AdLoadingCover.CommercialBreak)
        val coverShownAt = SystemClock.elapsedRealtime()
        if (!awaitLoadWhileStarted(activity, placement)) return AdResult.NotAvailable
        delay(MIN_COVER_MS - (SystemClock.elapsedRealtime() - coverShownAt))
        return show(activity, placement, onShown)
    }

    private suspend fun awaitLoadWhileStarted(
        activity: ComponentActivity,
        placement: AdPlacement,
    ): Boolean = merge(
        flow { emit(adsManager.awaitLoadInFlight(placement, store.current.interLoadWaitMs)) },
        activity.lifecycle.currentStateFlow
            .filter { state -> !state.isAtLeast(Lifecycle.State.STARTED) }
            .map { false },
    ).first()

    private suspend fun show(
        activity: ComponentActivity,
        placement: AdPlacement,
        onShown: () -> Unit,
    ): AdResult = adsManager.showOnDemand(activity, placement) {
        coverState.hide(AdLoadingCover.CommercialBreak)
        onShown()
    }

    private fun readyPlacement(placement: AdPlacement): AdPlacement? =
        candidatesFor(placement).firstOrNull(adsManager::isLoadedOnDemand)

    private fun candidatesFor(placement: AdPlacement): List<AdPlacement> =
        if (store.current.innerInterstitialSplashFallback) {
            listOf(placement, AdPlacement.SplashFullscreen)
        } else {
            listOf(placement)
        }

    private companion object {
        const val MIN_COVER_MS = 600L
    }
}
