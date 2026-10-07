package com.example.ads

object AdRemoteStyles {

    const val OFF: Int = 0

    fun slot(id: Int): AdSlotStyle = when (id) {
        1 -> AdSlotStyle.Banner(BannerStyle.ADAPTIVE)
        2 -> AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_TOP)
        3 -> AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_BOTTOM)
        4 -> AdSlotStyle.Native(NativeAdDesign.SMALL)
        5 -> AdSlotStyle.Native(NativeAdDesign.MEDIUM)
        6 -> AdSlotStyle.Native(NativeAdDesign.LARGE)
        7 -> AdSlotStyle.Banner(BannerStyle.INLINE)
        else -> AdSlotStyle.Hidden
    }

    fun banner(id: Int): AdSlotStyle = when (id) {
        1 -> AdSlotStyle.Banner(BannerStyle.ADAPTIVE)
        2 -> AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_TOP)
        3 -> AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_BOTTOM)
        4 -> AdSlotStyle.Banner(BannerStyle.INLINE)
        else -> AdSlotStyle.Hidden
    }

    fun native(id: Int): AdSlotStyle = when (id) {
        1 -> AdSlotStyle.Native(NativeAdDesign.SMALL)
        2 -> AdSlotStyle.Native(NativeAdDesign.MEDIUM)
        3 -> AdSlotStyle.Native(NativeAdDesign.LARGE)
        else -> AdSlotStyle.Hidden
    }

    fun bannerOrSmallNative(id: Int): AdSlotStyle = when (id) {
        1 -> AdSlotStyle.Banner(BannerStyle.ADAPTIVE)
        2 -> AdSlotStyle.Native(NativeAdDesign.SMALL)
        else -> AdSlotStyle.Hidden
    }

    fun fullscreen(id: Int): FullscreenAdStyle = when (id) {
        1 -> FullscreenAdStyle.Interstitial
        2 -> FullscreenAdStyle.AppOpen
        else -> FullscreenAdStyle.Hidden
    }

    fun rewardOffer(id: Int): FullscreenAdStyle = when (id) {
        1 -> FullscreenAdStyle.Interstitial
        2 -> FullscreenAdStyle.RewardedInterstitial
        3 -> FullscreenAdStyle.Rewarded
        else -> FullscreenAdStyle.Hidden
    }

    fun toggle(id: Int, whenOn: FullscreenAdStyle): FullscreenAdStyle =
        if (id == OFF) FullscreenAdStyle.Hidden else whenOn
}
