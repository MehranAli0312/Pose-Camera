package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.camera.models.MatchFeedback
import com.aipose.camera.posematch.ui.theme.PoseEmeraldLight
import com.aipose.camera.posematch.ui.theme.PoseTextLavender
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardShape = RoundedCornerShape(18.dp)
private val CardHeight = 64.dp
private val RingSize = 38.dp
private val RingStroke = 3.5.dp
private val BestShape = RoundedCornerShape(12.dp)
private const val SCRIM_ALPHA = 0.5f
private const val BORDER_ALPHA = 0.1f
private const val TRACK_ALPHA = 0.16f
private const val BEST_BACKGROUND_ALPHA = 0.18f
private const val FULL_SWEEP = 360f
private const val SWEEP_START = -90f
private const val PERCENT_SPAN = 100f
private const val RING_ANIMATION_MILLIS = 220

@Composable
internal fun CameraMatchCard(
    score: Int,
    feedback: MatchFeedback,
    bestScore: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CardHeight)
            .clip(CardShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), CardShape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScoreRing(score = score, ringColor = feedback.ringColor)
        Spacer(modifier = Modifier.size(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = stringResource(feedback.titleRes),
                style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(feedback.hintRes),
                style = poseTextStyle(9.5.sp, FontWeight.Normal, PoseTextLavender),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (bestScore > 0) {
            Spacer(modifier = Modifier.size(10.dp))
            Box(
                modifier = Modifier
                    .clip(BestShape)
                    .background(PoseEmeraldLight.copy(alpha = BEST_BACKGROUND_ALPHA))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = stringResource(R.string.camera_best_score, bestScore),
                    style = poseTextStyle(9.sp, FontWeight.Bold, PoseEmeraldLight),
                )
            }
        }
    }
}

@Composable
private fun ScoreRing(
    score: Int,
    ringColor: Color,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = (score / PERCENT_SPAN).coerceIn(0f, 1f),
        animationSpec = tween(RING_ANIMATION_MILLIS),
        label = "matchRing",
    )
    Box(
        modifier = modifier.size(RingSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RingSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = SCRIM_ALPHA))
        )
        Canvas(modifier = Modifier.size(RingSize)) {
            val stroke = RingStroke.toPx()
            val inset = stroke / 2f
            val diameter = size.minDimension - stroke
            drawArc(
                color = Color.White.copy(alpha = TRACK_ALPHA),
                startAngle = 0f,
                sweepAngle = FULL_SWEEP,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke),
            )
            drawArc(
                color = ringColor,
                startAngle = SWEEP_START,
                sweepAngle = FULL_SWEEP * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Text(
            text = stringResource(R.string.score_percent, score),
            style = poseTextStyle(10.5.sp, FontWeight.Bold, Color.White),
        )
    }
}
