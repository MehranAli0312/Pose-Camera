package com.aipose.camera.posematch.domain.models

enum class AdAnalyticsAction(val eventName: String) {
    Loaded("ad_loaded"),
    LoadFailed("ad_load_failed"),
    Shown("ad_shown"),
    ShowFailed("ad_show_failed"),
    Clicked("ad_clicked"),
}
