package com.example.ads


data class NativeAdColors(
    val ctaBackground: Int? = null,
    val ctaText: Int? = null,
    val labelBackground: Int? = null,
    val labelText: Int? = null,
) {
    companion object {
        private const val RGB_LENGTH = 6
        private const val ARGB_LENGTH = 8
        private const val OPAQUE_ALPHA = "FF"
        private const val HEX_RADIX = 16

        fun parseHex(value: String): Int? {
            val digits = value.trim().removePrefix("#")
            val argb = when (digits.length) {
                RGB_LENGTH -> OPAQUE_ALPHA + digits
                ARGB_LENGTH -> digits
                else -> return null
            }
            return argb.toLongOrNull(HEX_RADIX)?.toInt()
        }
    }
}
