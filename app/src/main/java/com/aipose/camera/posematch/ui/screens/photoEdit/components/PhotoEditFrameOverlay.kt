package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val BracketInset = 16.dp
private val BracketLength = 22.dp
private val BracketCorner = 8.dp
private val BracketStroke = 3.dp
private val GridStroke = 1.dp
private const val GRID_ALPHA = 0.16f
private const val BRACKET_ALPHA = 0.9f
private const val THIRD = 1f / 3f
private const val TWO_THIRDS = 2f / 3f

@Composable
internal fun PhotoEditFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawThirdsGrid(
            bounds = Rect(Offset.Zero, size),
            color = Color.White.copy(alpha = GRID_ALPHA),
            strokeWidth = GridStroke.toPx(),
        )
        drawRoundedBrackets()
    }
}

internal fun DrawScope.drawThirdsGrid(bounds: Rect, color: Color, strokeWidth: Float) {
    listOf(THIRD, TWO_THIRDS).forEach { fraction ->
        val x = bounds.left + bounds.width * fraction
        val y = bounds.top + bounds.height * fraction
        drawLine(color, Offset(x, bounds.top), Offset(x, bounds.bottom), strokeWidth)
        drawLine(color, Offset(bounds.left, y), Offset(bounds.right, y), strokeWidth)
    }
}

private fun DrawScope.drawRoundedBrackets() {
    val inset = BracketInset.toPx()
    val length = BracketLength.toPx()
    val corner = BracketCorner.toPx()
    val left = inset
    val top = inset
    val right = size.width - inset
    val bottom = size.height - inset
    val path = Path().apply {
        moveTo(left, top + length)
        lineTo(left, top + corner)
        quadraticTo(left, top, left + corner, top)
        lineTo(left + length, top)

        moveTo(right - length, top)
        lineTo(right - corner, top)
        quadraticTo(right, top, right, top + corner)
        lineTo(right, top + length)

        moveTo(right, bottom - length)
        lineTo(right, bottom - corner)
        quadraticTo(right, bottom, right - corner, bottom)
        lineTo(right - length, bottom)

        moveTo(left + length, bottom)
        lineTo(left + corner, bottom)
        quadraticTo(left, bottom, left, bottom - corner)
        lineTo(left, bottom - length)
    }
    drawPath(
        path = path,
        color = Color.White.copy(alpha = BRACKET_ALPHA),
        style = Stroke(
            width = BracketStroke.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}
