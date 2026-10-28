package com.aipose.camera.posematch.domain.models

enum class AnalyticsEvent(val eventName: String) {
    Splash("splash"),
    Language("language"),
    Onboard("onboard"),
    Home("home"),
    HomeHero("home_hero"),
    HomeCategory("home_category"),
    Camera("camera"),
    Editor("editor"),
    Save("save"),
}
