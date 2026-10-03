package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ListCardPosition { Single, First, Middle, Last }

private val DefaultCardRadius = 22.dp

fun listCardPosition(index: Int, count: Int): ListCardPosition = when {
    count == 1 -> ListCardPosition.Single
    index == 0 -> ListCardPosition.First
    index == count - 1 -> ListCardPosition.Last
    else -> ListCardPosition.Middle
}

val ListCardPosition.hasTopEdge: Boolean
    get() = this == ListCardPosition.First || this == ListCardPosition.Single

val ListCardPosition.hasBottomEdge: Boolean
    get() = this == ListCardPosition.Last || this == ListCardPosition.Single

fun ListCardPosition.cardShape(radius: Dp = DefaultCardRadius): Shape = when (this) {
    ListCardPosition.Single -> RoundedCornerShape(radius)
    ListCardPosition.First -> RoundedCornerShape(topStart = radius, topEnd = radius)
    ListCardPosition.Middle -> RoundedCornerShape(0.dp)
    ListCardPosition.Last -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
}
