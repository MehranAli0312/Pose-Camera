package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.aipose.camera.posematch.ui.screens.collections.models.FavoritePose
import com.aipose.camera.posematch.ui.screens.poseAlbum.components.ExplorePoseCard

private const val FAVORITE_ROW_KEY = "collections_favorite_row_"

internal fun LazyListScope.favoritePoseGridItems(
    favorites: List<FavoritePose>,
    columns: Int,
    sidePadding: Dp,
    cardGap: Dp,
    onPoseClick: (FavoritePose) -> Unit,
    onRemove: (FavoritePose) -> Unit,
) {
    items(
        favorites.chunked(columns),
        key = { row -> FAVORITE_ROW_KEY + row.first().pose.id },
    ) { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sidePadding),
            horizontalArrangement = Arrangement.spacedBy(cardGap),
        ) {
            row.forEach { favorite ->
                ExplorePoseCard(
                    pose = favorite.pose,
                    isSaved = true,
                    isLocked = favorite.isLocked,
                    onClick = { onPoseClick(favorite) },
                    onToggleSaved = { onRemove(favorite) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(columns - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
