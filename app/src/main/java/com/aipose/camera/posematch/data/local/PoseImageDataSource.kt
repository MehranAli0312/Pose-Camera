package com.aipose.camera.posematch.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PoseImageDataSource(private val context: Context) {

    suspend fun decode(imagePath: String, sampleSize: Int = DEFAULT_SAMPLE_SIZE): Bitmap? =
        withContext(Dispatchers.IO) {
            val assetPath = assetPathOf(imagePath)
            if (assetPath != null) decodeAsset(assetPath, sampleSize) else decodeFile(imagePath, sampleSize)
        }

    private fun decodeAsset(assetPath: String, sampleSize: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        for (candidate in extensionCandidates(assetPath)) {
            val bitmap = runCatching {
                context.assets.open(candidate).use { BitmapFactory.decodeStream(it, null, options) }
            }.getOrNull()
            if (bitmap != null) return bitmap
        }
        return null
    }

    private fun decodeFile(path: String, sampleSize: Int): Bitmap? {
        val file = File(path.removePrefix(FILE_SCHEME))
        if (!file.exists()) return null
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return runCatching { BitmapFactory.decodeFile(file.absolutePath, options) }.getOrNull()
    }

    private fun extensionCandidates(assetPath: String): List<String> = buildList {
        add(assetPath)
        val dot = assetPath.lastIndexOf('.')
        if (dot > 0) {
            val base = assetPath.substring(0, dot)
            IMAGE_EXTENSIONS.forEach { add("$base.$it") }
        }
    }.distinct()

    companion object {
        private const val ASSET_SCHEME = "file:///android_asset/"
        private const val FILE_SCHEME = "file://"
        private const val DEFAULT_SAMPLE_SIZE = 2
        private val IMAGE_EXTENSIONS = listOf("webp", "jpg", "jpeg", "png")

        fun assetPathOf(imagePath: String): String? =
            if (imagePath.startsWith(ASSET_SCHEME)) imagePath.removePrefix(ASSET_SCHEME) else null
    }
}
