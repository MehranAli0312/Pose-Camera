package com.aipose.camera.posematch.ads

import androidx.activity.ComponentActivity
import com.example.ads.AdPlacement
import com.example.ads.AdsManager

class PhotoSaveInterstitialAds(
    private val adsManager: AdsManager,
    private val commercialBreakAds: CommercialBreakAds,
    private val innerInterstitialAds: InnerInterstitialAds,
    private val rewardedUnlockSession: RewardedUnlockSession,
) {

    private val placement: AdPlacement get() = AdPlacement.PhotoSaveInterstitial

    private val isEnabled: Boolean
        get() = adsManager.fullscreenStyleFor(placement).isVisible

    fun prepare(poseId: Int?) {
        if (!allowsPose(poseId)) return
        adsManager.loadOnDemand(placement)
    }

    fun showThen(activity: ComponentActivity?, poseId: Int?, onContinue: () -> Unit) {
        if (activity == null || !allowsPose(poseId)) {
            onContinue()
            return
        }
        commercialBreakAds.showThen(
            activity = activity,
            placement = placement,
            onResult = { result ->
                if (result.wasShown) innerInterstitialAds.onOtherFullScreenAdShown()
            },
            onContinue = onContinue,
        )
    }

    private fun allowsPose(poseId: Int?): Boolean =
        isEnabled && !rewardedUnlockSession.hasWatchedRewardedAdFor(poseId)
}
