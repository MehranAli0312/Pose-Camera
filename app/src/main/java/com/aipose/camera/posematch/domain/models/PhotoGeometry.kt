package com.aipose.camera.posematch.domain.models

data class PhotoGeometry(
    val rotationDegrees: Int = 0,
    val straightenDegrees: Float = 0f,
    val isFlippedHorizontally: Boolean = false,
    val isFlippedVertically: Boolean = false,
    val cropAspect: Float? = null,
    val cropRect: PhotoCropRect = PhotoCropRect.Full
) {
    val isNeutral: Boolean
        get() = rotationDegrees == 0 && straightenDegrees == 0f &&
            !isFlippedHorizontally && !isFlippedVertically && cropAspect == null &&
            cropRect.isFull

    fun rotated(): PhotoGeometry =
        copy(rotationDegrees = (rotationDegrees + QUARTER_TURN) % FULL_TURN)

    fun rotatedBack(): PhotoGeometry =
        copy(rotationDegrees = (rotationDegrees + FULL_TURN - QUARTER_TURN) % FULL_TURN)

    fun flippedHorizontally(): PhotoGeometry =
        copy(isFlippedHorizontally = !isFlippedHorizontally)

    fun flippedVertically(): PhotoGeometry =
        copy(isFlippedVertically = !isFlippedVertically)

    fun withStraighten(degrees: Float): PhotoGeometry =
        copy(straightenDegrees = degrees.coerceIn(MIN_STRAIGHTEN, MAX_STRAIGHTEN))

    fun withCrop(aspect: Float?, rect: PhotoCropRect): PhotoGeometry =
        copy(cropAspect = aspect, cropRect = rect)

    fun withCropRect(rect: PhotoCropRect): PhotoGeometry = copy(cropRect = rect)

    companion object {
        const val MIN_STRAIGHTEN = -45f
        const val MAX_STRAIGHTEN = 45f
        private const val QUARTER_TURN = 90
        private const val FULL_TURN = 360
    }
}
