package com.aipose.camera.posematch.data.local

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CaptureGalleryDataSource(private val context: Context) {

    suspend fun export(source: File, albumName: String?, displayName: String): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    insertViaMediaStore(source, sanitize(albumName), displayName)
                } else {
                    copyToPublicDirectory(source, sanitize(albumName), displayName)
                }
            }.getOrNull()
        }

    private fun insertViaMediaStore(source: File, album: String, displayName: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, JPEG_MIME_TYPE)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$ALBUM_ROOT/$album"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { output ->
            source.inputStream().use { it.copyTo(output) }
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    private fun copyToPublicDirectory(source: File, album: String, displayName: String): Uri {
        @Suppress("DEPRECATION")
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val directory = File(pictures, "$ALBUM_ROOT/$album").apply { mkdirs() }
        val destination = File(directory, displayName)
        source.inputStream().use { input -> destination.outputStream().use { input.copyTo(it) } }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(destination.absolutePath),
            arrayOf(JPEG_MIME_TYPE),
            null
        )
        return Uri.fromFile(destination)
    }

    private fun sanitize(albumName: String?): String =
        albumName?.trim()?.replace(UNSAFE_CHARACTERS, "")?.takeIf { it.isNotBlank() } ?: DEFAULT_ALBUM

    private companion object {
        const val ALBUM_ROOT = "PoseMatch"
        const val DEFAULT_ALBUM = "Unknown Location"
        const val JPEG_MIME_TYPE = "image/jpeg"
        val UNSAFE_CHARACTERS = Regex("[^A-Za-z0-9 _-]")
    }
}
