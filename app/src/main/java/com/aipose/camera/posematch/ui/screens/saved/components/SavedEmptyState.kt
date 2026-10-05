package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BadgeSize = 56.dp
private val GlyphSize = 26.dp

@Composable
internal fun SavedEmptyState(
    tab: SavedTab,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GlossyIconBadge(
                iconRes = R.drawable.ic_pose_nav_saved,
                palette = GlossyBadgePalette.Pink,
                size = BadgeSize,
                glyphSize = GlyphSize,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(
                    if (tab == SavedTab.Shots) R.string.saved_empty_shots else R.string.saved_empty_poses
                ),
                style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (tab == SavedTab.Shots) {
                        R.string.saved_empty_shots_sub
                    } else {
                        R.string.saved_empty_poses_sub
                    }
                ),
                style = poseTextStyle(11.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
                textAlign = TextAlign.Center,
            )
        }
    }
}
