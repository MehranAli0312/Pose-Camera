package com.aipose.camera.posematch.domain.models

data class PhotoSize(val width: Int, val height: Int) {
    val isValid: Boolean get() = width > 0 && height > 0

    fun croppedTo(aspect: Float?): PhotoSize {
        if (aspect == null || !isValid) return this
        val currentAspect = width.toFloat() / height
        return if (currentAspect > aspect) {
            PhotoSize((height * aspect).toInt().coerceIn(1, width), height)
        } else {
            PhotoSize(width, (width / aspect).toInt().coerceIn(1, height))
        }
    }

    fun rotatedBy(degrees: Int): PhotoSize =
        if (degrees % STRAIGHT_ANGLE == 0) this else PhotoSize(height, width)

    private companion object {
        const val STRAIGHT_ANGLE = 180
    }
}
