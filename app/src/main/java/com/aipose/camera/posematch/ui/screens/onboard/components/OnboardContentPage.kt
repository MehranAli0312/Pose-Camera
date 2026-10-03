package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardSlide
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private const val IMAGE_HEIGHT_FRACTION = 0.85f
private const val IMAGE_ASPECT_RATIO = 2f / 3f
private val ImageShape = RoundedCornerShape(24.dp)

@Composable
internal fun OnboardContentPage(
    slide: OnboardSlide,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(slide.imageRes),
            contentDescription = stringResource(slide.titleRes),
            modifier = Modifier
                .fillMaxHeight(IMAGE_HEIGHT_FRACTION)
                .aspectRatio(IMAGE_ASPECT_RATIO)
                .clip(ImageShape)
                .border(1.dp, LocalAppPalette.current.glass, ImageShape),
            contentScale = ContentScale.Crop,
        )
    }
}
