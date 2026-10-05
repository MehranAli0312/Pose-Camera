package com.aipose.camera.posematch.ui.screens.poseDetail.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseBackButton
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.PoseScreenTopSpacing
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PosePinkSoft

private val ActionSize = 40.dp
private const val ACTION_FILL_ALPHA = 0.42f
private const val ACTION_BORDER_ALPHA = 0.2f

@Composable
internal fun PoseDetailTopBar(
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .safeTopSystemBarsPadding()
            .fillMaxWidth()
            .padding(
                start = PoseScreenGutter,
                end = PoseScreenGutter,
                top = PoseScreenTopSpacing,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PoseBackButton(onClick = onBack)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            HeroAction(
                iconRes = R.drawable.ic_pose_heart,
                contentDescription = stringResource(
                    if (isSaved) R.string.saved_remove else R.string.saved_add
                ),
                onClick = onToggleSaved,
                iconWidth = 15.dp,
                iconHeight = 15.dp,
                tint = if (isSaved) PosePinkSoft else Color.White,
            )
            HeroAction(
                iconRes = R.drawable.ic_pose_share,
                contentDescription = stringResource(R.string.action_share),
                onClick = onShare,
                iconWidth = 18.dp,
                iconHeight = 19.dp,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

@Composable
private fun HeroAction(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    iconWidth: Dp,
    iconHeight: Dp,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(ActionSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = ACTION_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = ACTION_BORDER_ALPHA), CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(width = iconWidth, height = iconHeight),
        )
    }
}
