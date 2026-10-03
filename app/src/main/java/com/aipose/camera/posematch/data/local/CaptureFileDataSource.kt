package com.aipose.camera.posematch.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaptureFileDataSource(private val context: Context) {

    suspend fun createCaptureTarget(): String = withContext(Dispatchers.IO) {
        val stamp = SimpleDateFormat(FILE_STAMP_PATTERN, Locale.US).format(Date())
        File(context.getExternalFilesDir(null), FILE_PREFIX + stamp + FILE_EXTENSION).absolutePath
    }

    suspend fun delete(path: String) {
        withContext(Dispatchers.IO) { runCatching { File(path).delete() } }
    }

    private companion object {
        const val FILE_STAMP_PATTERN = "yyyyMMdd_HHmmss"
        const val FILE_PREFIX = "SNAP_"
        const val FILE_EXTENSION = ".jpg"
    }
}
