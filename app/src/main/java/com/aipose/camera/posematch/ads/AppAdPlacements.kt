package com.aipose.camera.posematch.ads

import com.example.ads.AdPlacement

val AdPlacement.Companion.SplashFullscreen: AdPlacement
    get() = AdPlacement("splash_fullscreen")

val AdPlacement.Companion.HomeScreenBottom: AdPlacement
    get() = AdPlacement("home_screen_bottom")

val AdPlacement.Companion.AppOpenResume: AdPlacement
    get() = AdPlacement("app_open_resume")

val AdPlacement.Companion.InnerInterstitial: AdPlacement
    get() = AdPlacement("inner_interstitial")
