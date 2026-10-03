package com.example.ads.internal

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.AdUnitProvider
import com.example.ads.AdsConfig
import com.example.ads.ProStatus
import com.example.ads.ProStatusProvider

internal class AdGate(
    private val config: AdsConfig,
    private val state: AdsRuntimeState,
    private val network: NetworkChecker,
    private val proStatus: ProStatusProvider,
    private val adUnits: AdUnitProvider,
    private val log: AdsLog,
) {

    fun isPro(): Boolean = proStatus.proStatus().value == ProStatus.PRO

    fun resolveRequestableUnit(format: AdFormat, placement: AdPlacement): String? {
        if (isPro()) {
            log.d("$format blocked: pro user")
            return null
        }
        if (!state.canRequestAds.value) {
            log.d("$format blocked: consent not resolved / SDK not ready")
            return null
        }
        if (!network.isOnline()) {
            log.d("$format blocked: offline")
            return null
        }
        val unitId = adUnits.adUnitId(format, placement)
        if (unitId == null) {
            log.d("$format blocked: no ad unit configured for ${placement.id}")
            return null
        }
        return unitId
    }

    fun tryAcquireFullScreen(format: AdFormat): Boolean {
        val status = proStatus.proStatus().value
        if (!status.isEligibleForAds) {
            log.d("$format blocked: pro status is $status")
            return false
        }
        if (!state.foreground.value) {
            log.d("$format blocked: app is backgrounded")
            return false
        }
        if (!state.enoughTimeSinceLastFullScreenAd(config.minIntervalBetweenFullScreenAdsMs)) {
            log.d("$format blocked: minimum interval between full-screen ads not met")
            return false
        }
        if (!state.tryAcquireFullScreen()) {
            log.d("$format blocked: another full-screen ad is showing")
            return false
        }
        return true
    }

    fun passesInterstitialFrequency(): Boolean =
        state.passesInterstitialFrequency(config.interstitialFrequency)
}
