package com.example.ads

object AdRemoteStyles {

    const val OFF: Int = 0

    fun native(id: Int): AdSlotStyle = when (id) {
        1 -> AdSlotStyle.Native(NativeAdDesign.SMALL)
        2 -> AdSlotStyle.Native(NativeAdDesign.MEDIUM)
        3 -> AdSlotStyle.Native(NativeAdDesign.LARGE)
        else -> AdSlotStyle.Hidden
    }

    fun bannerOrNative(id: Int): AdSlotStyle = when (id) {
        1 -> AdSlotStyle.Banner(BannerStyle.ADAPTIVE)
        2 -> AdSlotStyle.Native(NativeAdDesign.SMALL)
        3 -> AdSlotStyle.Native(NativeAdDesign.MEDIUM)
        else -> AdSlotStyle.Hidden
    }

}
