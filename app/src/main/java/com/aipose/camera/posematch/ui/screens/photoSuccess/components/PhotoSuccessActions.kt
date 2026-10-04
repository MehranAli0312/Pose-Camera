package com.aipose.camera.posematch.ui.screens.photoSuccess.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.common.poseGradientPill
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ButtonHeight = 56.dp
private val ButtonCorner = 28.dp
private val ButtonShape = RoundedCornerShape(ButtonCorner)
private val ButtonGlyphSize = 20.dp
private val ChevronWidth = 8.dp
private val ChevronHeight = 10.dp
private const val SHARE_BORDER_ALPHA = 0.14f

@Composable
internal fun PhotoSuccessActions(
    onShare: () -> Unit,
    onShootAgain: () -> Unit,
    onViewCollections: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ActionButton(
                iconRes = R.drawable.ic_saved_share,
                iconTint = PoseTextBright,
                label = stringResource(R.string.action_share),
                onClick = onShare,
                modifier = Modifier
                    .weight(1f)
                    .poseRaisedSurface(ButtonShape, borderAlpha = SHARE_BORDER_ALPHA),
            )
            ActionButton(
                iconRes = R.drawable.ic_saved_camera,
                iconTint = Color.White,
                label = stringResource(R.string.saved_shoot_again),
                onClick = onShootAgain,
                modifier = Modifier
                    .weight(1f)
                    .poseGradientPill(
                        palette = GlossyBadgePalette.HeroCta,
                        cornerRadius = ButtonCorner,
                        glossHeight = 25.dp,
                    ),
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .click(onClick = onViewCollections)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.saved_view_collections),
                style = poseTextStyle(12.sp, FontWeight.Bold, PoseVioletLight),
                maxLines = 1,
            )
            Image(
                painter = painterResource(R.drawable.ic_pose_chevron_end),
                contentDescription = null,
                colorFilter = ColorFilter.tint(PoseVioletLight),
                modifier = Modifier.size(width = ChevronWidth, height = ChevronHeight),
            )
        }
    }
}

@Composable
private fun ActionButton(
    @DrawableRes iconRes: Int,
    iconTint: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(ButtonHeight)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconTint),
            modifier = Modifier.size(ButtonGlyphSize),
        )
        Text(
            text = label,
            style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
