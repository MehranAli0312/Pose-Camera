package com.example.ads

object AdRemoteStyles {

    const val OFF: Int = 0
    const val ADAPTIVE_BANNER: Int = 1
    const val COLLAPSIBLE_BOTTOM_BANNER: Int = 2
    const val MEDIUM_RECTANGLE: Int = 3
    const val NATIVE_SMALL: Int = 4
    const val NATIVE_MEDIUM: Int = 5

    fun bannerOrNative(id: Int): AdSlotStyle = when (id) {
        ADAPTIVE_BANNER -> AdSlotStyle.Banner(BannerStyle.ADAPTIVE)
        COLLAPSIBLE_BOTTOM_BANNER -> AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_BOTTOM)
        MEDIUM_RECTANGLE -> AdSlotStyle.Banner(BannerStyle.MEDIUM_RECTANGLE)
        NATIVE_SMALL -> AdSlotStyle.Native(NativeAdDesign.SMALL)
        NATIVE_MEDIUM -> AdSlotStyle.Native(NativeAdDesign.MEDIUM)
        else -> AdSlotStyle.Hidden
    }

}
