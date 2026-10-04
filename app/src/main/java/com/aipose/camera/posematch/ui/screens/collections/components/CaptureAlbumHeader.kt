package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.aipose.camera.posematch.ui.screens.collections.models.AlbumAccent
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val PinWidth = 16.dp
private val PinHeight = 22.dp
private val CountShape = RoundedCornerShape(10.dp)
private val ChevronWidth = 6.dp
private val ChevronHeight = 10.dp
private const val COUNT_BACKGROUND_ALPHA = 0.22f

@Composable
internal fun CaptureAlbumHeader(
    locationLabel: String,
    count: Int,
    accent: AlbumAccent,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_saved_pin),
            contentDescription = null,
            colorFilter = ColorFilter.tint(accent.pin),
            modifier = Modifier.size(width = PinWidth, height = PinHeight),
        )
        Text(
            text = locationLabel,
            style = poseTextStyle(15.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(start = 8.dp, end = 10.dp),
        )
        Box(
            modifier = Modifier
                .clip(CountShape)
                .background(accent.pin.copy(alpha = COUNT_BACKGROUND_ALPHA))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = count.toString(),
                style = poseTextStyle(10.sp, FontWeight.Bold, accent.label),
            )
        }
        Box(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .bounceClick(onClick = onShowAll)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.see_all),
                style = poseTextStyle(11.sp, FontWeight.Bold, PoseVioletLight),
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
