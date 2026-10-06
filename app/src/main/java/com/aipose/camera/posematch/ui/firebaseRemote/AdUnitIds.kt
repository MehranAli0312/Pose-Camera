package com.aipose.camera.posematch.ui.firebaseRemote

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.aipose.camera.posematch.ads.OnboardingFullScreenNative
import com.aipose.camera.posematch.ads.SplashFullscreen
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ACTIVITY_BANNER_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ACTIVITY_INTER_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ACTIVITY_NATIVE_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.APP_OPEN_ON_RESUME_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ONBOARD_FULL_NATIVE_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.REWARDED_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.SPLASH_INTER_AD_UNIT

data class AdUnitIds(
    val splashInter: String = "",
    val activityInter: String = "",
    val activityBanner: String = "",
    val activityNative: String = "",
    val onboardFullNative: String = "",
    val appOpenOnResume: String = "",
    val rewarded: String = "",
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
        AdFormat.REWARDED, AdFormat.REWARDED_INTERSTITIAL -> rewarded
    }

    fun byKey(): Map<String, String> = mapOf(
        SPLASH_INTER_AD_UNIT to splashInter,
        ACTIVITY_INTER_AD_UNIT to activityInter,
        ACTIVITY_BANNER_AD_UNIT to activityBanner,
        ACTIVITY_NATIVE_AD_UNIT to activityNative,
        ONBOARD_FULL_NATIVE_AD_UNIT to onboardFullNative,
        APP_OPEN_ON_RESUME_AD_UNIT to appOpenOnResume,
        REWARDED_AD_UNIT to rewarded,
    )

    companion object {
        inline fun read(valueOf: (key: String) -> String): AdUnitIds = AdUnitIds(
            splashInter = valueOf(SPLASH_INTER_AD_UNIT).trim(),
            activityInter = valueOf(ACTIVITY_INTER_AD_UNIT).trim(),
            activityBanner = valueOf(ACTIVITY_BANNER_AD_UNIT).trim(),
            activityNative = valueOf(ACTIVITY_NATIVE_AD_UNIT).trim(),
            onboardFullNative = valueOf(ONBOARD_FULL_NATIVE_AD_UNIT).trim(),
            appOpenOnResume = valueOf(APP_OPEN_ON_RESUME_AD_UNIT).trim(),
            rewarded = valueOf(REWARDED_AD_UNIT).trim(),
        )
    }
}
