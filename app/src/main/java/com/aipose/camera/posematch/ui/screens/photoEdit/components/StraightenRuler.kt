package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.LtrLayout
import com.aipose.camera.posematch.ui.common.PoseSliderThumb
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import kotlin.math.roundToInt

private val RulerHeight = 24.dp
private val ThumbSize = 20.dp
private val TickStroke = 1.6.dp
private val TickHeight = 4.dp
private val EdgeTickHeight = 6.dp
private val ZeroMarkStroke = 2.4.dp
private val ZeroMarkHeight = 12.dp
private const val TICK_COUNT = 11
private const val TICK_ALPHA = 0.18f

@Composable
internal fun StraightenRuler(
    degrees: Float,
    range: ClosedFloatingPointRange<Float>,
    onDegreesChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnChange by rememberUpdatedState(onDegreesChange)
    LtrLayout {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .height(RulerHeight),
        ) {
            val span = range.endInclusive - range.start
            val fraction = if (span <= 0f) 0f else ((degrees - range.start) / span).coerceIn(0f, 1f)
            val thumbHalfPx = with(LocalDensity.current) { (ThumbSize / 2).toPx() }
            fun degreesAt(x: Float, width: Float): Float {
                val track = (width - thumbHalfPx * 2f).coerceAtLeast(1f)
                val position = ((x - thumbHalfPx) / track).coerceIn(0f, 1f)
                return (range.start + position * span).roundToInt().toFloat()
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(range) {
                        detectTapGestures { offset -> currentOnChange(degreesAt(offset.x, size.width.toFloat())) }
                    }
                    .pointerInput(range) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            currentOnChange(degreesAt(change.position.x, size.width.toFloat()))
                        }
                    },
            ) {
                val start = thumbHalfPx
                val end = size.width - thumbHalfPx
                val centerY = size.height / 2f
                val tickColor = Color.White.copy(alpha = TICK_ALPHA)
                repeat(TICK_COUNT) { index ->
                    val isEdge = index == 0 || index == TICK_COUNT - 1
                    val isCenter = index == TICK_COUNT / 2
                    if (isCenter) return@repeat
                    val x = start + (end - start) * index / (TICK_COUNT - 1)
                    val half = (if (isEdge) EdgeTickHeight else TickHeight).toPx() / 2f
                    drawLine(
                        color = tickColor,
                        start = Offset(x, centerY - half),
                        end = Offset(x, centerY + half),
                        strokeWidth = TickStroke.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                val zeroX = start + (end - start) * ((0f - range.start) / span).coerceIn(0f, 1f)
                val zeroHalf = ZeroMarkHeight.toPx() / 2f
                drawLine(
                    color = PoseVioletLight,
                    start = Offset(zeroX, centerY - zeroHalf),
                    end = Offset(zeroX, centerY + zeroHalf),
                    strokeWidth = ZeroMarkStroke.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            PoseSliderThumb(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (maxWidth - ThumbSize) * fraction),
            )
        }
    }
}
