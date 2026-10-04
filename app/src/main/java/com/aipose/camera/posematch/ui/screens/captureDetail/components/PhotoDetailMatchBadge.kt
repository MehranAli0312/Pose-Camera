package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.Emerald
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BadgeShape = RoundedCornerShape(16.dp)
private val BadgeMinHeight = 32.dp
private val CheckGlyphSize = 12.dp
private const val BADGE_ALPHA = 0.9f

@Composable
internal fun PhotoDetailMatchBadge(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = BadgeMinHeight)
            .clip(BadgeShape)
            .background(Emerald.copy(alpha = BADGE_ALPHA))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_check_small),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(CheckGlyphSize),
        )
        Text(
            text = stringResource(R.string.edit_match_badge, score),
            style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}
