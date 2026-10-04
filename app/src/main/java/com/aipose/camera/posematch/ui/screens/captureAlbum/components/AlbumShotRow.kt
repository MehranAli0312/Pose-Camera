package com.aipose.camera.posematch.ui.screens.captureAlbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseEmptySlot

internal const val ALBUM_COLUMNS = 3

private val TileShape = RoundedCornerShape(20.dp)
private const val TILE_ASPECT_RATIO = 110f / 146f

@Composable
internal fun AlbumShotRow(
    shots: List<Capture>,
    onShotClick: (Capture) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        shots.forEach { shot ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(TILE_ASPECT_RATIO)
                    .clip(TileShape)
                    .bounceClick(onClick = { onShotClick(shot) }),
            ) {
                PoseImage(
                    imagePath = shot.imagePath,
                    contentDescription = shot.title,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        repeat(ALBUM_COLUMNS - shots.size) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(TILE_ASPECT_RATIO)
                    .clip(TileShape)
                    .background(PoseEmptySlot),
            )
        }
    }
}
