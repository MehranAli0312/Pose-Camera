package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.camera.models.CameraTool
import com.aipose.camera.posematch.ui.screens.camera.models.CaptureTimer
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val RailShape = RoundedCornerShape(18.dp)
private val ItemShape = RoundedCornerShape(11.dp)
private val ItemSize = 36.dp
private val GlyphSize = 19.dp
private const val SCRIM_ALPHA = 0.45f
private const val BORDER_ALPHA = 0.1f
private const val ACTIVE_BACKGROUND_ALPHA = 0.22f

@Composable
internal fun CameraToolRail(
    timer: CaptureTimer,
    isToolActive: (CameraTool) -> Boolean,
    onToolClick: (CameraTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RailShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), RailShape)
            .padding(vertical = 7.dp, horizontal = 5.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CameraTool.entries.forEach { tool ->
            CameraToolItem(
                tool = tool,
                isActive = isToolActive(tool),
                timer = timer,
                onClick = { onToolClick(tool) },
            )
        }
    }
}

@Composable
private fun CameraToolItem(
    tool: CameraTool,
    isActive: Boolean,
    timer: CaptureTimer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ItemSize)
            .clip(ItemShape)
            .background(
                if (isActive) {
                    PoseVioletLight.copy(alpha = ACTIVE_BACKGROUND_ALPHA)
                } else {
                    Color.Transparent
                }
            )
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(tool.iconRes),
            contentDescription = stringResource(tool.labelRes),
            colorFilter = ColorFilter.tint(if (isActive) PoseVioletPale else Color.White),
            modifier = Modifier.size(GlyphSize),
        )
        if (tool == CameraTool.Timer && timer.isEnabled) {
            Text(
                text = stringResource(R.string.camera_timer_badge, timer.seconds),
                style = poseTextStyle(7.5.sp, FontWeight.Bold, PoseVioletPale),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 1.dp, bottom = 1.dp),
            )
        }
    }
}
