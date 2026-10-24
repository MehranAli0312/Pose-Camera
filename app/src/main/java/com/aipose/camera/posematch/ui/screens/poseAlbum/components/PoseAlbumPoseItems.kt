package com.aipose.camera.posematch.ui.screens.poseAlbum.components

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.LaunchedEffect
import com.aipose.camera.posematch.ads.PoseAlbumInlineFirst
import com.aipose.camera.posematch.ads.PoseAlbumInlineSecond
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.InlineNativeAd
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseAlbumAdLayout
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseAlbumUiState
import com.example.ads.AdPlacement

private const val FIRST_AD_KEY = "pose_album_ad_first"
private const val SECOND_AD_KEY = "pose_album_ad_second"

internal fun LazyGridScope.poseAlbumPoseItems(
    content: PoseAlbumUiState.Content,
    adLayout: PoseAlbumAdLayout,
    onPoseClick: (pose: Pose, isLocked: Boolean) -> Unit,
    onToggleSaved: (Pose) -> Unit,
    prepareSecondAd: () -> Unit,
) {
    val poses = content.poses
    val firstAdIndex = adLayout.firstAdIndex
    val secondAdIndex = adLayout.secondAdIndex

    fun poseCards(from: Int, to: Int) {
        items(poses.subList(from, to), key = { pose -> pose.id }) { pose ->
            val isLocked = pose.id in content.lockedPoseIds
            ExplorePoseCard(
                pose = pose,
                isSaved = pose.id in content.savedPoseIds,
                isLocked = isLocked,
                onClick = { onPoseClick(pose, isLocked) },
                onToggleSaved = { onToggleSaved(pose) },
            )
        }
    }

    if (firstAdIndex == null) {
        poseCards(0, poses.size)
        return
    }

    poseCards(0, firstAdIndex)
    item(key = FIRST_AD_KEY, span = { GridItemSpan(maxLineSpan) }) {
        if (secondAdIndex != null) LaunchedEffect(Unit) { prepareSecondAd() }
        InlineNativeAd(placement = AdPlacement.PoseAlbumInlineFirst)
    }

    if (secondAdIndex == null) {
        poseCards(firstAdIndex, poses.size)
        return
    }

    poseCards(firstAdIndex, secondAdIndex)
    item(key = SECOND_AD_KEY, span = { GridItemSpan(maxLineSpan) }) {
        InlineNativeAd(placement = AdPlacement.PoseAlbumInlineSecond)
    }
    poseCards(secondAdIndex, poses.size)
}
