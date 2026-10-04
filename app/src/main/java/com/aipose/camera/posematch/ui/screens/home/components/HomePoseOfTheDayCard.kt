package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.CaptureFab
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseRaisedCard
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseDailyBottom
import com.aipose.camera.posematch.ui.theme.PoseDailyMid
import com.aipose.camera.posematch.ui.theme.PoseDailySubtitle
import com.aipose.camera.posematch.ui.theme.PoseDailyTitle
import com.aipose.camera.posematch.ui.theme.PoseDailyTop
import com.aipose.camera.posematch.ui.theme.PoseMagenta
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardCorner = 24.dp
private val CardHeight = 112.dp
private val BulbSize = 34.dp
private val ThumbSize = 28.dp
private val ThumbShape = RoundedCornerShape(10.dp)
private val PoseRowShape = RoundedCornerShape(14.dp)
private val PoseRowHeight = 36.dp
private val CaptureSize = 60.dp
private val CaptureGlowSize = 72.dp
private val CaptureGlyphWidth = 25.dp

private const val CARD_BORDER_ALPHA = 0.12f
private const val CARD_SHADOW_ALPHA = 0.5f
private const val ROW_FILL_ALPHA = 0.28f
private const val ROW_BORDER_ALPHA = 0.10f
private const val GRADIENT_MID_STOP = 0.55f

@Composable
internal fun HomePoseOfTheDayCard(
    pose: Pose,
    onTryPose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CardHeight)
            .poseRaisedCard(
                cornerRadius = CardCorner,
                brush = Brush.linearGradient(
                    0f to PoseDailyTop,
                    GRADIENT_MID_STOP to PoseDailyMid,
                    1f to PoseDailyBottom,
                ),
                shadowColor = PoseShadow,
                shadowAlpha = CARD_SHADOW_ALPHA,
                borderColor = Color.White.copy(alpha = CARD_BORDER_ALPHA),
            )
            .bounceClick(onClick = onTryPose),
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, top = 14.dp, end = 110.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GlossyIconBadge(
                    iconRes = R.drawable.ic_pose_bulb,
                    palette = GlossyBadgePalette.Bulb,
                    size = BulbSize,
                )
                Column {
                    Text(
                        text = stringResource(R.string.home_daily_title),
                        style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.home_daily_subtitle),
                        style = poseTextStyle(9.5.sp, FontWeight.Normal, PoseDailyTitle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            DailyPoseRow(pose = pose)
        }
        CaptureFab(
            onClick = onTryPose,
            glowColor = PoseMagenta,
            contentDescription = stringResource(R.string.home_hero_cta),
            buttonSize = CaptureSize,
            glowSize = CaptureGlowSize,
            glyphSize = CaptureGlyphWidth,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
        )
    }
}

@Composable
private fun DailyPoseRow(pose: Pose, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(PoseRowHeight)
            .clip(PoseRowShape)
            .background(Color.Black.copy(alpha = ROW_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = ROW_BORDER_ALPHA), PoseRowShape)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PoseImage(
            imagePath = pose.imagePath,
            contentDescription = null,
            modifier = Modifier
                .size(ThumbSize)
                .clip(ThumbShape),
        )
        Column {
            Text(
                text = pose.title,
                style = poseTextStyle(11.5.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.home_daily_meta,
                    pose.category,
                    pose.difficulty,
                ),
                style = poseTextStyle(8.5.sp, FontWeight.Normal, PoseDailySubtitle),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

