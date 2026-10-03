package com.aipose.camera.posematch.ui.firebaseRemote

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.aipose.camera.posematch.ads.SplashFullscreen
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ACTIVITY_BANNER_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ACTIVITY_INTER_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ACTIVITY_NATIVE_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.APP_OPEN_ON_RESUME_AD_UNIT
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.SPLASH_INTER_AD_UNIT

data class AdUnitIds(
    val splashInter: String = "",
    val activityInter: String = "",
    val activityBanner: String = "",
    val activityNative: String = "",
    val appOpenOnResume: String = "",
) {
    fun unitFor(format: AdFormat, placement: AdPlacement): String = when (format) {
        AdFormat.INTERSTITIAL ->
            if (placement == AdPlacement.SplashFullscreen) splashInter else activityInter
        AdFormat.BANNER -> activityBanner
        AdFormat.NATIVE -> activityNative
        AdFormat.APP_OPEN -> appOpenOnResume
        AdFormat.REWARDED, AdFormat.REWARDED_INTERSTITIAL -> ""
    }

    fun byKey(): Map<String, String> = mapOf(
        SPLASH_INTER_AD_UNIT to splashInter,
        ACTIVITY_INTER_AD_UNIT to activityInter,
        ACTIVITY_BANNER_AD_UNIT to activityBanner,
        ACTIVITY_NATIVE_AD_UNIT to activityNative,
        APP_OPEN_ON_RESUME_AD_UNIT to appOpenOnResume,
    )

    companion object {
        inline fun read(valueOf: (key: String) -> String): AdUnitIds = AdUnitIds(
            splashInter = valueOf(SPLASH_INTER_AD_UNIT).trim(),
            activityInter = valueOf(ACTIVITY_INTER_AD_UNIT).trim(),
            activityBanner = valueOf(ACTIVITY_BANNER_AD_UNIT).trim(),
            activityNative = valueOf(ACTIVITY_NATIVE_AD_UNIT).trim(),
            appOpenOnResume = valueOf(APP_OPEN_ON_RESUME_AD_UNIT).trim(),
        )
    }
}
