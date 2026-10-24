package com.example.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdRemoteStylesTest {

    @Test
    fun `banner or native scale maps off banner small and medium native`() {
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.bannerOrNative(0))
        assertEquals(AdSlotStyle.Banner(BannerStyle.ADAPTIVE), AdRemoteStyles.bannerOrNative(1))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.SMALL), AdRemoteStyles.bannerOrNative(2))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.MEDIUM), AdRemoteStyles.bannerOrNative(3))
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.bannerOrNative(4))
    }

    @Test
    fun `native scale maps off and every shipped design`() {
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.native(0))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.SMALL), AdRemoteStyles.native(1))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.MEDIUM), AdRemoteStyles.native(2))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.LARGE), AdRemoteStyles.native(3))
    }

    @Test
    fun `native scale never resolves to a banner`() {
        (0..50).forEach { id ->
            val style = AdRemoteStyles.native(id)
            assert(style !is AdSlotStyle.Banner) { "native($id) resolved to $style" }
        }
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
    fun `an unrecognised value never shows an ad on any scale`() {
        val unknown = listOf(-1, 99, 1_000, Int.MIN_VALUE, Int.MAX_VALUE)

        unknown.forEach { id ->
            assertEquals("native($id)", AdSlotStyle.Hidden, AdRemoteStyles.native(id))
            assertEquals(
                "bannerOrNative($id)",
                AdSlotStyle.Hidden,
                AdRemoteStyles.bannerOrNative(id),
            )
        }
    }

    @Test
    fun `an explicit banner style overrides the configured one`() {
        val configured = AdRemoteStyles.bannerOrNative(1)

        assertEquals(
            BannerStyle.COLLAPSIBLE_BOTTOM,
            configured.bannerStyleOr(BannerStyle.COLLAPSIBLE_BOTTOM),
        )
    }

    @Test
    fun `the configured banner style is used when the caller names none`() {
        assertEquals(BannerStyle.ADAPTIVE, AdRemoteStyles.bannerOrNative(1).bannerStyleOr(null))
    }

    @Test
    fun `an explicit native design overrides the configured one`() {
        val configured = AdRemoteStyles.native(1)

        assertEquals(NativeAdDesign.LARGE, configured.nativeDesignOr(NativeAdDesign.LARGE))
    }

    @Test
    fun `the configured native design is used when the caller names none`() {
        assertEquals(NativeAdDesign.MEDIUM, AdRemoteStyles.native(2).nativeDesignOr(null))
    }

    @Test
    fun `a placement that is off renders nothing when the caller names no style`() {
        assertNull(AdSlotStyle.Hidden.bannerStyleOr(null))
        assertNull(AdSlotStyle.Hidden.nativeDesignOr(null))
    }

    @Test
    fun `a placement never renders a format it was not configured for`() {
        val nativePlacement = AdRemoteStyles.native(2)
        val bannerPlacement = AdRemoteStyles.bannerOrNative(1)

        assertNull("a native placement must not fall back to a banner",
            nativePlacement.bannerStyleOr(null))
        assertNull("a banner placement must not fall back to a native ad",
            bannerPlacement.nativeDesignOr(null))
    }

}
