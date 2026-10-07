package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aipose.camera.posematch.ui.theme.AppTheme
import java.io.File

private val URI_SCHEMES = listOf("file://", "content://", "android.resource://", "http://", "https://")
private const val IMAGE_CROSSFADE_MILLIS = 150

fun imageModelOf(imagePath: String): Any =
    if (URI_SCHEMES.any { scheme -> imagePath.startsWith(scheme) }) imagePath else File(imagePath)

@Composable
fun PoseImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = 1f,
    onImageSizeKnown: ((Size) -> Unit)? = null,
) {
    Box(modifier = modifier.background(AppTheme.extendedColors.mediaPlaceholder)) {
        if (imagePath.isNotBlank()) {
            val context = LocalContext.current
            val request = remember(context, imagePath) {
                ImageRequest.Builder(context)
                    .data(imageModelOf(imagePath))
                    .placeholderMemoryCacheKey(imagePath)
                    .crossfade(IMAGE_CROSSFADE_MILLIS)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                alpha = alpha,
                onSuccess = onImageSizeKnown?.let { report ->
                    { state -> report(state.painter.intrinsicSize) }
                },
            )
        }
    }
}
