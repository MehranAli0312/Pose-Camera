package com.aipose.camera.posematch.domain

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device background removal for reference/pose images. Uses ML Kit Selfie Segmentation
 * (model bundled with the app — free, fully offline, no network). Returns the subject with a
 * transparent background so only the pose silhouette overlays the live camera.
 */
object SubjectExtractor {

    /** Returns a copy of [bitmap] with the background removed, or null if segmentation fails. */
    suspend fun removeBackground(bitmap: Bitmap): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build()
            val segmenter = Segmentation.getClient(options)
            try {
                val input = InputImage.fromBitmap(bitmap, 0)
                val mask = Tasks.await(segmenter.process(input))
                applyMask(bitmap, mask)
            } finally {
                segmenter.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Builds an ARGB bitmap where per-pixel alpha follows the foreground (person) confidence. */
    private fun applyMask(src: Bitmap, mask: SegmentationMask): Bitmap {
        val w = mask.width
        val h = mask.height
        val scaled = if (src.width != w || src.height != h) {
            Bitmap.createScaledBitmap(src, w, h, true)
        } else src

        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)

        val buffer = mask.buffer
        buffer.rewind()
        // Soft edges: fully transparent below 0.35, opaque above 0.75, ramp between.
        val lo = 0.35f
        val hi = 0.75f
        for (i in 0 until w * h) {
            val conf = buffer.float
            val alpha = when {
                conf <= lo -> 0
                conf >= hi -> 255
                else -> (((conf - lo) / (hi - lo)) * 255f).toInt()
            }
            pixels[i] = (alpha shl 24) or (pixels[i] and 0x00FFFFFF)
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }
}
