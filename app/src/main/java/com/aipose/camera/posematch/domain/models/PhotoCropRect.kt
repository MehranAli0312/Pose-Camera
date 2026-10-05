package com.aipose.camera.posematch.domain.models

data class PhotoCropRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
) {
    val width: Float get() = right - left

    val height: Float get() = bottom - top

    val isFull: Boolean get() = left <= 0f && top <= 0f && right >= 1f && bottom >= 1f

    companion object {
        val Full = PhotoCropRect()

        fun centeredFor(aspect: Float?, imageAspect: Float): PhotoCropRect {
            if (aspect == null || aspect <= 0f || imageAspect <= 0f) return Full
            return if (imageAspect > aspect) {
                val width = aspect / imageAspect
                PhotoCropRect((1f - width) / 2f, 0f, (1f + width) / 2f, 1f)
            } else {
                val height = imageAspect / aspect
                PhotoCropRect(0f, (1f - height) / 2f, 1f, (1f + height) / 2f)
            }
        }
    }
}
