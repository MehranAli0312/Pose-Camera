package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

private const val GRID_STROKE_WIDTH = 1.5f
private const val GRID_LINE_ALPHA = 0.35f
private const val FIRST_THIRD = 3f
private const val SECOND_THIRD = 2f / 3f

@Composable
internal fun CameraGridOverlay(modifier: Modifier = Modifier) {
    val lineColor = Color.White.copy(alpha = GRID_LINE_ALPHA)
    Canvas(modifier = modifier) {
        drawLine(
            color = lineColor,
            start = Offset(size.width / FIRST_THIRD, 0f),
            end = Offset(size.width / FIRST_THIRD, size.height),
            strokeWidth = GRID_STROKE_WIDTH,
        )
        drawLine(
            color = lineColor,
            start = Offset(size.width * SECOND_THIRD, 0f),
            end = Offset(size.width * SECOND_THIRD, size.height),
            strokeWidth = GRID_STROKE_WIDTH,
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height / FIRST_THIRD),
            end = Offset(size.width, size.height / FIRST_THIRD),
            strokeWidth = GRID_STROKE_WIDTH,
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height * SECOND_THIRD),
            end = Offset(size.width, size.height * SECOND_THIRD),
            strokeWidth = GRID_STROKE_WIDTH,
        )
    }
}
