package com.aipose.camera.posematch.ui.firebaseRemote

import com.example.ads.NativeAdColors
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NativeAdColorHexes(
    @SerialName("cta_bg") val ctaBackground: String = WHITE_HEX,
    @SerialName("cta_text") val ctaText: String = BRAND_BLUE_HEX,
    @SerialName("label_bg") val labelBackground: String = BRAND_BLUE_HEX,
    @SerialName("label_text") val labelText: String = WHITE_HEX,
) {
    fun toNativeAdColors(): NativeAdColors = NativeAdColors(
        ctaBackground = NativeAdColors.parseHex(ctaBackground),
        ctaText = NativeAdColors.parseHex(ctaText),
        labelBackground = NativeAdColors.parseHex(labelBackground),
        labelText = NativeAdColors.parseHex(labelText),
    )

    private companion object {
        const val WHITE_HEX = "#FFFFFF"
        const val BRAND_BLUE_HEX = "#077EFC"
    }
}
