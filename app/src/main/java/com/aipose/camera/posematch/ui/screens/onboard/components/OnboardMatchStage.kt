package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.onboard.data.ONBOARD_LIVE_MATCH_PERCENT
import com.aipose.camera.posematch.ui.screens.onboard.data.ONBOARD_OVERLAY_PERCENT
import com.aipose.camera.posematch.ui.theme.Emerald
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseFuchsiaLight
import com.aipose.camera.posematch.ui.theme.PoseIndigo400
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val PreviewStart = 39.dp
private val PreviewTop = 28.dp
private val PreviewWidth = 232.dp
private val PreviewHeight = 300.dp
private val PreviewShape = RoundedCornerShape(20.dp)
private val GridInset = 8.dp
private val OverlayPillStart = 55.dp
private val OverlayPillTop = 48.dp
private val OverlayPillHeight = 24.dp
private val OverlayPillShape = RoundedCornerShape(12.dp)
private val RingStart = 207.dp
private val RingTop = 43.dp
private val RingSize = 52.dp
private val RingStroke = 4.dp
private val RingArcDiameter = 40.dp
private val MatchPillStart = 64.dp
private val MatchPillTop = 280.dp
private val MatchPillHeight = 34.dp
private val MatchPillShape = RoundedCornerShape(17.dp)
private val CheckBubbleSize = 18.dp
private val CheckIconWidth = 12.dp
private val CheckIconHeight = 10.dp

private const val GRID_ALPHA = 0.13f
private const val OVERLAY_FILL_ALPHA = 0.45f
private const val OVERLAY_BORDER_ALPHA = 0.18f
private const val RING_FILL_ALPHA = 0.5f
private const val RING_BORDER_ALPHA = 0.16f
private const val RING_TRACK_ALPHA = 0.18f
private const val MATCH_PILL_ALPHA = 0.92f
private const val CHECK_BUBBLE_ALPHA = 0.25f
private const val SWEEP_START_DEGREES = -90f
private const val FULL_TURN_DEGREES = 360f
private const val PERCENT_SCALE = 100f
private const val OVERLAY_IMAGE_ALPHA = ONBOARD_OVERLAY_PERCENT / PERCENT_SCALE

@Composable
internal fun OnboardMatchStage(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = PreviewStart, y = PreviewTop)
                .size(width = PreviewWidth, height = PreviewHeight)
                .clip(PreviewShape),
        ) {
            Image(
                painter = painterResource(R.drawable.onboard_camera_preview),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Image(
                painter = painterResource(R.drawable.onboard_pose_viral),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = OVERLAY_IMAGE_ALPHA,
                modifier = Modifier.fillMaxSize(),
            )
            Canvas(modifier = Modifier.fillMaxSize()) { drawThirdsGrid() }
        }
        OverlayPill()
        MatchRing()
        MatchHintPill()
    }
}

private fun DrawScope.drawThirdsGrid() {
    val inset = GridInset.toPx()
    val color = Color.White.copy(alpha = GRID_ALPHA)
    for (step in 1..2) {
        val x = size.width * step / 3f
        drawLine(color, Offset(x, inset), Offset(x, size.height - inset))
        val y = size.height * step / 3f
        drawLine(color, Offset(inset, y), Offset(size.width - inset, y))
    }
}

@Composable
private fun BoxScope.OverlayPill() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = OverlayPillStart, y = OverlayPillTop)
            .height(OverlayPillHeight)
            .clip(OverlayPillShape)
            .background(Color.Black.copy(alpha = OVERLAY_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = OVERLAY_BORDER_ALPHA), OverlayPillShape)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.onboard_overlay_label, ONBOARD_OVERLAY_PERCENT),
            style = poseTextStyle(8.5.sp, FontWeight.Bold, PoseTextBright),
        )
    }
}

@Composable
private fun BoxScope.MatchRing() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = RingStart, y = RingTop)
            .size(RingSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = RING_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = RING_BORDER_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawMatchRing() }
        Text(
            text = stringResource(R.string.onboard_match_percent, ONBOARD_LIVE_MATCH_PERCENT),
            style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
        )
    }
}

private fun DrawScope.drawMatchRing() {
    val stroke = RingStroke.toPx()
    val diameter = RingArcDiameter.toPx()
    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
    val arcSize = Size(diameter, diameter)
    drawArc(
        color = Color.White.copy(alpha = RING_TRACK_ALPHA),
        startAngle = 0f,
        sweepAngle = FULL_TURN_DEGREES,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke),
    )
    drawArc(
        brush = Brush.linearGradient(
            listOf(PoseCyanBright, PoseIndigo400, PoseFuchsiaLight),
            start = topLeft,
            end = Offset(topLeft.x + diameter, topLeft.y + diameter),
        ),
        startAngle = SWEEP_START_DEGREES,
        sweepAngle = FULL_TURN_DEGREES * ONBOARD_LIVE_MATCH_PERCENT / PERCENT_SCALE,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

@Composable
private fun BoxScope.MatchHintPill() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = MatchPillStart, y = MatchPillTop)
            .height(MatchPillHeight)
            .clip(MatchPillShape)
            .background(Emerald.copy(alpha = MATCH_PILL_ALPHA))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(CheckBubbleSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = CHECK_BUBBLE_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pose_check_small),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(width = CheckIconWidth, height = CheckIconHeight),
            )
        }
        Text(
            text = stringResource(R.string.onboard_match_hint),
            style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
        )
    }
}
