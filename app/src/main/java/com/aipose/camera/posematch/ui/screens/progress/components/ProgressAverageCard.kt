package com.aipose.camera.posematch.ui.screens.progress.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.poseElevatedSurface
import com.aipose.camera.posematch.ui.theme.Emerald
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseEmeraldLight
import com.aipose.camera.posematch.ui.theme.PoseFuchsiaLight
import com.aipose.camera.posematch.ui.theme.PoseIndigo400
import com.aipose.camera.posematch.ui.theme.PoseRed400
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val CardCorner = 26.dp
private val CardMinHeight = 150.dp
private val RingBoxSize = 101.dp
private val RingStroke = 9.dp
private val ChipHeight = 26.dp
private val ChipShape = RoundedCornerShape(13.dp)
private val TriangleSize = 8.dp

private const val MAX_SCORE = 100f
private const val FULL_SWEEP = 360f
private const val START_ANGLE = -90f
private const val RING_TRACK_ALPHA = 0.12f
private const val CHIP_FILL_ALPHA = 0.18f
private const val CHIP_BORDER_ALPHA = 0.4f

@Composable
internal fun ProgressAverageCard(
    averageMatch: Int,
    delta: Int?,
    @StringRes deltaLabelRes: Int?,
    modifier: Modifier = Modifier,
) {
    val percentText = stringResource(R.string.score_percent, averageMatch).bidiIsolate()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardMinHeight)
            .poseElevatedSurface(CardCorner)
            .padding(start = 25.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(RingBoxSize),
            contentAlignment = Alignment.Center,
        ) {
            MatchRing(
                progress = averageMatch / MAX_SCORE,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = percentText,
                style = poseTextStyle(20.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.width(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.progress_average_label),
                style = poseTextStyle(11.sp, FontWeight.Bold, LocalAppPalette.current.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = percentText,
                style = poseTextStyle(32.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
            )
            if (delta != null && deltaLabelRes != null) {
                Spacer(modifier = Modifier.height(10.dp))
                DeltaChip(delta = delta, labelRes = deltaLabelRes)
            }
        }
    }
}

@Composable
private fun MatchRing(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = RingStroke.toPx()
        val diameter = size.minDimension - stroke
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        drawArc(
            color = Color.White.copy(alpha = RING_TRACK_ALPHA),
            startAngle = 0f,
            sweepAngle = FULL_SWEEP,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke),
        )
        val sweep = FULL_SWEEP * progress.coerceIn(0f, 1f)
        if (sweep <= 0f) return@Canvas
        rotate(START_ANGLE) {
            drawArc(
                brush = Brush.sweepGradient(
                    0f to PoseCyanBright,
                    0.5f to PoseIndigo400,
                    1f to PoseFuchsiaLight,
                ),
                startAngle = 0f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun DeltaChip(
    delta: Int,
    @StringRes labelRes: Int,
) {
    val isRising = delta >= 0
    val tone = if (isRising) Emerald else PoseRed400
    val textColor = if (isRising) PoseEmeraldLight else PoseRed400
    val deltaText = stringResource(R.string.progress_delta_value, delta).bidiIsolate()
    Row(
        modifier = Modifier
            .height(ChipHeight)
            .background(tone.copy(alpha = CHIP_FILL_ALPHA), ChipShape)
            .border(1.dp, tone.copy(alpha = CHIP_BORDER_ALPHA), ChipShape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(modifier = Modifier.size(TriangleSize)) {
            val path = Path().apply {
                if (isRising) {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                } else {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                }
                close()
            }
            drawPath(path, textColor)
        }
        Text(
            text = stringResource(labelRes, deltaText),
            style = poseTextStyle(10.sp, FontWeight.Bold, textColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
