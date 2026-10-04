package com.aipose.camera.posematch.ui.common

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

private const val IMAGE_MIME_TYPE = "image/*"
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

fun Context.shareImageFile(path: String, chooserTitle: String) {
    runCatching {
        val uri = FileProvider.getUriForFile(this, packageName + FILE_PROVIDER_SUFFIX, File(path))
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = IMAGE_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }
}
