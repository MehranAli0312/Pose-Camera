package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.GlossyIconCircle
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.isProUser
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseStreakBottom
import com.aipose.camera.posematch.ui.theme.PoseStreakTop
import com.aipose.camera.posematch.ui.theme.PoseTextSoft
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val HeaderHeight = 72.dp
private val LogoSize = 44.dp
private val TrophySize = 40.dp
private val StreakShape = RoundedCornerShape(20.dp)
private const val STREAK_BORDER_ALPHA = 0.35f

@Composable
internal fun HomeHeader(
    streakDays: Int,
    onOpenPro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlossyIconBadge(
            iconRes = R.drawable.ic_pose_logo,
            palette = GlossyBadgePalette.Indigo,
            size = LogoSize,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = stringResource(R.string.home_title_pose),
                    style = poseTextStyle(21.sp, FontWeight.Bold, Color.White),
                )
                Text(
                    text = stringResource(R.string.home_title_ai),
                    style = poseTextStyle(21.sp, FontWeight.Bold, PoseVioletLight),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_tagline),
                style = poseTextStyle(9.5.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
            )
        }
        StreakPill(streakDays = streakDays)
        if (!isProUser()) {
            Spacer(modifier = Modifier.width(14.dp))
            GlossyIconCircle(
                iconRes = R.drawable.ic_pose_trophy,
                palette = GlossyBadgePalette.Amber,
                size = TrophySize,
                contentDescription = stringResource(R.string.pro_upgrade_title),
                modifier = Modifier.bounceClick(onClick = onOpenPro),
            )
        }
    }
}

@Composable
private fun StreakPill(streakDays: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(StreakShape)
            .background(Brush.verticalGradient(listOf(PoseStreakTop, PoseStreakBottom)))
            .border(1.dp, PoseAmber.copy(alpha = STREAK_BORDER_ALPHA), StreakShape)
            .padding(start = 14.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_streak_flame),
            contentDescription = null,
            modifier = Modifier.size(width = 13.dp, height = 19.dp),
        )
        Column {
            Text(
                text = streakDays.toString(),
                style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
            )
            Text(
                text = stringResource(R.string.home_day_streak),
                style = poseTextStyle(8.5.sp, FontWeight.Bold, PoseTextSoft),
            )
        }
    }
}
