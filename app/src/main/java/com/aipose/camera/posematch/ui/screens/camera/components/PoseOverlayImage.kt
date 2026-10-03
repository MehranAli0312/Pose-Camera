package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.imageModelOf
import com.aipose.camera.posematch.ui.screens.camera.models.OverlayTransform

@Composable
internal fun PoseOverlayImage(
    imagePath: String,
    transform: OverlayTransform,
    surfaceSize: IntSize,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = transform.scale
                scaleY = transform.scale
                translationX = transform.offsetX * surfaceSize.width
                translationY = transform.offsetY * surfaceSize.height
                rotationZ = transform.rotation
            }
    ) {
        AsyncImage(
            model = imageModelOf(imagePath),
            contentDescription = stringResource(R.string.pose_overlay),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alpha = transform.opacity,
        )
    }
}
