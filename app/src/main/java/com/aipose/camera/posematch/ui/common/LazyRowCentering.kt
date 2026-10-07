package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState

suspend fun LazyListState.centerItem(index: Int, animated: Boolean = true) {
    if (index < 0) return
    if (layoutInfo.visibleItemsInfo.none { it.index == index }) {
        if (animated) animateScrollToItem(index) else scrollToItem(index)
    }
    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    if (viewportSize <= 0) return
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    val distance = item.offset + item.size / 2f - viewportSize / 2f
    if (animated) animateScrollBy(distance) else scrollBy(distance)
}
