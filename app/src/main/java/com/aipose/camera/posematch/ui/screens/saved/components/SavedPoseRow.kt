package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.PoseProBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.models.badgeForCategory
import com.aipose.camera.posematch.ui.screens.saved.models.SavedPose
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PosePinkSoft
import com.aipose.camera.posematch.ui.theme.PoseRaisedBottom
import com.aipose.camera.posematch.ui.theme.PoseRaisedTop
import com.aipose.camera.posematch.ui.theme.PoseTextFaint
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val RowMinHeight = 60.dp
private val RowShape = RoundedCornerShape(20.dp)
private val BadgeSize = 38.dp
private val HeartSize = 14.dp
private val HeartTouchSize = 36.dp
private val ChevronWidth = 5.dp
private val ChevronHeight = 10.dp

private const val ROW_BORDER_ALPHA = 0.09f

@Composable
internal fun SavedPoseRow(
    saved: SavedPose,
    onClick: () -> Unit,
    onUnsave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badge = badgeForCategory(saved.pose.category)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .clip(RowShape)
            .background(Brush.verticalGradient(listOf(PoseRaisedTop, PoseRaisedBottom)))
            .border(1.dp, Color.White.copy(alpha = ROW_BORDER_ALPHA), RowShape)
            .bounceClick(onClick = onClick)
            .padding(start = 16.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlossyIconBadge(
            iconRes = badge.iconRes,
            palette = badge.palette,
            size = BadgeSize,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 12.dp),
        ) {
            Text(
                text = saved.pose.title,
                style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.saved_pose_meta,
                    saved.pose.category,
                    saved.pose.difficulty,
                ),
                style = poseTextStyle(10.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (saved.isLocked) {
                PoseProBadge(modifier = Modifier.padding(end = 4.dp))
            }
            Box(
                modifier = Modifier
                    .size(HeartTouchSize)
                    .click(onClick = onUnsave),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_pose_heart_row),
                    contentDescription = stringResource(R.string.saved_remove),
                    colorFilter = ColorFilter.tint(PosePinkSoft),
                    modifier = Modifier.size(HeartSize),
                )
            }
            Image(
                painter = painterResource(R.drawable.ic_pose_chevron_end),
                contentDescription = null,
                colorFilter = ColorFilter.tint(PoseTextFaint),
                modifier = Modifier.size(width = ChevronWidth, height = ChevronHeight),
            )
        }
    }
}
