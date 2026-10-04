package com.aipose.camera.posematch.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val ASSET_SCHEME = "file:///android_asset/"
private const val SHARE_DIRECTORY = "shared_poses"

class PoseShareDataSource(private val context: Context) {

    suspend fun shareableFile(imagePath: String): File? = withContext(Dispatchers.IO) {
        if (!imagePath.startsWith(ASSET_SCHEME)) {
            return@withContext File(imagePath).takeIf { it.exists() }
        }
        val assetName = imagePath.removePrefix(ASSET_SCHEME)
        val target = File(File(context.cacheDir, SHARE_DIRECTORY), assetName.substringAfterLast('/'))
        runCatching {
            target.parentFile?.mkdirs()
            context.assets.open(assetName).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target
        }.getOrNull()
    }
}
