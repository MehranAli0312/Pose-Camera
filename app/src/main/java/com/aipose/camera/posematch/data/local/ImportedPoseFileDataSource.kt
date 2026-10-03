package com.aipose.camera.posematch.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImportedPoseFileDataSource(private val context: Context) {

    suspend fun copyToAppStorage(sourceUri: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(sourceUri)
            val target = File(context.getExternalFilesDir(null), fileName())
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            target.absolutePath
        }.getOrNull()
    }

    private fun fileName(): String {
        val stamp = SimpleDateFormat(FILE_STAMP_PATTERN, Locale.US).format(Date())
        return FILE_PREFIX + stamp + FILE_EXTENSION
    }

    private companion object {
        const val FILE_STAMP_PATTERN = "yyyyMMdd_HHmmss"
        const val FILE_PREFIX = "imported_pose_"
        const val FILE_EXTENSION = ".png"
    }
}
