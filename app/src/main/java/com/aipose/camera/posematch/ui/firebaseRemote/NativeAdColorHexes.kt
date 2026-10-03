package com.aipose.camera.posematch.ui.firebaseRemote

import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.NATIVE_AD_CTA_BG_COLOR
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.NATIVE_AD_CTA_TEXT_COLOR
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.NATIVE_AD_LABEL_BG_COLOR
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.NATIVE_AD_LABEL_TEXT_COLOR
import com.example.ads.NativeAdColors

data class NativeAdColorHexes(
    val ctaBackground: String = "",
    val ctaText: String = "",
    val labelBackground: String = "",
    val labelText: String = "",
) {
    fun toNativeAdColors(): NativeAdColors = NativeAdColors(
        ctaBackground = NativeAdColors.parseHex(ctaBackground),
        ctaText = NativeAdColors.parseHex(ctaText),
        labelBackground = NativeAdColors.parseHex(labelBackground),
        labelText = NativeAdColors.parseHex(labelText),
    )

    fun byKey(): Map<String, String> = mapOf(
        NATIVE_AD_CTA_BG_COLOR to ctaBackground,
        NATIVE_AD_CTA_TEXT_COLOR to ctaText,
        NATIVE_AD_LABEL_BG_COLOR to labelBackground,
        NATIVE_AD_LABEL_TEXT_COLOR to labelText,
    )

    companion object {
        inline fun read(valueOf: (key: String) -> String): NativeAdColorHexes = NativeAdColorHexes(
            ctaBackground = valueOf(NATIVE_AD_CTA_BG_COLOR).trim(),
            ctaText = valueOf(NATIVE_AD_CTA_TEXT_COLOR).trim(),
            labelBackground = valueOf(NATIVE_AD_LABEL_BG_COLOR).trim(),
            labelText = valueOf(NATIVE_AD_LABEL_TEXT_COLOR).trim(),
        )
    }
}
