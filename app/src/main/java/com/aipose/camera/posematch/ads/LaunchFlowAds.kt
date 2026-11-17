package com.aipose.camera.posematch.ads

import com.aipose.camera.posematch.ui.screens.launch.models.LaunchStep
import com.example.ads.AdPlacement
import com.example.ads.AdsManager

class LaunchFlowAds(
    private val adsManager: AdsManager,
) {

    fun preloadFor(step: LaunchStep) {
        when (step) {
            LaunchStep.Language -> adsManager.preloadFor(listOf(AdPlacement.LanguageScreenBottom))
            LaunchStep.Onboarding -> {
                adsManager.preloadFor(listOf(AdPlacement.OnboardScreenBottom))
                adsManager.prepareSlot(AdPlacement.OnboardingFullScreenNative)
            }
            LaunchStep.Home -> adsManager.preloadFor(listOf(AdPlacement.HomeScreenBottom))
            LaunchStep.Premium -> Unit
        }
    }
}
