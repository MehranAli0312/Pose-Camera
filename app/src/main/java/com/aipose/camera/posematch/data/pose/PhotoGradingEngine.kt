package com.aipose.camera.posematch.data.pose

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoAdjustments
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.ReferenceLook
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

class PhotoGradingEngine {

    fun gradeFor(filterId: PhotoFilterId, autoGrade: ColorGrade?): ColorGrade? = when (filterId) {
        PhotoFilterId.Auto -> autoGrade
        PhotoFilterId.Original -> null
        else -> presets[filterId]
    }

    fun adjustmentGrade(adjustments: PhotoAdjustments): ColorGrade? {
        if (adjustments.isNeutral) return null
        val matrix = ColorMatrix()
        with(adjustments) {
            if (exposure != 0f) {
                val gain = 2.0.pow(exposure.toDouble()).toFloat()
                matrix.postConcat(channelGain(gain, gain, gain))
            }
            if (brightness != 0f) {
                val lift = brightness * BRIGHTNESS_RANGE
                matrix.postConcat(channelGain(1f, 1f, 1f, lift, lift, lift))
            }
            if (contrast != 0f) {
                val scale = (1f + contrast * CONTRAST_RANGE).coerceIn(MIN_CONTRAST, MAX_CONTRAST)
                matrix.postConcat(contrastMatrix(scale, MID_GRAY * (1f - scale)))
            }
            if (saturation != 0f) {
                matrix.postConcat(saturationMatrix((1f + saturation).coerceIn(0f, MAX_SATURATION)))
            }
            if (warmth != 0f) {
                val shift = warmth * WARMTH_RANGE
                matrix.postConcat(channelGain(1f + shift, 1f, 1f - shift))
            }
            if (tint != 0f) {
                val shift = tint * TINT_RANGE
                matrix.postConcat(channelGain(1f + shift * HALF, 1f - shift, 1f + shift * HALF))
            }
            if (hue != 0f) {
                matrix.postConcat(hueMatrix(hue * HUE_RANGE_DEGREES))
            }
            if (fade != 0f) {
                val scale = (1f - fade * FADE_SCALE_RANGE).coerceIn(MIN_FADE_SCALE, MAX_FADE_SCALE)
                val lift = fade * FADE_LIFT_RANGE
                matrix.postConcat(channelGain(scale, scale, scale, lift, lift, lift))
            }
        }
        return ColorGrade(matrix.array.toList())
    }

    fun compose(base: ColorGrade?, overlay: ColorGrade?): ColorGrade? {
        if (base == null) return overlay
        if (overlay == null) return base
        val matrix = ColorMatrix(base.values.toFloatArray())
        matrix.postConcat(ColorMatrix(overlay.values.toFloatArray()))
        return ColorGrade(matrix.array.toList())
    }

    fun extractLook(bitmap: Bitmap): ReferenceLook? {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return null

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        var saturationSum = 0.0
        var sampled = 0
        val step = max(1, pixels.size / SAMPLE_BUDGET)

        var index = 0
        while (index < pixels.size) {
            val pixel = pixels[index]
            val red = (pixel shr RED_SHIFT) and CHANNEL_MASK
            val green = (pixel shr GREEN_SHIFT) and CHANNEL_MASK
            val blue = pixel and CHANNEL_MASK
            redSum += red
            greenSum += green
            blueSum += blue
            val brightest = max(red, max(green, blue))
            val darkest = min(red, min(green, blue))
            if (brightest > 0) saturationSum += (brightest - darkest).toDouble() / brightest
            sampled++
            index += step
        }
        if (sampled == 0) return null

        val redMean = redSum.toFloat() / sampled
        val greenMean = greenSum.toFloat() / sampled
        val blueMean = blueSum.toFloat() / sampled
        val averageSaturation = (saturationSum / sampled).toFloat()
        val gray = max(1f, (redMean + greenMean + blueMean) / CHANNEL_COUNT)

        fun gain(channelMean: Float) =
            (1f + ((channelMean / gray) - 1f) * CAST_DAMPING).coerceIn(MIN_CAST_GAIN, MAX_CAST_GAIN)

        val saturationFactor = (AUTO_SATURATION_BASE + averageSaturation * AUTO_SATURATION_SPAN)
            .coerceIn(MIN_AUTO_SATURATION, MAX_AUTO_SATURATION)
        val luma = LUMA_RED * redMean + LUMA_GREEN * greenMean + LUMA_BLUE * blueMean
        val brightnessLift = ((luma - MID_GRAY) * AUTO_BRIGHTNESS_DAMPING)
            .coerceIn(-MAX_AUTO_BRIGHTNESS, MAX_AUTO_BRIGHTNESS)

        val grade = combine(
            saturationMatrix(saturationFactor),
            channelGain(
                gain(redMean),
                gain(greenMean),
                gain(blueMean),
                brightnessLift,
                brightnessLift,
                brightnessLift
            )
        )
        return ReferenceLook(
            grade = grade,
            dominantColor = Color.rgb(
                redMean.toInt().coerceIn(0, CHANNEL_MASK),
                greenMean.toInt().coerceIn(0, CHANNEL_MASK),
                blueMean.toInt().coerceIn(0, CHANNEL_MASK)
            )
        )
    }

    fun bake(source: Bitmap, grade: ColorGrade?): Bitmap {
        if (grade == null) return source
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            isFilterBitmap = true
            colorFilter = ColorMatrixColorFilter(ColorMatrix(grade.values.toFloatArray()))
        }
        Canvas(output).drawBitmap(source, 0f, 0f, paint)
        return output
    }

    private fun saturationMatrix(value: Float) = ColorMatrix().apply { setSaturation(value) }

    private fun contrastMatrix(scale: Float, translate: Float) = ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )

    private fun channelGain(
        red: Float,
        green: Float,
        blue: Float,
        redTranslate: Float = 0f,
        greenTranslate: Float = 0f,
        blueTranslate: Float = 0f
    ) = ColorMatrix(
        floatArrayOf(
            red, 0f, 0f, 0f, redTranslate,
            0f, green, 0f, 0f, greenTranslate,
            0f, 0f, blue, 0f, blueTranslate,
            0f, 0f, 0f, 1f, 0f
        )
    )

    private fun hueMatrix(degrees: Float): ColorMatrix {
        val radians = Math.toRadians(degrees.toDouble())
        val cosine = cos(radians).toFloat()
        val sine = sin(radians).toFloat()
        return ColorMatrix(
            floatArrayOf(
                LUMA_RED + cosine * (1 - LUMA_RED) - sine * LUMA_RED,
                LUMA_GREEN - cosine * LUMA_GREEN - sine * LUMA_GREEN,
                LUMA_BLUE - cosine * LUMA_BLUE + sine * (1 - LUMA_BLUE),
                0f, 0f,
                LUMA_RED - cosine * LUMA_RED + sine * HUE_GREEN_RED,
                LUMA_GREEN + cosine * (1 - LUMA_GREEN) + sine * HUE_GREEN_GREEN,
                LUMA_BLUE - cosine * LUMA_BLUE + sine * HUE_GREEN_BLUE,
                0f, 0f,
                LUMA_RED - cosine * LUMA_RED - sine * (1 - LUMA_RED),
                LUMA_GREEN - cosine * LUMA_GREEN + sine * LUMA_GREEN,
                LUMA_BLUE + cosine * (1 - LUMA_BLUE) + sine * LUMA_BLUE,
                0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    private fun combine(vararg parts: ColorMatrix): ColorGrade {
        val matrix = ColorMatrix()
        parts.forEach { matrix.postConcat(it) }
        return ColorGrade(matrix.array.toList())
    }

    private val presets: Map<PhotoFilterId, ColorGrade> by lazy {
        mapOf(
            PhotoFilterId.Vivid to combine(saturationMatrix(1.4f), contrastMatrix(1.12f, -14f)),
            PhotoFilterId.Golden to combine(
                saturationMatrix(1.06f),
                channelGain(1.12f, 1.02f, 0.88f),
                contrastMatrix(1.03f, -4f)
            ),
            PhotoFilterId.Sunrise to combine(
                saturationMatrix(1.12f),
                channelGain(1.16f, 1.05f, 0.92f, 12f, 6f, 2f),
                contrastMatrix(1.02f, -2f)
            ),
            PhotoFilterId.Sunset to combine(
                saturationMatrix(1.2f),
                channelGain(1.24f, 0.99f, 0.84f, 10f, 0f, 4f),
                contrastMatrix(1.06f, -8f)
            ),
            PhotoFilterId.Azure to combine(
                saturationMatrix(1.04f),
                channelGain(0.9f, 1.0f, 1.13f)
            ),
            PhotoFilterId.Cinema to combine(
                saturationMatrix(0.95f),
                channelGain(1.12f, 0.98f, 1.06f, 6f, 0f, 8f),
                contrastMatrix(1.08f, -14f)
            ),
            PhotoFilterId.Fade to combine(saturationMatrix(0.85f), contrastMatrix(0.82f, 26f)),
            PhotoFilterId.Noir to combine(saturationMatrix(0f), contrastMatrix(1.45f, -46f)),
            PhotoFilterId.Vintage to combine(sepiaMatrix(), contrastMatrix(0.9f, 12f)),
            PhotoFilterId.Mono to combine(saturationMatrix(0f))
        )
    }

    private fun sepiaMatrix() = ColorMatrix(
        floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )

    private companion object {
        const val MID_GRAY = 128f
        const val HALF = 0.5f
        const val CHANNEL_COUNT = 3f
        const val CHANNEL_MASK = 0xFF
        const val RED_SHIFT = 16
        const val GREEN_SHIFT = 8

        const val BRIGHTNESS_RANGE = 80f
        const val CONTRAST_RANGE = 0.6f
        const val MIN_CONTRAST = 0.2f
        const val MAX_CONTRAST = 2.2f
        const val MAX_SATURATION = 2f
        const val WARMTH_RANGE = 0.18f
        const val TINT_RANGE = 0.15f
        const val HUE_RANGE_DEGREES = 30f
        const val FADE_SCALE_RANGE = 0.18f
        const val MIN_FADE_SCALE = 0.5f
        const val MAX_FADE_SCALE = 1.2f
        const val FADE_LIFT_RANGE = 40f

        const val LUMA_RED = 0.213f
        const val LUMA_GREEN = 0.715f
        const val LUMA_BLUE = 0.072f
        const val HUE_GREEN_RED = 0.143f
        const val HUE_GREEN_GREEN = 0.140f
        const val HUE_GREEN_BLUE = -0.283f

        const val SAMPLE_BUDGET = 4096
        const val CAST_DAMPING = 0.6f
        const val MIN_CAST_GAIN = 0.85f
        const val MAX_CAST_GAIN = 1.25f
        const val AUTO_SATURATION_BASE = 0.85f
        const val AUTO_SATURATION_SPAN = 0.7f
        const val MIN_AUTO_SATURATION = 0.7f
        const val MAX_AUTO_SATURATION = 1.25f
        const val AUTO_BRIGHTNESS_DAMPING = 0.12f
        const val MAX_AUTO_BRIGHTNESS = 20f
    }
}
