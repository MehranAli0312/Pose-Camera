package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseStreakBottom
import com.aipose.camera.posematch.ui.theme.PoseStreakTop
import com.aipose.camera.posematch.ui.theme.PoseTextSoft
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val StreakMinHeight = 40.dp
private val StreakShape = RoundedCornerShape(20.dp)
private const val STREAK_BORDER_ALPHA = 0.35f

@Composable
internal fun HomeHeader(
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val titleStyle = poseTextStyle(21.sp, FontWeight.Bold, Color.White)
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.home_title_pose))
                    withStyle(titleStyle.toSpanStyle().copy(color = PoseVioletLight)) {
                        append(stringResource(R.string.home_title_ai))
                    }
                },
                style = titleStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.home_tagline),
                style = poseTextStyle(9.5.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        StreakPill(streakDays = streakDays)
    }
}

@Composable
private fun StreakPill(streakDays: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = StreakMinHeight)
            .clip(StreakShape)
            .background(Brush.verticalGradient(listOf(PoseStreakTop, PoseStreakBottom)))
            .border(1.dp, PoseAmber.copy(alpha = STREAK_BORDER_ALPHA), StreakShape)
            .padding(start = 14.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
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
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.home_day_streak),
                style = poseTextStyle(8.5.sp, FontWeight.Bold, PoseTextSoft),
                maxLines = 1,
            )
        }
    }
}
