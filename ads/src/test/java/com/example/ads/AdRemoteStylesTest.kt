package com.example.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdRemoteStylesTest {

    @Test
    fun `banner or native scale maps every banner and native style`() {
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.bannerOrNative(AdRemoteStyles.OFF))
        assertEquals(
            AdSlotStyle.Banner(BannerStyle.ADAPTIVE),
            AdRemoteStyles.bannerOrNative(AdRemoteStyles.ADAPTIVE_BANNER),
        )
        assertEquals(
            AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_BOTTOM),
            AdRemoteStyles.bannerOrNative(AdRemoteStyles.COLLAPSIBLE_BOTTOM_BANNER),
        )
        assertEquals(
            AdSlotStyle.Banner(BannerStyle.MEDIUM_RECTANGLE),
            AdRemoteStyles.bannerOrNative(AdRemoteStyles.MEDIUM_RECTANGLE),
        )
        assertEquals(
            AdSlotStyle.Native(NativeAdDesign.SMALL),
            AdRemoteStyles.bannerOrNative(AdRemoteStyles.NATIVE_SMALL),
        )
        assertEquals(
            AdSlotStyle.Native(NativeAdDesign.MEDIUM),
            AdRemoteStyles.bannerOrNative(AdRemoteStyles.NATIVE_MEDIUM),
        )
    }

    @Test
    fun `every style reports the format it will present`() {
        assertNull(FullscreenAdStyle.Hidden.format)
        assertEquals(AdFormat.INTERSTITIAL, FullscreenAdStyle.Interstitial.format)
        assertEquals(AdFormat.APP_OPEN, FullscreenAdStyle.AppOpen.format)
        assertEquals(AdFormat.REWARDED, FullscreenAdStyle.Rewarded.format)
        assert(!FullscreenAdStyle.Hidden.isVisible)
        assert(FullscreenAdStyle.Interstitial.isVisible)
    }

    @Test
    fun `an unrecognised value never shows an ad`() {
        val unknown = listOf(-1, 6, 99, 1_000, Int.MIN_VALUE, Int.MAX_VALUE)

        unknown.forEach { id ->
            assertEquals("bannerOrNative($id)", AdSlotStyle.Hidden, AdRemoteStyles.bannerOrNative(id))
        }
    }

    @Test
    fun `an explicit banner style overrides the configured one`() {
        val configured = AdRemoteStyles.bannerOrNative(AdRemoteStyles.ADAPTIVE_BANNER)

        assertEquals(
            BannerStyle.COLLAPSIBLE_BOTTOM,
            configured.bannerStyleOr(BannerStyle.COLLAPSIBLE_BOTTOM),
        )
    }

    @Test
    fun `the configured banner style is used when the caller names none`() {
        assertEquals(
            BannerStyle.ADAPTIVE,
            AdRemoteStyles.bannerOrNative(AdRemoteStyles.ADAPTIVE_BANNER).bannerStyleOr(null),
        )
    }

    @Test
    fun `an explicit native design overrides the configured one`() {
        val configured = AdRemoteStyles.bannerOrNative(AdRemoteStyles.NATIVE_SMALL)

        assertEquals(NativeAdDesign.LARGE, configured.nativeDesignOr(NativeAdDesign.LARGE))
    }

    @Test
    fun `the configured native design is used when the caller names none`() {
        assertEquals(
            NativeAdDesign.MEDIUM,
            AdRemoteStyles.bannerOrNative(AdRemoteStyles.NATIVE_MEDIUM).nativeDesignOr(null),
        )
    }

    @Test
    fun `a placement that is off renders nothing when the caller names no style`() {
        assertNull(AdSlotStyle.Hidden.bannerStyleOr(null))
        assertNull(AdSlotStyle.Hidden.nativeDesignOr(null))
    }

    @Test
    fun `a placement never renders a format it was not configured for`() {
        val nativePlacement = AdRemoteStyles.bannerOrNative(AdRemoteStyles.NATIVE_MEDIUM)
        val bannerPlacement = AdRemoteStyles.bannerOrNative(AdRemoteStyles.ADAPTIVE_BANNER)

        assertNull("a native placement must not fall back to a banner",
            nativePlacement.bannerStyleOr(null))
        assertNull("a banner placement must not fall back to a native ad",
            bannerPlacement.nativeDesignOr(null))
    }

}
