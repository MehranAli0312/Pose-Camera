package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseEmerald600
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val SideButtonSize = 52.dp
private val ShutterSize = 90.dp
private val ShutterHaloSize = 74.dp
private val ShutterCoreSize = 62.dp
private val ShutterStroke = 4.dp
private val BadgeSize = 20.dp
private val BadgeOffsetX = 6.dp
private val BadgeOffsetY = (-8).dp
private val GalleryGlyphSize = DpSize(20.dp, 16.dp)
private val FlipGlyphSize = DpSize(24.dp, 24.dp)
private val ProgressColors = listOf(PoseEmerald400, PoseEmerald600)
private const val TRACK_ALPHA = 0.2f
private const val HALO_ALPHA = 0.22f
private const val FULL_SWEEP = 360f
private const val SWEEP_START = -90f
private const val PERCENT_SPAN = 100f
private const val PROGRESS_ANIMATION_MILLIS = 220
private const val MAX_BADGE_COUNT = 99

@Composable
internal fun CameraShutterBar(
    galleryCount: Int,
    matchScore: Int,
    onGalleryClick: () -> Unit,
    onShutterClick: () -> Unit,
    onToggleFacing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 34.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            SideButton(
                iconRes = R.drawable.ic_camera_gallery,
                glyphSize = GalleryGlyphSize,
                tint = null,
                contentDescription = stringResource(R.string.camera_gallery),
                onClick = onGalleryClick,
            )
            if (galleryCount > 0) {
                CountBadge(
                    count = galleryCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = BadgeOffsetX, y = BadgeOffsetY),
                )
            }
        }
        ShutterButton(matchScore = matchScore, onClick = onShutterClick)
        SideButton(
            iconRes = R.drawable.ic_camera_flip,
            glyphSize = FlipGlyphSize,
            tint = Color.White,
            contentDescription = stringResource(R.string.camera_switch),
            onClick = onToggleFacing,
        )
    }
}

@Composable
private fun ShutterButton(
    matchScore: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = (matchScore / PERCENT_SPAN).coerceIn(0f, 1f),
        animationSpec = tween(PROGRESS_ANIMATION_MILLIS),
        label = "shutterProgress",
    )
    val description = stringResource(R.string.camera_shutter)
    Box(
        modifier = modifier
            .size(ShutterSize)
            .semantics { contentDescription = description }
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(ShutterSize)) {
            val stroke = ShutterStroke.toPx()
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
                brush = Brush.linearGradient(ProgressColors, start = Offset.Zero, end = Offset(size.width, size.height)),
                startAngle = SWEEP_START,
                sweepAngle = FULL_SWEEP * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Box(
            modifier = Modifier
                .size(ShutterHaloSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = HALO_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(ShutterCoreSize)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun SideButton(
    @DrawableRes iconRes: Int,
    glyphSize: DpSize,
    tint: Color?,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(SideButtonSize)
            .cameraGlass(CircleShape, scrimAlpha = CAMERA_CARD_SCRIM_ALPHA)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = tint?.let { color -> ColorFilter.tint(color) },
            modifier = Modifier.size(glyphSize),
        )
    }
}

@Composable
private fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(BadgeSize)
            .clip(CircleShape)
            .background(Violet),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.coerceAtMost(MAX_BADGE_COUNT).toString(),
            style = poseTextStyle(9.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}
