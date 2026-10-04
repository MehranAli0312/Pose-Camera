package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val GridStroke = 1.dp
private const val GRID_LINE_ALPHA = 0.12f
private const val FIRST_THIRD = 1f / 3f
private const val SECOND_THIRD = 2f / 3f

@Composable
internal fun CameraGridOverlay(modifier: Modifier = Modifier) {
    val lineColor = Color.White.copy(alpha = GRID_LINE_ALPHA)
    Canvas(modifier = modifier) {
        val stroke = GridStroke.toPx()
        listOf(FIRST_THIRD, SECOND_THIRD).forEach { fraction ->
            drawLine(
                color = lineColor,
                start = Offset(size.width * fraction, 0f),
                end = Offset(size.width * fraction, size.height),
                strokeWidth = stroke,
            )
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height * fraction),
                end = Offset(size.width, size.height * fraction),
                strokeWidth = stroke,
            )
        }
    }
}
