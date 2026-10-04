package com.aipose.camera.posematch.ui.screens.home.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.CaptureProgress
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseCard
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseVioletBright
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val CardShape = RoundedCornerShape(24.dp)
private val CardMinHeight = 140.dp
private val StatsShape = RoundedCornerShape(18.dp)
private val StatsMinHeight = 76.dp
private val HeaderBadgeSize = 26.dp
private val StatBadgeSize = 28.dp
private val RingSize = 41.dp
private val RingStroke = 5.dp

private const val STATS_FILL_ALPHA = 0.04f
private const val DIVIDER_ALPHA = 0.08f
private const val TRACK_ALPHA = 0.12f
private const val DIVIDER_TOP_RATIO = 0.184f
private const val DIVIDER_BOTTOM_RATIO = 0.816f
private const val RING_START_ANGLE = -90f
private const val FULL_SWEEP = 360f
private const val PERCENT_SCALE = 100f

@Composable
internal fun HomeProgressCard(
    progress: CaptureProgress,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardMinHeight)
            .poseCard(CardShape)
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        ProgressHeader(onViewAll = onViewAll)
        Spacer(modifier = Modifier.height(8.dp))
        StatsRow(progress = progress)
    }
}

@Composable
private fun ProgressHeader(onViewAll: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlossyIconBadge(
            iconRes = R.drawable.ic_pose_chart,
            palette = GlossyBadgePalette.Indigo,
            size = HeaderBadgeSize,
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = stringResource(R.string.home_progress_title),
            style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.bounceClick(onClick = onViewAll),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.home_progress_view_all),
                style = poseTextStyle(10.5.sp, FontWeight.Bold, PoseVioletLight),
            )
            Image(
                painter = painterResource(R.drawable.ic_pose_chevron_end),
                contentDescription = null,
                colorFilter = ColorFilter.tint(PoseVioletLight),
                modifier = Modifier.size(width = 8.dp, height = 10.dp),
            )
        }
    }
}

@Composable
private fun StatsRow(progress: CaptureProgress, modifier: Modifier = Modifier) {
    val dividerColor = Color.White.copy(alpha = DIVIDER_ALPHA)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = StatsMinHeight)
            .clip(StatsShape)
            .background(Color.White.copy(alpha = STATS_FILL_ALPHA))
            .drawBehind {
                val top = size.height * DIVIDER_TOP_RATIO
                val bottom = size.height * DIVIDER_BOTTOM_RATIO
                for (index in 1..3) {
                    val x = size.width * index / 4f
                    drawLine(
                        color = dividerColor,
                        start = Offset(x, top),
                        end = Offset(x, bottom),
                    )
                }
            },
    ) {
        AverageMatchStat(percent = progress.averageMatch)
        BadgeStat(
            iconRes = R.drawable.ic_pose_star,
            palette = GlossyBadgePalette.Amber,
            value = progress.shotsTaken,
            labelRes = R.string.home_progress_shots,
        )
        BadgeStat(
            iconRes = R.drawable.ic_pose_trophy_small,
            palette = GlossyBadgePalette.Pink,
            value = progress.perfectShots,
            labelRes = R.string.home_progress_perfect,
        )
        BadgeStat(
            iconRes = R.drawable.ic_pose_target,
            palette = GlossyBadgePalette.Cyan,
            value = progress.dayStreak,
            labelRes = R.string.home_progress_day_goal,
        )
    }
}

@Composable
private fun RowScope.AverageMatchStat(percent: Int) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(7.dp))
        Box(modifier = Modifier.size(RingSize), contentAlignment = Alignment.Center) {
            MatchRing(percent = percent)
            Text(
                text = stringResource(R.string.score_percent, percent).bidiIsolate(),
                style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        StatLabel(labelRes = R.string.home_progress_avg_match)
    }
}

@Composable
private fun MatchRing(percent: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(RingSize)) {
        val stroke = RingStroke.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(
            color = Color.White.copy(alpha = TRACK_ALPHA),
            startAngle = 0f,
            sweepAngle = FULL_SWEEP,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke),
        )
        drawArc(
            brush = Brush.linearGradient(listOf(PoseCyanBright, Indigo, PoseVioletBright)),
            startAngle = RING_START_ANGLE,
            sweepAngle = FULL_SWEEP * percent / PERCENT_SCALE,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun RowScope.BadgeStat(
    @DrawableRes iconRes: Int,
    palette: GlossyBadgePalette,
    value: Int,
    @StringRes labelRes: Int,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        GlossyIconBadge(iconRes = iconRes, palette = palette, size = StatBadgeSize)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value.toString(),
            style = poseTextStyle(16.sp, FontWeight.Bold, Color.White),
        )
        Spacer(modifier = Modifier.height(1.dp))
        StatLabel(labelRes = labelRes)
    }
}

@Composable
private fun StatLabel(@StringRes labelRes: Int) {
    Text(
        text = stringResource(labelRes),
        style = poseTextStyle(8.5.sp, FontWeight.Normal, LocalAppPalette.current.textFaint),
        textAlign = TextAlign.Center,
    )
}
