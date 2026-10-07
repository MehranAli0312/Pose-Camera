package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BadgeShape = RoundedCornerShape(11.dp)
private val BadgeMinHeight = 22.dp
private val BadgeGlyphSize = 11.dp
private val BadgeLabelSize = 11.sp
private const val BADGE_FILL_ALPHA = 0.6f

@Composable
fun PoseProBadge(
    modifier: Modifier = Modifier,
    withLock: Boolean = true,
) {
    Row(
        modifier = modifier
            .heightIn(min = BadgeMinHeight)
            .clip(BadgeShape)
            .background(Color.Black.copy(alpha = BADGE_FILL_ALPHA))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (withLock) {
            Icon(
                painter = painterResource(R.drawable.ic_hero_lock_small),
                contentDescription = null,
                tint = PosePremiumGold,
                modifier = Modifier.size(BadgeGlyphSize),
            )
        }
        Text(
            text = stringResource(R.string.pose_pro_badge),
            style = poseTextStyle(BadgeLabelSize, FontWeight.Bold, PosePremiumGold),
            maxLines = 1,
        )
    }
}
