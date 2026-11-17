package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.aipose.camera.posematch.ads.HomeInlineNative
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.InlineNativeAd
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExplorePoseCard
import com.example.ads.AdPlacement

private const val INLINE_AD_AFTER_ROWS = 2
private const val INLINE_AD_KEY = "home_native_ad"

internal fun LazyGridScope.homePoseGridItems(
    poses: List<Pose>,
    savedPoseIds: Set<Int>,
    lockedPoseIds: Set<Int>,
    columns: Int,
    showNativeAd: Boolean,
    itemPadding: Dp,
    onPoseClick: (pose: Pose, isLocked: Boolean) -> Unit,
    onToggleSaved: (Pose) -> Unit,
) {
    val adIndex = if (showNativeAd) {
        (columns * INLINE_AD_AFTER_ROWS).coerceAtMost(poses.size)
    } else {
        poses.size
    }

    fun poseCards(from: Int, to: Int) {
        items(poses.subList(from, to), key = { pose -> pose.id }) { pose ->
            val isLocked = pose.id in lockedPoseIds
            ExplorePoseCard(
                pose = pose,
                isSaved = pose.id in savedPoseIds,
                isLocked = isLocked,
                onClick = { onPoseClick(pose, isLocked) },
                onToggleSaved = { onToggleSaved(pose) },
                modifier = Modifier.padding(itemPadding),
            )
        }
    }

    poseCards(0, adIndex)
    if (showNativeAd) {
        item(key = INLINE_AD_KEY, span = { GridItemSpan(maxLineSpan) }) {
            InlineNativeAd(placement = AdPlacement.HomeInlineNative)
        }
    }
    poseCards(adIndex, poses.size)
}
