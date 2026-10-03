package com.aipose.camera.posematch.ui.screens.camera.models

data class OverlayTransform(
    val opacity: Float = DEFAULT_OPACITY,
    val scale: Float = DEFAULT_SCALE,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f
) {
    fun withOpacity(value: Float): OverlayTransform =
        copy(opacity = value.coerceIn(MIN_OPACITY, MAX_OPACITY))

    fun withGestures(scale: Float, offsetX: Float, offsetY: Float, rotation: Float): OverlayTransform =
        copy(
            scale = scale.coerceIn(MIN_SCALE, MAX_SCALE),
            offsetX = offsetX.coerceIn(-MAX_OFFSET, MAX_OFFSET),
            offsetY = offsetY.coerceIn(-MAX_OFFSET, MAX_OFFSET),
            rotation = rotation
        )

    companion object {
        const val DEFAULT_OPACITY = 0.45f
        const val DEFAULT_SCALE = 1f
        const val MIN_OPACITY = 0f
        const val MAX_OPACITY = 1f
        const val MIN_SCALE = 0.2f
        const val MAX_SCALE = 4f
        const val MAX_OFFSET = 0.8f
    }
}
