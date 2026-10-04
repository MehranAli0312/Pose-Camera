package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import kotlin.math.max
import kotlin.math.min

private val TrackHeight = 5.dp
private val ThumbSize = 15.dp
private val ThumbElevation = 4.dp
private val SliderHeight = 20.dp
private const val TRACK_ALPHA = 0.14f
private const val CENTER_FRACTION = 0.5f

@Composable
fun PoseGradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    isCentered: Boolean = false,
) {
    val span = valueRange.endInclusive - valueRange.start
    val fraction = if (span <= 0f) 0f else ((value - valueRange.start) / span).coerceIn(0f, 1f)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier.height(SliderHeight),
        colors = SliderDefaults.colors(thumbColor = Color.White),
        track = { PoseSliderTrack(fraction = fraction, isCentered = isCentered) },
        thumb = {
            Box(
                modifier = Modifier
                    .size(ThumbSize)
                    .shadow(ThumbElevation, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        },
    )
}

@Composable
private fun PoseSliderTrack(
    fraction: Float,
    isCentered: Boolean,
    modifier: Modifier = Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val activeBrush = Brush.horizontalGradient(listOf(PoseCyanBright, PoseVioletLight))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TrackHeight)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = TRACK_ALPHA))
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(TrackHeight)) {
            val start = if (isCentered) min(CENTER_FRACTION, fraction) else 0f
            val end = if (isCentered) max(CENTER_FRACTION, fraction) else fraction
            if (end - start <= 0f) return@Canvas
            val left = if (isRtl) size.width * (1f - end) else size.width * start
            drawRoundRect(
                brush = activeBrush,
                topLeft = Offset(left, 0f),
                size = Size(size.width * (end - start), size.height),
                cornerRadius = CornerRadius(size.height / 2f),
            )
        }
    }
}
