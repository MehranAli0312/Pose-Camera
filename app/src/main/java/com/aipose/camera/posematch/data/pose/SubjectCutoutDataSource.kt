package com.aipose.camera.posematch.data.pose

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubjectCutoutDataSource {

    suspend fun removeBackground(bitmap: Bitmap): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val segmenter = Segmentation.getClient(
                SelfieSegmenterOptions.Builder()
                    .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                    .build()
            )
            try {
                val mask = Tasks.await(segmenter.process(InputImage.fromBitmap(bitmap, 0)))
                applyMask(bitmap, mask)
            } finally {
                segmenter.close()
            }
        }.getOrNull()
    }

    private fun applyMask(source: Bitmap, mask: SegmentationMask): Bitmap {
        val width = mask.width
        val height = mask.height
        val scaled = if (source.width != width || source.height != height) {
            Bitmap.createScaledBitmap(source, width, height, true)
        } else {
            source
        }

        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        val buffer = mask.buffer
        buffer.rewind()
        for (index in pixels.indices) {
            val confidence = buffer.float
            val alpha = when {
                confidence <= TRANSPARENT_BELOW -> 0
                confidence >= OPAQUE_ABOVE -> MAX_ALPHA
                else -> {
                    val ramp = (confidence - TRANSPARENT_BELOW) / (OPAQUE_ABOVE - TRANSPARENT_BELOW)
                    (ramp * MAX_ALPHA).toInt()
                }
            }
            pixels[index] = (alpha shl ALPHA_SHIFT) or (pixels[index] and RGB_MASK)
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    private companion object {
        const val TRANSPARENT_BELOW = 0.35f
        const val OPAQUE_ABOVE = 0.75f
        const val MAX_ALPHA = 255
        const val ALPHA_SHIFT = 24
        const val RGB_MASK = 0x00FFFFFF
    }
}
