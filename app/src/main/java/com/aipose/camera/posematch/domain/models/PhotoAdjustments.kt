package com.aipose.camera.posematch.domain.models

data class PhotoAdjustments(
    val exposure: Float = 0f,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val warmth: Float = 0f,
    val tint: Float = 0f,
    val hue: Float = 0f,
    val fade: Float = 0f
) {
    val isNeutral: Boolean
        get() = exposure == 0f && brightness == 0f && contrast == 0f && saturation == 0f &&
            warmth == 0f && tint == 0f && hue == 0f && fade == 0f
}
