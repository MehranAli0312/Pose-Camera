package com.aipose.camera.posematch.ads

import com.aipose.camera.posematch.BuildConfig
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import com.example.ads.AdUnitProvider

internal class RemoteAdUnitProvider(
    private val store: AdsRemoteConfigStore,
    private val debugUnits: AdUnitProvider = AdUnitProvider.BuildConfigUnits,
) : AdUnitProvider {

    override fun adUnitId(format: AdFormat, placement: AdPlacement): String? {
        if (BuildConfig.DEBUG) return debugUnits.adUnitId(format, placement)
        return store.current.adUnits.unitFor(format, placement).ifBlank { null }
    }
}
