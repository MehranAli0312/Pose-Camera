package com.aipose.camera.posematch.ads

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdsManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch

class PhotoSaveInterstitialAds(
    private val adsManager: AdsManager,
    private val innerInterstitialAds: InnerInterstitialAds,
    private val rewardedUnlockSession: RewardedUnlockSession,
) {

    private val isShowing = AtomicBoolean(false)

    private val placement: AdPlacement get() = AdPlacement.PhotoSaveInterstitial

    private val isEnabled: Boolean
        get() = adsManager.fullscreenStyleFor(placement).isVisible

    fun prepare(poseId: Int?) {
        if (!allowsPose(poseId)) return
        requestLoad()
    }

    fun showThen(activity: ComponentActivity?, poseId: Int?, onContinue: () -> Unit) {
        if (isShowing.get()) return
        if (activity == null || !allowsPose(poseId)) {
            onContinue()
            return
        }
        if (!adsManager.isLoadedOnDemand(placement)) {
            requestLoad()
            onContinue()
            return
        }
        present(activity, onContinue)
    }

    private fun allowsPose(poseId: Int?): Boolean =
        isEnabled && !rewardedUnlockSession.hasWatchedRewardedAdFor(poseId)

    private fun present(activity: ComponentActivity, onContinue: () -> Unit) {
        if (!isShowing.compareAndSet(false, true)) return
        activity.lifecycleScope.launch {
            val continued = AtomicBoolean(false)
            fun continueOnce() {
                if (!continued.compareAndSet(false, true)) return
                activity.runOnUiThread(onContinue)
            }

            val result = try {
                runCatching { adsManager.showOnDemand(activity, placement) { continueOnce() } }
                    .getOrElse { error -> AdResult.Failed(error.message.orEmpty()) }
            } finally {
                isShowing.set(false)
            }
            if (result.wasShown) innerInterstitialAds.onOtherFullScreenAdShown()
            activity.lifecycle.withResumed { continueOnce() }
        }
    }

    private fun requestLoad() {
        if (adsManager.isLoadedOnDemand(placement)) return
        adsManager.loadOnDemand(placement)
    }
}
