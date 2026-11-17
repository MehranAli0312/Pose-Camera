package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.theme.PoseSky
import com.aipose.camera.posematch.ui.theme.SurfaceBarDark
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ButtonHeight = 48.dp
private val IconSize = 20.dp

@Composable
internal fun PhotoDetailActions(
    onSend: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .safeBottomSystemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ActionButton(
            iconRes = R.drawable.ic_trash,
            label = stringResource(R.string.action_delete),
            backgroundColor = SurfaceBarDark,
            onClick = onDelete,
            modifier = Modifier.weight(1f),
        )
        ActionButton(
            iconRes = R.drawable.ic_pose_arrow_end,
            label = stringResource(R.string.capture_send_to),
            backgroundColor = PoseSky,
            onClick = onSend,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionButton(
    @DrawableRes iconRes: Int,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(ButtonHeight)
            .clip(CircleShape)
            .background(backgroundColor)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(IconSize),
        )
        Text(
            text = label,
            style = poseTextStyle(13.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
