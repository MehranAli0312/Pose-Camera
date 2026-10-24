package com.aipose.camera.posematch.ui.screens.poseAlbum.models

import androidx.compose.ui.unit.Dp
import kotlin.math.floor

private const val FIRST_AD_AFTER_ROWS = 2
private const val SECOND_AD_AFTER_ROWS = 10

internal data class PoseAlbumAdLayout(
    val firstAdIndex: Int?,
    val secondAdIndex: Int?,
) {
    companion object {
        val None = PoseAlbumAdLayout(firstAdIndex = null, secondAdIndex = null)

        fun of(columns: Int, poseCount: Int): PoseAlbumAdLayout {
            fun indexAfterRows(rows: Int): Int? =
                (columns * rows).takeIf { index -> index + columns <= poseCount }

            val first = indexAfterRows(FIRST_AD_AFTER_ROWS) ?: return None
            return PoseAlbumAdLayout(
                firstAdIndex = first,
                secondAdIndex = indexAfterRows(SECOND_AD_AFTER_ROWS),
            )
        }
    }
}

internal fun adaptiveColumnCount(availableWidth: Dp, minCellWidth: Dp, gap: Dp): Int =
    floor((availableWidth + gap) / (minCellWidth + gap)).toInt().coerceAtLeast(1)
