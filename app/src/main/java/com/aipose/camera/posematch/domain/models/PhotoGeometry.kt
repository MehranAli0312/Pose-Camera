package com.aipose.camera.posematch.domain.models

data class PhotoGeometry(
    val rotationDegrees: Int = 0,
    val straightenDegrees: Float = 0f,
    val isFlippedHorizontally: Boolean = false,
    val isFlippedVertically: Boolean = false,
    val cropAspect: Float? = null
) {
    val isNeutral: Boolean
        get() = rotationDegrees == 0 && straightenDegrees == 0f &&
            !isFlippedHorizontally && !isFlippedVertically && cropAspect == null

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

    fun withCropAspect(aspect: Float?): PhotoGeometry = copy(cropAspect = aspect)

    companion object {
        const val MIN_STRAIGHTEN = -45f
        const val MAX_STRAIGHTEN = 45f
        private const val QUARTER_TURN = 90
        private const val FULL_TURN = 360
    }
}
