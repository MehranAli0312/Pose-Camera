package com.aipose.camera.posematch.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.max
import kotlin.math.min

/**
 * A single professional look. [matrix] is a 4x5 [ColorMatrix] array (20 floats) or
 * null for "no processing". The special "auto" filter carries a null matrix here and
 * is resolved at runtime from the reference overlay image (see [PhotoFilters.extractLook]).
 */
data class PhotoFilter(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val swatch: Color,
    val matrix: FloatArray?
) {
    override fun equals(other: Any?) = other is PhotoFilter && other.id == id
    override fun hashCode() = id.hashCode()
}

/**
 * On-device, dependency-free photo grading. Every look is a plain Android [ColorMatrix]
 * so it can be applied identically to the live preview (via RenderEffect on API 31+) and
 * baked into the captured JPEG at full quality on every API level.
 */
object PhotoFilters {

    // ---- matrix builders ------------------------------------------------------------------

    private fun saturation(s: Float) = ColorMatrix().apply { setSaturation(s) }

    private fun contrast(scale: Float, translate: Float) = ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )

    private fun channelGain(
        r: Float, g: Float, b: Float,
        tr: Float = 0f, tg: Float = 0f, tb: Float = 0f
    ) = ColorMatrix(
        floatArrayOf(
            r, 0f, 0f, 0f, tr,
            0f, g, 0f, 0f, tg,
            0f, 0f, b, 0f, tb,
            0f, 0f, 0f, 1f, 0f
        )
    )

    private val sepia = ColorMatrix(
        floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )

    private fun combine(vararg parts: ColorMatrix): FloatArray {
        val out = ColorMatrix()
        parts.forEach { out.postConcat(it) }
        return out.array
    }

    // ---- preset looks ---------------------------------------------------------------------

    private val vivid = combine(saturation(1.4f), contrast(1.12f, -14f))
    private val golden = combine(saturation(1.06f), channelGain(1.12f, 1.02f, 0.88f), contrast(1.03f, -4f))
    // Soft, warm dawn glow — gentle pink/gold lift with a touch of brightness.
    private val sunrise = combine(saturation(1.12f), channelGain(1.16f, 1.05f, 0.92f, 12f, 6f, 2f), contrast(1.02f, -2f))
    // Deep golden-hour warmth — richer oranges, cooler-suppressed blues, more contrast.
    private val sunset = combine(saturation(1.2f), channelGain(1.24f, 0.99f, 0.84f, 10f, 0f, 4f), contrast(1.06f, -8f))
    private val teal = combine(saturation(1.04f), channelGain(0.9f, 1.0f, 1.13f))
    private val cinematic = combine(saturation(0.95f), channelGain(1.12f, 0.98f, 1.06f, 6f, 0f, 8f), contrast(1.08f, -14f))
    private val fade = combine(saturation(0.85f), contrast(0.82f, 26f))
    private val noir = combine(saturation(0f), contrast(1.45f, -46f))
    private val vintage = combine(sepia, contrast(0.9f, 12f))
    private val mono = saturation(0f).array

    /** Selectable looks after the runtime "Auto" + neutral "Original". */
    private val presets: List<PhotoFilter> = listOf(
        PhotoFilter("vivid", "Vivid", Icons.Filled.Bolt, Color(0xFFFF5A5F), vivid),
        PhotoFilter("golden", "Golden", Icons.Filled.WbSunny, Color(0xFFE0A45E), golden),
        PhotoFilter("sunrise", "Sunrise", Icons.Filled.WbTwilight, Color(0xFFFFB27A), sunrise),
        PhotoFilter("sunset", "Sunset", Icons.Filled.Brightness4, Color(0xFFE8703A), sunset),
        PhotoFilter("teal", "Azure", Icons.Filled.AcUnit, Color(0xFF3FA7B5), teal),
        PhotoFilter("cinematic", "Cinema", Icons.Filled.Movie, Color(0xFF2E6E7E), cinematic),
        PhotoFilter("fade", "Fade", Icons.Filled.Gradient, Color(0xFFB8AEA0), fade),
        PhotoFilter("noir", "Noir", Icons.Filled.Contrast, Color(0xFF111111), noir),
        PhotoFilter("vintage", "Vintage", Icons.Filled.FilterVintage, Color(0xFF9B7B4E), vintage),
        PhotoFilter("mono", "Mono", Icons.Filled.FilterBAndW, Color(0xFF808080), mono)
    )

    const val ID_AUTO = "auto"
    const val ID_ORIGINAL = "original"

    /** The runtime-extracted look (index 0) and the neutral pass-through (index 1). */
    val autoFilter = PhotoFilter(ID_AUTO, "Auto", Icons.Filled.AutoAwesome, Color(0xFF6366F1), null)
    val originalFilter = PhotoFilter(ID_ORIGINAL, "Original", Icons.Filled.RadioButtonUnchecked, Color(0xFF2E2E33), null)

    /** Full ordered strip: Auto (from overlay) first, then Original, then the presets. */
    val strip: List<PhotoFilter> = listOf(autoFilter, originalFilter) + presets

    private fun presetMatrix(id: String): FloatArray? = presets.firstOrNull { it.id == id }?.matrix

    /**
     * Resolves the effective color matrix for a selected filter id.
     * [autoMatrix] is the look extracted from the current overlay (may be null before it loads).
     */
    fun matrixFor(id: String, autoMatrix: FloatArray?): FloatArray? = when (id) {
        ID_AUTO -> autoMatrix
        ID_ORIGINAL -> null
        else -> presetMatrix(id)
    }

    // ---- iPhone-style manual adjustments --------------------------------------------------

    /**
     * Builds a live-adjustment [ColorMatrix] from the editor sliders. Each parameter is
     * neutral at 0f and ranges roughly -1f..1f. Returns null when everything is neutral so
     * callers can skip processing entirely. Every effect here is a pure linear color
     * transform, so the live preview matches the baked result exactly.
     *
     * [exposure] multiplicative gain (photographic stops), [brightness] additive lift,
     * [contrast] scales around mid-gray, [saturation] fades to gray / boosts vividness,
     * [warmth] amber↔blue white balance, [tint] green↔magenta balance, [hue] rotates all
     * colors around the luminance axis, [fade] lifts blacks for a soft film look.
     */
    fun adjustmentMatrix(
        exposure: Float = 0f,
        brightness: Float = 0f,
        contrast: Float = 0f,
        saturation: Float = 0f,
        warmth: Float = 0f,
        tint: Float = 0f,
        hue: Float = 0f,
        fade: Float = 0f
    ): FloatArray? {
        if (exposure == 0f && brightness == 0f && contrast == 0f && saturation == 0f &&
            warmth == 0f && tint == 0f && hue == 0f && fade == 0f
        ) return null
        val m = ColorMatrix()
        if (exposure != 0f) {
            val f = Math.pow(2.0, exposure.toDouble()).toFloat() // ±1 stop at the extremes
            m.postConcat(channelGain(f, f, f))
        }
        if (brightness != 0f) {
            val t = brightness * 80f
            m.postConcat(channelGain(1f, 1f, 1f, t, t, t))
        }
        if (contrast != 0f) {
            val scale = (1f + contrast * 0.6f).coerceIn(0.2f, 2.2f)
            val tr = 128f * (1f - scale)
            m.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, tr,
                        0f, scale, 0f, 0f, tr,
                        0f, 0f, scale, 0f, tr,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }
        if (saturation != 0f) {
            m.postConcat(ColorMatrix().apply { setSaturation((1f + saturation).coerceIn(0f, 2f)) })
        }
        if (warmth != 0f) {
            val w = warmth * 0.18f
            m.postConcat(channelGain(1f + w, 1f, 1f - w))
        }
        if (tint != 0f) {
            // Positive → magenta (lift R/B, drop G); negative → green.
            val t = tint * 0.15f
            m.postConcat(channelGain(1f + t * 0.5f, 1f - t, 1f + t * 0.5f))
        }
        if (hue != 0f) {
            m.postConcat(hueMatrix(hue * 30f)) // ±30° rotation across the slider range
        }
        if (fade != 0f) {
            // Compress the tonal range and lift the black point for a matte finish.
            val scale = (1f - fade * 0.18f).coerceIn(0.5f, 1.2f)
            val lift = fade * 40f
            m.postConcat(channelGain(scale, scale, scale, lift, lift, lift))
        }
        return m.array
    }

    /** Luminance-preserving hue rotation by [degrees], as a standard 4x5 [ColorMatrix]. */
    private fun hueMatrix(degrees: Float): ColorMatrix {
        val rad = Math.toRadians(degrees.toDouble())
        val cos = kotlin.math.cos(rad).toFloat()
        val sin = kotlin.math.sin(rad).toFloat()
        val lr = 0.213f; val lg = 0.715f; val lb = 0.072f
        return ColorMatrix(
            floatArrayOf(
                lr + cos * (1 - lr) + sin * (-lr), lg + cos * (-lg) + sin * (-lg), lb + cos * (-lb) + sin * (1 - lb), 0f, 0f,
                lr + cos * (-lr) + sin * (0.143f), lg + cos * (1 - lg) + sin * (0.140f), lb + cos * (-lb) + sin * (-0.283f), 0f, 0f,
                lr + cos * (-lr) + sin * (-(1 - lr)), lg + cos * (-lg) + sin * (lg), lb + cos * (1 - lb) + sin * (lb), 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    /** Post-concatenates an adjustment matrix [over] on top of a base look [base]; either may be null. */
    fun compose(base: FloatArray?, over: FloatArray?): FloatArray? {
        if (base == null) return over
        if (over == null) return base
        val m = ColorMatrix(base)
        m.postConcat(ColorMatrix(over))
        return m.array
    }

    // ---- runtime "Auto" extraction from the reference overlay -----------------------------

    /**
     * Samples the reference overlay and derives a subtle grade that mimics its tone:
     * white-balance cast, overall saturation and exposure. Returns the matrix plus a
     * representative dominant color for the filter swatch.
     */
    fun extractLook(bmp: Bitmap): Pair<FloatArray, Int> {
        val w = bmp.width
        val h = bmp.height
        if (w <= 0 || h <= 0) return mono to android.graphics.Color.GRAY

        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)

        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var satSum = 0.0
        var count = 0
        val step = max(1, pixels.size / 4096)

        var i = 0
        while (i < pixels.size) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            rSum += r; gSum += g; bSum += b
            val mx = max(r, max(g, b))
            val mn = min(r, min(g, b))
            if (mx > 0) satSum += (mx - mn).toDouble() / mx
            count++
            i += step
        }
        if (count == 0) return mono to android.graphics.Color.GRAY

        val rM = rSum.toFloat() / count
        val gM = gSum.toFloat() / count
        val bM = bSum.toFloat() / count
        val avgSat = (satSum / count).toFloat()
        val gray = max(1f, (rM + gM + bM) / 3f)

        // Impart the reference's color cast, dampened so it flatters instead of overpowering.
        val damp = 0.6f
        fun gain(c: Float) = (1f + ((c / gray) - 1f) * damp).coerceIn(0.85f, 1.25f)
        val rGain = gain(rM)
        val gGain = gain(gM)
        val bGain = gain(bM)

        // Match overall vividness and exposure.
        val satFactor = (0.85f + avgSat * 0.7f).coerceIn(0.7f, 1.25f)
        val luma = 0.2126f * rM + 0.7152f * gM + 0.0722f * bM
        val bright = ((luma - 128f) * 0.12f).coerceIn(-20f, 20f)

        val matrix = combine(
            saturation(satFactor),
            channelGain(rGain, gGain, bGain, bright, bright, bright)
        )
        val dominant = android.graphics.Color.rgb(
            rM.toInt().coerceIn(0, 255),
            gM.toInt().coerceIn(0, 255),
            bM.toInt().coerceIn(0, 255)
        )
        return matrix to dominant
    }

    // ---- baking a look into a captured bitmap ---------------------------------------------

    /** Returns a filtered copy of [src]. Caller owns recycling of [src] if desired. */
    fun apply(src: Bitmap, matrix: FloatArray?): Bitmap {
        if (matrix == null) return src
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint().apply {
            isFilterBitmap = true
            colorFilter = ColorMatrixColorFilter(ColorMatrix(matrix))
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }
}
