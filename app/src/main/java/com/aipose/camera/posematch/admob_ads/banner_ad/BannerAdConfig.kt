package com.aipose.camera.posematch.admob_ads.banner_ad

data class BannerAdConfig(
    val idAds: String,
    val isFreeSizeAd: Boolean,
    val canReloadAds: Boolean = true,
    val isCollapsibleAd: Boolean = false
)
