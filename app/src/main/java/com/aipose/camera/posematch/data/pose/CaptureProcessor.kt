package com.aipose.camera.posematch.data.pose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoGeometry
import com.aipose.camera.posematch.domain.models.PhotoSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class CaptureProcessor(private val gradingEngine: PhotoGradingEngine) {

    suspend fun readSize(sourcePath: String): PhotoSize = withContext(Dispatchers.IO) {
        runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourcePath, options)
            val isQuarterTurned = when (readExifOrientation(sourcePath)) {
                ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_ROTATE_270 -> true
                else -> false
            }
            if (isQuarterTurned) {
                PhotoSize(options.outHeight, options.outWidth)
            } else {
                PhotoSize(options.outWidth, options.outHeight)
            }
        }.getOrDefault(PhotoSize(0, 0))
    }

    suspend fun writeEditedCopy(
        sourcePath: String,
        grade: ColorGrade?,
        geometry: PhotoGeometry,
        targetPath: String
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val decoded = BitmapFactory.decodeFile(sourcePath) ?: return@runCatching false
            var bitmap = applyExifRotation(decoded, readExifOrientation(sourcePath))
            if (geometry.isFlippedHorizontally || geometry.isFlippedVertically) {
                bitmap = flip(bitmap, geometry.isFlippedHorizontally, geometry.isFlippedVertically)
            }
            if (geometry.straightenDegrees != 0f) {
                bitmap = straighten(bitmap, geometry.straightenDegrees)
            }
            if (geometry.rotationDegrees % FULL_TURN != 0) {
                bitmap = rotate(bitmap, geometry.rotationDegrees)
            }
            geometry.cropAspect?.let { aspect -> bitmap = centerCrop(bitmap, aspect) }
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

    private fun flip(source: Bitmap, horizontally: Boolean, vertically: Boolean): Bitmap {
        val matrix = Matrix().apply {
            postScale(
                if (horizontally) -1f else 1f,
                if (vertically) -1f else 1f,
                source.width / 2f,
                source.height / 2f,
            )
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun straighten(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated =
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        val radians = Math.toRadians(abs(degrees).toDouble())
        val cosine = cos(radians)
        val sine = sin(radians)
        val denominator = cosine * cosine - sine * sine
        if (denominator <= MIN_DENOMINATOR) return rotated
        val width = source.width.toDouble()
        val height = source.height.toDouble()
        val insetWidth = ((width * cosine - height * sine) / denominator).toInt()
        val insetHeight = ((height * cosine - width * sine) / denominator).toInt()
        if (insetWidth < 1 || insetHeight < 1) return rotated
        val cropWidth = insetWidth.coerceAtMost(rotated.width)
        val cropHeight = insetHeight.coerceAtMost(rotated.height)
        return Bitmap.createBitmap(
            rotated,
            (rotated.width - cropWidth) / 2,
            (rotated.height - cropHeight) / 2,
            cropWidth,
            cropHeight,
        )
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
        const val MIN_DENOMINATOR = 0.0001
    }
}
