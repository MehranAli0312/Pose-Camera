package com.aipose.camera.posematch.ui.screens.photoEdit.models

import com.aipose.camera.posematch.domain.models.PhotoCropRect

enum class CropHandle(
    val movesLeft: Boolean = false,
    val movesTop: Boolean = false,
    val movesRight: Boolean = false,
    val movesBottom: Boolean = false
) {
    None,
    Move,
    TopLeft(movesLeft = true, movesTop = true),
    TopRight(movesTop = true, movesRight = true),
    BottomLeft(movesLeft = true, movesBottom = true),
    BottomRight(movesRight = true, movesBottom = true),
    Left(movesLeft = true),
    Top(movesTop = true),
    Right(movesRight = true),
    Bottom(movesBottom = true)
}

fun PhotoCropRect.dragged(
    handle: CropHandle,
    dx: Float,
    dy: Float,
    lockedAspect: Float?,
    minWidth: Float,
    minHeight: Float
): PhotoCropRect = when (handle) {
    CropHandle.None -> this
    CropHandle.Move -> moved(dx, dy)
    else -> resized(handle, dx, dy, lockedAspect, minWidth, minHeight)
}

private fun PhotoCropRect.moved(dx: Float, dy: Float): PhotoCropRect {
    val clampedDx = dx.coerceIn(-left, 1f - right)
    val clampedDy = dy.coerceIn(-top, 1f - bottom)
    return PhotoCropRect(left + clampedDx, top + clampedDy, right + clampedDx, bottom + clampedDy)
}

private fun PhotoCropRect.resized(
    handle: CropHandle,
    dx: Float,
    dy: Float,
    lockedAspect: Float?,
    minWidth: Float,
    minHeight: Float
): PhotoCropRect {
    val nextLeft = if (handle.movesLeft) (left + dx).coerceIn(0f, right - minWidth) else left
    val nextRight = if (handle.movesRight) (right + dx).coerceIn(left + minWidth, 1f) else right
    val nextTop = if (handle.movesTop) (top + dy).coerceIn(0f, bottom - minHeight) else top
    val nextBottom =
        if (handle.movesBottom) (bottom + dy).coerceIn(top + minHeight, 1f) else bottom

    if (lockedAspect == null || lockedAspect <= 0f) {
        return PhotoCropRect(nextLeft, nextTop, nextRight, nextBottom)
    }

    val width = (nextRight - nextLeft).coerceAtLeast(minWidth)
    val height = (width / lockedAspect).coerceAtLeast(minHeight)
    return if (handle.movesTop) {
        val anchoredTop = nextBottom - height
        if (anchoredTop >= 0f) {
            PhotoCropRect(nextLeft, anchoredTop, nextRight, nextBottom)
        } else {
            cappedByHeight(handle, nextBottom, nextLeft, nextRight, lockedAspect, top = 0f)
        }
    } else {
        val anchoredBottom = nextTop + height
        if (anchoredBottom <= 1f) {
            PhotoCropRect(nextLeft, nextTop, nextRight, anchoredBottom)
        } else {
            cappedByHeight(handle, 1f, nextLeft, nextRight, lockedAspect, top = nextTop)
        }
    }
}

private fun cappedByHeight(
    handle: CropHandle,
    bottom: Float,
    left: Float,
    right: Float,
    lockedAspect: Float,
    top: Float
): PhotoCropRect {
    val cappedWidth = ((bottom - top) * lockedAspect).coerceIn(0f, 1f)
    return if (handle.movesLeft) {
        PhotoCropRect((right - cappedWidth).coerceAtLeast(0f), top, right, bottom)
    } else {
        PhotoCropRect(left, top, (left + cappedWidth).coerceAtMost(1f), bottom)
    }
}
