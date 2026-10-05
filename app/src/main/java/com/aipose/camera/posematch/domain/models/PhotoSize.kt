package com.aipose.camera.posematch.domain.models

data class PhotoSize(val width: Int, val height: Int) {
    val isValid: Boolean get() = width > 0 && height > 0

    val aspect: Float? get() = if (isValid) width.toFloat() / height else null

    fun croppedTo(rect: PhotoCropRect): PhotoSize {
        if (!isValid) return this
        return PhotoSize(
            (width * rect.width).toInt().coerceIn(1, width),
            (height * rect.height).toInt().coerceIn(1, height),
        )
    }

    fun rotatedBy(degrees: Int): PhotoSize =
        if (degrees % STRAIGHT_ANGLE == 0) this else PhotoSize(height, width)

    private companion object {
        const val STRAIGHT_ANGLE = 180
    }
}
