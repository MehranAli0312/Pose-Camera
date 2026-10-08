package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.screens.collections.models.AlbumAccent
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureAlbum
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi

internal const val ALBUM_PREVIEW_SIZE = 3

@Composable
internal fun CaptureAlbumSection(
    album: CaptureAlbum,
    accent: AlbumAccent,
    onShowAll: () -> Unit,
    onCaptureClick: (CaptureUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CaptureAlbumHeader(
            locationLabel = album.locationLabel,
            count = album.count,
            accent = accent,
            onShowAll = onShowAll,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val preview = album.captures.take(ALBUM_PREVIEW_SIZE)
            preview.forEach { captureUi ->
                CaptureShotCard(
                    captureUi = captureUi,
                    onClick = { onCaptureClick(captureUi) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(ALBUM_PREVIEW_SIZE - preview.size) { Spacer(modifier = Modifier.weight(1f)) }
        }
    }
}
