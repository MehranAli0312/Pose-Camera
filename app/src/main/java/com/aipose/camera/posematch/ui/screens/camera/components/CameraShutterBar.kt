package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BarHeight = 96.dp
private val SideButtonSize = 48.dp
private val ShutterSize = 86.dp
private val ShutterCoreSize = 66.dp
private val ShutterRing = 3.dp
private val BadgeSize = 18.dp
private val GlyphSize = 20.dp
private const val SCRIM_ALPHA = 0.45f
private const val BORDER_ALPHA = 0.12f

@Composable
internal fun CameraShutterBar(
    galleryCount: Int,
    onGalleryClick: () -> Unit,
    onShutterClick: () -> Unit,
    onToggleFacing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight)
            .padding(horizontal = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GalleryButton(count = galleryCount, onClick = onGalleryClick)
        ShutterButton(onClick = onShutterClick)
        GlassCircleButton(
            iconRes = R.drawable.ic_camera_flip,
            contentDescription = stringResource(R.string.camera_switch),
            onClick = onToggleFacing,
        )
    }
}

@Composable
private fun GalleryButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(SideButtonSize + BadgeSize / 2)) {
        GlassCircleButton(
            iconRes = R.drawable.ic_camera_gallery,
            contentDescription = stringResource(R.string.camera_gallery),
            onClick = onClick,
            modifier = Modifier.align(Alignment.BottomStart),
            tint = null,
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(BadgeSize)
                    .clip(CircleShape)
                    .background(PoseVioletLight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    style = poseTextStyle(9.sp, FontWeight.Bold, Color.White),
                )
            }
        }
    }
}

@Composable
private fun ShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ShutterSize)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(ShutterSize)) {
            val stroke = ShutterRing.toPx()
            drawCircle(
                color = PoseEmerald400,
                radius = size.minDimension / 2f - stroke / 2f,
                style = Stroke(width = stroke),
            )
        }
        Box(
            modifier = Modifier
                .size(ShutterCoreSize)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun GlassCircleButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = Color.White,
) {
    Box(
        modifier = modifier
            .size(SideButtonSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = tint?.let { color -> ColorFilter.tint(color) },
            modifier = Modifier.size(GlyphSize),
        )
    }
}
