package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.imageModelOf

@Composable
internal fun PhotoEditPreview(
    imagePath: String,
    colorFilter: ColorFilter?,
    rotationDegrees: Int,
    cropAspect: Float?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageModelOf(imagePath),
            contentDescription = stringResource(R.string.captured_frame),
            modifier = Modifier
                .then(
                    if (cropAspect != null) {
                        Modifier.aspectRatio(cropAspect)
                    } else {
                        Modifier.fillMaxSize()
                    }
                )
                .clip(RoundedCornerShape(10.dp))
                .graphicsLayer { rotationZ = rotationDegrees.toFloat() },
            contentScale = if (cropAspect != null) ContentScale.Crop else ContentScale.Fit,
            colorFilter = colorFilter,
        )
    }
}
