package com.example.ads

import com.example.ads.internal.BuildConfigAdUnitProvider
import kotlinx.coroutines.flow.StateFlow

fun interface AdUnitProvider {
    fun adUnitId(format: AdFormat, placement: AdPlacement): String?

    companion object {
        val BuildConfigUnits: AdUnitProvider = BuildConfigAdUnitProvider()
    }
}

fun interface ProStatusProvider {
    fun proStatus(): StateFlow<ProStatus>
}

interface AdSlotStyleProvider {

    fun styleFor(placement: AdPlacement): AdSlotStyle

    fun fullscreenStyleFor(placement: AdPlacement): FullscreenAdStyle = FullscreenAdStyle.Hidden

    fun nativeAdColors(): NativeAdColors = NativeAdColors()
}
