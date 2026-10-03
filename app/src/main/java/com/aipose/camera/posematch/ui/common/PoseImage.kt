package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.aipose.camera.posematch.ui.theme.AppTheme
import java.io.File

private val URI_SCHEMES = listOf("file://", "content://", "android.resource://", "http://", "https://")

fun imageModelOf(imagePath: String): Any =
    if (URI_SCHEMES.any { scheme -> imagePath.startsWith(scheme) }) imagePath else File(imagePath)

@Composable
fun PoseImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = 1f,
) {
    Box(modifier = modifier.background(AppTheme.extendedColors.mediaPlaceholder)) {
        if (imagePath.isNotBlank()) {
            AsyncImage(
                model = imageModelOf(imagePath),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                alpha = alpha,
            )
        }
    }
}
