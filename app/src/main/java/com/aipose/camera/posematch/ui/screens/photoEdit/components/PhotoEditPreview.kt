package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoGeometry
import com.aipose.camera.posematch.ui.common.imageModelOf
import com.aipose.camera.posematch.ui.theme.PosePhotoScrim
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private val CardShape = RoundedCornerShape(24.dp)
private const val STRAIGHT_ANGLE = 180
private const val FLIPPED_SCALE = -1f

@Composable
internal fun PhotoEditPreview(
    imagePath: String,
    colorFilter: ColorFilter?,
    geometry: PhotoGeometry,
    frameAspect: Float?,
    modifier: Modifier = Modifier,
    imageInset: Dp = 0.dp,
    onCompareChange: ((Boolean) -> Unit)? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val containerAspect = if (maxHeight.value > 0f) maxWidth / maxHeight else 1f
        val cardModifier = if (frameAspect != null && frameAspect > 0f) {
            Modifier.aspectRatio(frameAspect, matchHeightConstraintsFirst = containerAspect > frameAspect)
        } else {
            Modifier.fillMaxSize()
        }
        Box(
            modifier = cardModifier
                .clip(CardShape)
                .background(PosePhotoScrim)
                .compareGesture(onCompareChange),
        ) {
            GeometryImage(
                imagePath = imagePath,
                colorFilter = colorFilter,
                geometry = geometry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(imageInset),
            )
            overlay()
        }
    }
}

@Composable
private fun GeometryImage(
    imagePath: String,
    colorFilter: ColorFilter?,
    geometry: PhotoGeometry,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val isQuarterTurned = geometry.rotationDegrees % STRAIGHT_ANGLE != 0
        val imageWidth = if (isQuarterTurned) maxHeight else maxWidth
        val imageHeight = if (isQuarterTurned) maxWidth else maxHeight
        val straightenScale = straightenCoverScale(
            degrees = geometry.straightenDegrees,
            aspect = if (imageHeight.value > 0f) imageWidth / imageHeight else 1f,
        )
        AsyncImage(
            model = imageModelOf(imagePath),
            contentDescription = stringResource(R.string.captured_frame),
            contentScale = ContentScale.Crop,
            colorFilter = colorFilter,
            modifier = Modifier
                .requiredSize(width = imageWidth, height = imageHeight)
                .graphicsLayer {
                    rotationZ = geometry.rotationDegrees + geometry.straightenDegrees
                    scaleX = straightenScale * if (geometry.isFlippedHorizontally) FLIPPED_SCALE else 1f
                    scaleY = straightenScale * if (geometry.isFlippedVertically) FLIPPED_SCALE else 1f
                },
        )
    }
}

private fun straightenCoverScale(degrees: Float, aspect: Float): Float {
    if (degrees == 0f) return 1f
    val radians = Math.toRadians(abs(degrees).toDouble())
    val longSideRatio = max(aspect, 1f / aspect)
    return (cos(radians) + sin(radians) * longSideRatio).toFloat()
}

private fun Modifier.compareGesture(onCompareChange: ((Boolean) -> Unit)?): Modifier {
    if (onCompareChange == null) return this
    return pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            onCompareChange(true)
            waitForUpOrCancellation()
            onCompareChange(false)
        }
    }
}
