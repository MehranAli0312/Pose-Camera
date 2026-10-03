package com.example.ads

enum class BannerStyle {
    ADAPTIVE,

    INLINE,

    COLLAPSIBLE_TOP,

    COLLAPSIBLE_BOTTOM,

    MEDIUM_RECTANGLE,
}

enum class NativeAdDesign {
    SMALL,

    MEDIUM,

    LARGE,
}

sealed interface AdSlotStyle {

    data object Hidden : AdSlotStyle

    data class Banner(val style: BannerStyle) : AdSlotStyle

    data class Native(val design: NativeAdDesign) : AdSlotStyle

    data class BannerWithNativeBackfill(
        val style: BannerStyle,
        val backfill: NativeAdDesign,
    ) : AdSlotStyle
}

internal fun AdSlotStyle.bannerStyleOr(explicit: BannerStyle?): BannerStyle? =
    explicit ?: (this as? AdSlotStyle.Banner)?.style

internal fun AdSlotStyle.nativeDesignOr(explicit: NativeAdDesign?): NativeAdDesign? =
    explicit ?: (this as? AdSlotStyle.Native)?.design
