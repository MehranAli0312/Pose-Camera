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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.aipose.camera.posematch.ui.theme.Emerald
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseEmeraldLight
import com.aipose.camera.posematch.ui.theme.PoseFuchsiaLight
import com.aipose.camera.posematch.ui.theme.PoseIndigo400
import com.aipose.camera.posematch.ui.theme.PoseTextLavender
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val CardShape = RoundedCornerShape(22.dp)
private val CardMinHeight = 62.dp
private val RingSize = 40.dp
private val RingStroke = 4.dp
private val BestShape = RoundedCornerShape(14.dp)
private val BestMinHeight = 28.dp
private val RingColors = listOf(PoseCyanBright, PoseIndigo400, PoseFuchsiaLight)
private const val CARD_GLOSS_LAYER_ALPHA = 0.4f
private const val TRACK_ALPHA = 0.2f
private const val BEST_BACKGROUND_ALPHA = 0.2f
private const val BEST_BORDER_ALPHA = 0.5f
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
            .heightIn(min = CardMinHeight)
            .cameraGlass(
                shape = CardShape,
                scrimAlpha = CAMERA_CARD_SCRIM_ALPHA,
                borderColor = Color.White.copy(alpha = CAMERA_CARD_BORDER_ALPHA),
                glossLayerAlpha = CARD_GLOSS_LAYER_ALPHA,
                glossInset = 8.dp,
            )
            .padding(start = 12.dp, end = 20.dp, top = 11.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScoreRing(score = score)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(feedback.titleRes),
                style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(feedback.hintRes),
                style = poseTextStyle(10.sp, FontWeight.Normal, PoseTextLavender),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (bestScore > 0) {
            Box(
                modifier = Modifier
                    .heightIn(min = BestMinHeight)
                    .clip(BestShape)
                    .background(Emerald.copy(alpha = BEST_BACKGROUND_ALPHA))
                    .border(1.dp, Emerald.copy(alpha = BEST_BORDER_ALPHA), BestShape)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.camera_best_score, bestScore).bidiIsolate(),
                    style = poseTextStyle(9.sp, FontWeight.Bold, PoseEmeraldLight),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ScoreRing(
    score: Int,
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
                brush = Brush.linearGradient(RingColors, start = Offset.Zero, end = Offset(size.width, size.height)),
                startAngle = SWEEP_START,
                sweepAngle = FULL_SWEEP * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Text(
            text = stringResource(R.string.score_percent, score).bidiIsolate(),
            style = poseTextStyle(10.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}
