package com.aipose.camera.posematch.ads

import com.example.ads.AdPlacement

val AdPlacement.Companion.SplashFullscreen: AdPlacement
    get() = AdPlacement("splash_fullscreen")

val AdPlacement.Companion.HomeScreenBottom: AdPlacement
    get() = AdPlacement("home_screen_bottom")

val AdPlacement.Companion.LanguageScreenBottom: AdPlacement
    get() = AdPlacement("language_screen_bottom")

val AdPlacement.Companion.OnboardScreenBottom: AdPlacement
    get() = AdPlacement("onboard_screen_bottom")

val AdPlacement.Companion.OnboardingFullScreenNative: AdPlacement
    get() = AdPlacement("onboarding_full_screen_native")

val AdPlacement.Companion.AppOpenResume: AdPlacement
    get() = AdPlacement("app_open_resume")

val AdPlacement.Companion.InnerInterstitial: AdPlacement
    get() = AdPlacement("inner_interstitial")

val AdPlacement.Companion.PremiumRewarded: AdPlacement
    get() = AdPlacement("premium_rewarded")

val AdPlacement.Companion.PhotoSaveInterstitial: AdPlacement
    get() = AdPlacement("photo_save_interstitial")
