package com.aipose.camera.posematch.data.pose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.aipose.camera.posematch.domain.models.ColorGrade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CaptureProcessor(private val gradingEngine: PhotoGradingEngine) {

    suspend fun writeEditedCopy(
        sourcePath: String,
        grade: ColorGrade?,
        rotationDegrees: Int,
        cropAspect: Float?,
        targetPath: String
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val decoded = BitmapFactory.decodeFile(sourcePath) ?: return@runCatching false
            var bitmap = applyExifRotation(decoded, readExifOrientation(sourcePath))
            if (rotationDegrees % FULL_TURN != 0) bitmap = rotate(bitmap, rotationDegrees)
            if (cropAspect != null) bitmap = centerCrop(bitmap, cropAspect)
            bitmap = gradingEngine.bake(bitmap, grade)
            File(targetPath).outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            }
            true
        }.getOrDefault(false)
    }

    private fun readExifOrientation(path: String): Int = runCatching {
        ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun applyExifRotation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(QUARTER_TURN)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(HALF_TURN)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(THREE_QUARTER_TURN)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return source
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun rotate(source: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun centerCrop(source: Bitmap, aspect: Float): Bitmap {
        val width = source.width
        val height = source.height
        val currentAspect = width.toFloat() / height
        return if (currentAspect > aspect) {
            val croppedWidth = (height * aspect).toInt().coerceIn(1, width)
            Bitmap.createBitmap(source, (width - croppedWidth) / 2, 0, croppedWidth, height)
        } else {
            val croppedHeight = (width / aspect).toInt().coerceIn(1, height)
            Bitmap.createBitmap(source, 0, (height - croppedHeight) / 2, width, croppedHeight)
        }
    }

    private companion object {
        const val JPEG_QUALITY = 95
        const val QUARTER_TURN = 90f
        const val HALF_TURN = 180f
        const val THREE_QUARTER_TURN = 270f
        const val FULL_TURN = 360
    }
}
