package com.aipose.camera.posematch.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal val assetBitmapCache = LruCache<String, androidx.compose.ui.graphics.ImageBitmap>(80)
/** "file:///android_asset/poses/x.jpg" -> "poses/x.jpg"; null when not a bundled-asset uri. */
internal fun assetPathOf(image: String): String? =
    if (image.startsWith("file:///android_asset/")) image.removePrefix("file:///android_asset/") else null
internal fun decodeAsset(context: Context, assetPath: String, sample: Int = 2): Bitmap? {
    // Tolerate extension mismatches (e.g. JSON says .jpg but Android Studio converted the
    // bundled file to .webp): try the given path first, then the same basename with other
    // common image extensions. BitmapFactory decodes jpg/png/webp regardless of extension.
    val candidates = buildList {
        add(assetPath)
        val dot = assetPath.lastIndexOf('.')
        if (dot > 0) {
            val base = assetPath.substring(0, dot)
            listOf("webp", "jpg", "jpeg", "png").forEach { add("$base.$it") }
        }
    }.distinct()
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    for (path in candidates) {
        val bmp = runCatching {
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, opts) }
        }.getOrNull()
        if (bmp != null) return bmp
    }
    return null
}
@Composable
internal fun AssetImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = 1f
) {
    val context = LocalContext.current
    val img by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
        initialValue = assetBitmapCache.get(assetPath),
        key1 = assetPath
    ) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                decodeAsset(context, assetPath)?.asImageBitmap()
                    ?.also { assetBitmapCache.put(assetPath, it) }
            }
        }
    }
    val bmp = img
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alpha = alpha
        )
    } else {
        Box(modifier = modifier.background(Color(0xFF1A1A1F)))
    }
}
