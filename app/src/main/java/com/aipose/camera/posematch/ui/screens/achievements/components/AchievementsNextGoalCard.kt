package com.aipose.camera.posematch.ui.screens.achievements.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.StreakGoal
import com.aipose.camera.posematch.ui.common.poseElevatedSurface
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val CardCorner = 26.dp
private val CardMinHeight = 110.dp
private val BarHeight = 8.dp
private val BarShape = RoundedCornerShape(4.dp)

private const val TRACK_ALPHA = 0.1f

@Composable
internal fun AchievementsNextGoalCard(
    goal: StreakGoal,
    modifier: Modifier = Modifier,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val fillColors = if (isRtl) listOf(PosePink, PoseAmber) else listOf(PoseAmber, PosePink)
    val fraction = (goal.current.toFloat() / goal.target.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardMinHeight)
            .poseElevatedSurface(CardCorner)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.achievements_next_goal, goal.target),
                style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.achievements_next_progress, goal.current, goal.target).bidiIsolate(),
                style = poseTextStyle(11.sp, FontWeight.Bold, PoseAmber400),
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .clip(BarShape)
                .background(Color.White.copy(alpha = TRACK_ALPHA)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(BarShape)
                    .background(Brush.horizontalGradient(fillColors)),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = pluralStringResource(
                R.plurals.achievements_next_hint,
                goal.remainingDays,
                goal.remainingDays,
            ),
            style = poseTextStyle(10.5.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
        )
    }
}
