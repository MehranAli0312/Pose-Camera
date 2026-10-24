package com.aipose.camera.posematch.ui.firebaseRemote

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.aipose.camera.posematch.ads.OnboardingFullScreenNative
import com.aipose.camera.posematch.ads.SplashFullscreen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdUnitIds(
    @SerialName("splash_inter") val splashInter: String = "",
    @SerialName("activity_inter") val activityInter: String = "",
    @SerialName("activity_banner") val activityBanner: String = "",
    @SerialName("activity_native") val activityNative: String = "",
    @SerialName("onboard_full_native") val onboardFullNative: String = "",
    @SerialName("app_open_on_resume") val appOpenOnResume: String = "",
    @SerialName("rewarded") val rewarded: String = "",
) {
    fun unitFor(format: AdFormat, placement: AdPlacement): String = when (format) {
        AdFormat.INTERSTITIAL ->
            if (placement == AdPlacement.SplashFullscreen) splashInter else activityInter
        AdFormat.BANNER -> activityBanner
        AdFormat.NATIVE ->
            if (placement == AdPlacement.OnboardingFullScreenNative) {
                onboardFullNative
            } else {
                activityNative
            }
        AdFormat.APP_OPEN -> appOpenOnResume
        AdFormat.REWARDED -> rewarded
    }.trim()
}
