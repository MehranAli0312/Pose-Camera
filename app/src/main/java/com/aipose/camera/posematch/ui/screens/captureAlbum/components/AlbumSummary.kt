package com.aipose.camera.posematch.ui.screens.captureAlbum.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

@Composable
internal fun AlbumSummary(
    summary: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = summary,
        style = poseTextStyle(12.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
