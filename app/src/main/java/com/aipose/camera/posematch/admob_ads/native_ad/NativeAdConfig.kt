package com.aipose.camera.posematch.admob_ads.native_ad

import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.admob_ads.remote.RemoteConfig

data class NativeAdConfig(
    val idAds: String? = null,                 // normal ad id
    val highFloorAdId: String? = null,         // nullable high-floor ad id
    val remoteHighFloorEnabled: Boolean = false, // remote toggle for high-floor behavior
    val canReloadAds: Boolean = RemoteConfig.reloadNativeAds,
    val reloadTime: Long = RemoteConfig.nativeAdReloadTime,
    val layoutId: Int = R.layout.native_layout1_loading,
    var shimmerLayout: Int? = null,
    var TAG: String = "NativeAdHelper12",
    val cacheKey: String? = null
)
