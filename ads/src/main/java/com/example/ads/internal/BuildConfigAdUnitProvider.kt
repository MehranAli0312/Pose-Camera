package com.example.ads.internal

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.AdUnitProvider
import com.example.ads.BuildConfig

internal class BuildConfigAdUnitProvider : AdUnitProvider {

    override fun adUnitId(format: AdFormat, placement: AdPlacement): String? = when (format) {
        AdFormat.BANNER -> BuildConfig.AD_UNIT_BANNER
        AdFormat.NATIVE -> BuildConfig.AD_UNIT_NATIVE
        AdFormat.INTERSTITIAL -> BuildConfig.AD_UNIT_INTERSTITIAL
        AdFormat.REWARDED -> BuildConfig.AD_UNIT_REWARDED
        AdFormat.REWARDED_INTERSTITIAL -> BuildConfig.AD_UNIT_REWARDED_INTERSTITIAL
        AdFormat.APP_OPEN -> BuildConfig.AD_UNIT_APP_OPEN
    }.takeIf { it.isNotBlank() }
}
