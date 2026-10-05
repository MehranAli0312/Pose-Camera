package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.domain.models.PhotoCropRect
import com.aipose.camera.posematch.ui.screens.photoEdit.models.CropHandle
import com.aipose.camera.posematch.ui.screens.photoEdit.models.dragged
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import kotlin.math.abs

private val FrameStroke = 1.5.dp
private val GridStroke = 1.dp
private val HandleLength = 26.dp
private val HandleThickness = 4.dp
private val HandleTouchRadius = 32.dp
private val MinFrameSize = 72.dp
private val LabelShape = RoundedCornerShape(13.dp)
private val LabelBottomGap = 14.dp
private const val DIM_ALPHA = 0.58f
private const val FRAME_ALPHA = 0.85f
private const val GRID_ALPHA = 0.3f
private const val LABEL_BORDER_ALPHA = 0.22f

@Composable
internal fun CropFrameOverlay(
    cropRect: PhotoCropRect,
    lockedAspect: Float?,
    outputLabel: String,
    onCropRectChange: (PhotoCropRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val touchRadiusPx = with(density) { HandleTouchRadius.toPx() }
        val minWidth = with(density) { MinFrameSize.toPx() } / widthPx
        val minHeight = with(density) { MinFrameSize.toPx() } / heightPx
        val frame = cropRect.toFrame(widthPx, heightPx)
        val currentRect by rememberUpdatedState(cropRect)
        val currentOnChange by rememberUpdatedState(onCropRectChange)
        val normalizedAspect = lockedAspect?.takeIf { it > 0f }?.let { it * heightPx / widthPx }
        var activeHandle by remember { mutableStateOf(CropHandle.None) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(widthPx, heightPx, normalizedAspect) {
                    detectDragGestures(
                        onDragStart = { position ->
                            activeHandle = handleAt(
                                position = position,
                                frame = currentRect.toFrame(widthPx, heightPx),
                                touchRadius = touchRadiusPx,
                                allowEdges = normalizedAspect == null,
                            )
                        },
                        onDragEnd = { activeHandle = CropHandle.None },
                        onDragCancel = { activeHandle = CropHandle.None },
                    ) { _, drag ->
                        currentOnChange(
                            currentRect.dragged(
                                handle = activeHandle,
                                dx = drag.x / widthPx,
                                dy = drag.y / heightPx,
                                lockedAspect = normalizedAspect,
                                minWidth = minWidth,
                                minHeight = minHeight,
                            )
                        )
                    }
                }
        ) {
            drawDimOutside(frame)
            drawRect(
                color = Color.White.copy(alpha = FRAME_ALPHA),
                topLeft = frame.topLeft,
                size = frame.size,
                style = Stroke(width = FrameStroke.toPx()),
            )
            drawThirdsGrid(
                bounds = frame,
                color = Color.White.copy(alpha = GRID_ALPHA),
                strokeWidth = GridStroke.toPx(),
            )
            drawCornerHandles(frame)
        }
        if (outputLabel.isNotEmpty()) {
            val labelBottom = with(density) { (heightPx - frame.bottom).toDp() }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -(labelBottom + LabelBottomGap))
                    .clip(LabelShape)
                    .background(Color.Black.copy(alpha = DIM_ALPHA))
                    .border(1.dp, Color.White.copy(alpha = LABEL_BORDER_ALPHA), LabelShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = outputLabel,
                    style = poseTextStyle(10.sp, FontWeight.Bold, Color.White),
                    maxLines = 1,
                )
            }
        }
    }
}

private fun PhotoCropRect.toFrame(widthPx: Float, heightPx: Float): Rect = Rect(
    left = left * widthPx,
    top = top * heightPx,
    right = right * widthPx,
    bottom = bottom * heightPx,
)

private fun handleAt(
    position: Offset,
    frame: Rect,
    touchRadius: Float,
    allowEdges: Boolean
): CropHandle {
    val nearLeft = abs(position.x - frame.left) <= touchRadius
    val nearRight = abs(position.x - frame.right) <= touchRadius
    val nearTop = abs(position.y - frame.top) <= touchRadius
    val nearBottom = abs(position.y - frame.bottom) <= touchRadius
    val withinVertical = position.y >= frame.top - touchRadius && position.y <= frame.bottom + touchRadius
    val withinHorizontal = position.x >= frame.left - touchRadius && position.x <= frame.right + touchRadius

    return when {
        nearLeft && nearTop -> CropHandle.TopLeft
        nearRight && nearTop -> CropHandle.TopRight
        nearLeft && nearBottom -> CropHandle.BottomLeft
        nearRight && nearBottom -> CropHandle.BottomRight
        allowEdges && nearLeft && withinVertical -> CropHandle.Left
        allowEdges && nearRight && withinVertical -> CropHandle.Right
        allowEdges && nearTop && withinHorizontal -> CropHandle.Top
        allowEdges && nearBottom && withinHorizontal -> CropHandle.Bottom
        else -> CropHandle.Move
    }
}

private fun DrawScope.drawDimOutside(frame: Rect) {
    val dim = Color.Black.copy(alpha = DIM_ALPHA)
    drawRect(dim, Offset.Zero, Size(size.width, frame.top))
    drawRect(dim, Offset(0f, frame.bottom), Size(size.width, size.height - frame.bottom))
    drawRect(dim, Offset(0f, frame.top), Size(frame.left, frame.height))
    drawRect(dim, Offset(frame.right, frame.top), Size(size.width - frame.right, frame.height))
}

private fun DrawScope.drawCornerHandles(frame: Rect) {
    val length = HandleLength.toPx().coerceAtMost(frame.width / 3f)
    val thickness = HandleThickness.toPx()
    val corners = listOf(
        frame.topLeft to Offset(1f, 1f),
        frame.topRight to Offset(-1f, 1f),
        frame.bottomLeft to Offset(1f, -1f),
        frame.bottomRight to Offset(-1f, -1f),
    )
    corners.forEach { (corner, direction) ->
        val horizontalLeft = if (direction.x > 0f) corner.x else corner.x - length
        val verticalTop = if (direction.y > 0f) corner.y else corner.y - length
        val horizontalTop = if (direction.y > 0f) corner.y else corner.y - thickness
        val verticalLeft = if (direction.x > 0f) corner.x else corner.x - thickness
        drawRect(Color.White, Offset(horizontalLeft, horizontalTop), Size(length, thickness))
        drawRect(Color.White, Offset(verticalLeft, verticalTop), Size(thickness, length))
    }
}
