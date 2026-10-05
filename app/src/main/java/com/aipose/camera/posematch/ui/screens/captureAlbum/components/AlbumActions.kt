package com.aipose.camera.posematch.ui.screens.captureAlbum.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.common.poseGradientPill
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseRoseLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ButtonHeight = 56.dp
private val ButtonCorner = 28.dp
private val CameraGlyphSize = 20.dp

@Composable
internal fun AlbumActions(
    onShootAgain: () -> Unit,
    onRemoveAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonHeight)
                .poseGradientPill(
                    palette = GlossyBadgePalette.HeroCta,
                    cornerRadius = ButtonCorner,
                    glossHeight = 25.dp,
                )
                .bounceClick(onClick = onShootAgain),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_shoot_again),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(CameraGlyphSize),
            )
            Text(
                text = stringResource(R.string.album_shoot_again),
                style = poseTextStyle(16.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
            )
        }
        Text(
            text = stringResource(R.string.album_remove_all),
            style = poseTextStyle(11.5.sp, FontWeight.Bold, PoseRoseLight),
            modifier = Modifier
                .padding(top = 18.dp)
                .click(onClick = onRemoveAll)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
