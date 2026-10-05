package com.aipose.camera.posematch.ui.screens.achievements.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.StreakDay
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseAmberLight
import com.aipose.camera.posematch.ui.theme.PoseOrangeShadow
import com.aipose.camera.posematch.ui.theme.PoseStreakSand
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val RingSize = 36.dp
private val CircleSize = 30.dp
private val TodayDotSize = 8.dp
private val CheckWidth = 12.dp
private val CheckHeight = 10.dp

private const val TODAY_RING_ALPHA = 0.5f
private const val EMPTY_FILL_ALPHA = 0.08f
private const val EMPTY_BORDER_ALPHA = 0.14f
private const val WEEKDAY_PATTERN = "EEEEE"

@Composable
internal fun StreakWeekRow(
    days: List<StreakDay>,
    modifier: Modifier = Modifier,
) {
    val labels = remember(days) {
        val format = SimpleDateFormat(WEEKDAY_PATTERN, Locale.getDefault())
        days.map { day -> format.format(Date(day.dayMillis)) }
    }
    Row(modifier = modifier.fillMaxWidth()) {
        days.forEachIndexed { index, day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier.size(RingSize),
                    contentAlignment = Alignment.Center,
                ) {
                    StreakDayCircle(day = day)
                }
                Text(
                    text = labels[index],
                    style = poseTextStyle(
                        9.sp,
                        FontWeight.Bold,
                        if (day.isToday) Color.White else PoseStreakSand,
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun StreakDayCircle(day: StreakDay) {
    when {
        day.isToday -> Box(
            modifier = Modifier
                .size(RingSize)
                .border(1.5.dp, PoseAmberLight.copy(alpha = TODAY_RING_ALPHA), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(CircleSize)
                    .background(PoseAmberLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (day.hasCapture) {
                    StreakCheck(tint = PoseOrangeShadow)
                } else {
                    Box(
                        modifier = Modifier
                            .size(TodayDotSize)
                            .background(PoseOrangeShadow, CircleShape),
                    )
                }
            }
        }
        day.hasCapture -> Box(
            modifier = Modifier
                .size(CircleSize)
                .background(PoseAmber, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            StreakCheck(tint = Color.White)
        }
        else -> Box(
            modifier = Modifier
                .size(CircleSize)
                .background(Color.White.copy(alpha = EMPTY_FILL_ALPHA), CircleShape)
                .border(1.dp, Color.White.copy(alpha = EMPTY_BORDER_ALPHA), CircleShape),
        )
    }
}

@Composable
private fun StreakCheck(tint: Color) {
    Image(
        painter = painterResource(R.drawable.ic_pose_check_small),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = Modifier.size(width = CheckWidth, height = CheckHeight),
    )
}
