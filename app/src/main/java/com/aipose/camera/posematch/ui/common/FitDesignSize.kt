package com.aipose.camera.posematch.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.min
import kotlin.math.roundToInt

fun Modifier.fitDesignSize(designWidth: Dp, designHeight: Dp): Modifier = layout { measurable, constraints ->
    val widthPx = designWidth.roundToPx()
    val heightPx = designHeight.roundToPx()
    val placeable = measurable.measure(Constraints.fixed(widthPx, heightPx))
    val widthScale = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() / widthPx else 1f
    val heightScale = if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() / heightPx else 1f
    val scale = min(1f, min(widthScale, heightScale))
    layout((widthPx * scale).roundToInt(), (heightPx * scale).roundToInt()) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}
