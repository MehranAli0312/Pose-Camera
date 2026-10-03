package com.example.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdRemoteStylesTest {

    @Test
    fun `slot scale maps every value the console can currently be set to`() {
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.slot(0))
        assertEquals(AdSlotStyle.Banner(BannerStyle.ADAPTIVE), AdRemoteStyles.slot(1))
        assertEquals(AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_TOP), AdRemoteStyles.slot(2))
        assertEquals(AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_BOTTOM), AdRemoteStyles.slot(3))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.SMALL), AdRemoteStyles.slot(4))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.MEDIUM), AdRemoteStyles.slot(5))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.LARGE), AdRemoteStyles.slot(6))
        assertEquals(AdSlotStyle.Banner(BannerStyle.INLINE), AdRemoteStyles.slot(7))
    }

    @Test
    fun `banner scale maps off and every banner style`() {
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.banner(0))
        assertEquals(AdSlotStyle.Banner(BannerStyle.ADAPTIVE), AdRemoteStyles.banner(1))
        assertEquals(AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_TOP), AdRemoteStyles.banner(2))
        assertEquals(AdSlotStyle.Banner(BannerStyle.COLLAPSIBLE_BOTTOM), AdRemoteStyles.banner(3))
        assertEquals(AdSlotStyle.Banner(BannerStyle.INLINE), AdRemoteStyles.banner(4))
    }

    @Test
    fun `banner scale never resolves to a native ad`() {
        (0..50).forEach { id ->
            val style = AdRemoteStyles.banner(id)
            assert(style !is AdSlotStyle.Native) { "banner($id) resolved to $style" }
        }
    }

    @Test
    fun `banner or small native scale maps off banner and small native`() {
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.bannerOrSmallNative(0))
        assertEquals(AdSlotStyle.Banner(BannerStyle.ADAPTIVE), AdRemoteStyles.bannerOrSmallNative(1))
        assertEquals(AdSlotStyle.Native(NativeAdDesign.SMALL), AdRemoteStyles.bannerOrSmallNative(2))
        assertEquals(AdSlotStyle.Hidden, AdRemoteStyles.bannerOrSmallNative(3))
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
    fun `fullscreen scale maps off, interstitial and app open`() {
        assertEquals(FullscreenAdStyle.Hidden, AdRemoteStyles.fullscreen(0))
        assertEquals(FullscreenAdStyle.Interstitial, AdRemoteStyles.fullscreen(1))
        assertEquals(FullscreenAdStyle.AppOpen, AdRemoteStyles.fullscreen(2))
    }

    @Test
    fun `reward offer scale keeps the numbering the console has always used`() {
        assertEquals(FullscreenAdStyle.Hidden, AdRemoteStyles.rewardOffer(0))
        assertEquals(FullscreenAdStyle.Interstitial, AdRemoteStyles.rewardOffer(1))
        assertEquals(FullscreenAdStyle.RewardedInterstitial, AdRemoteStyles.rewardOffer(2))
        assertEquals(FullscreenAdStyle.Rewarded, AdRemoteStyles.rewardOffer(3))
    }

    @Test
    fun `toggle treats zero as off and anything else as on`() {
        assertEquals(
            FullscreenAdStyle.Hidden,
            AdRemoteStyles.toggle(0, FullscreenAdStyle.AppOpen),
        )
        assertEquals(
            FullscreenAdStyle.AppOpen,
            AdRemoteStyles.toggle(1, FullscreenAdStyle.AppOpen),
        )
        assertEquals(
            FullscreenAdStyle.AppOpen,
            AdRemoteStyles.toggle(7, FullscreenAdStyle.AppOpen),
        )
    }

    @Test
    fun `every style reports the format it will present`() {
        assertNull(FullscreenAdStyle.Hidden.format)
        assertEquals(AdFormat.INTERSTITIAL, FullscreenAdStyle.Interstitial.format)
        assertEquals(AdFormat.APP_OPEN, FullscreenAdStyle.AppOpen.format)
        assertEquals(AdFormat.REWARDED, FullscreenAdStyle.Rewarded.format)
        assertEquals(
            AdFormat.REWARDED_INTERSTITIAL,
            FullscreenAdStyle.RewardedInterstitial.format,
        )
        assert(!FullscreenAdStyle.Hidden.isVisible)
        assert(FullscreenAdStyle.Interstitial.isVisible)
    }

    @Test
    fun `an unrecognised value never shows an ad on any scale`() {
        val unknown = listOf(-1, 99, 1_000, Int.MIN_VALUE, Int.MAX_VALUE)

        unknown.forEach { id ->
            assertEquals("slot($id)", AdSlotStyle.Hidden, AdRemoteStyles.slot(id))
            assertEquals("banner($id)", AdSlotStyle.Hidden, AdRemoteStyles.banner(id))
            assertEquals("native($id)", AdSlotStyle.Hidden, AdRemoteStyles.native(id))
            assertEquals("fullscreen($id)", FullscreenAdStyle.Hidden, AdRemoteStyles.fullscreen(id))
            assertEquals(
                "rewardOffer($id)",
                FullscreenAdStyle.Hidden,
                AdRemoteStyles.rewardOffer(id),
            )
        }
    }

    @Test
    fun `an explicit banner style overrides the configured one`() {
        val configured = AdRemoteStyles.slot(1)

        assertEquals(
            BannerStyle.COLLAPSIBLE_BOTTOM,
            configured.bannerStyleOr(BannerStyle.COLLAPSIBLE_BOTTOM),
        )
    }

    @Test
    fun `the configured banner style is used when the caller names none`() {
        assertEquals(BannerStyle.COLLAPSIBLE_TOP, AdRemoteStyles.slot(2).bannerStyleOr(null))
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
        val bannerPlacement = AdRemoteStyles.banner(1)

        assertNull("a native placement must not fall back to a banner",
            nativePlacement.bannerStyleOr(null))
        assertNull("a banner placement must not fall back to a native ad",
            bannerPlacement.nativeDesignOr(null))
    }

}
