package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val RailWidth = 48.dp
private val RailShape = RoundedCornerShape(24.dp)
private val ItemSize = 34.dp
private val ItemSlotHeight = 48.dp
private val GlyphSize = 18.dp
private const val ACTIVE_BACKGROUND_ALPHA = 0.28f
private const val RAIL_GLOSS_LAYER_ALPHA = 0.4f

@Composable
internal fun CameraToolRail(
    timer: CaptureTimer,
    isToolActive: (CameraTool) -> Boolean,
    onToolClick: (CameraTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(RailWidth)
            .cameraGlass(
                shape = RailShape,
                borderColor = Color.White.copy(alpha = CAMERA_CARD_BORDER_ALPHA),
                glossLayerAlpha = RAIL_GLOSS_LAYER_ALPHA,
                glossInset = 6.dp,
            )
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
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
            .size(width = RailWidth, height = ItemSlotHeight)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ItemSize)
                .clip(CircleShape)
                .background(if (isActive) Violet.copy(alpha = ACTIVE_BACKGROUND_ALPHA) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(tool.iconRes),
                contentDescription = stringResource(tool.labelRes),
                colorFilter = ColorFilter.tint(if (isActive) PoseVioletPale else Color.White),
                modifier = Modifier.size(GlyphSize),
            )
        }
        if (tool == CameraTool.Timer && timer.isEnabled) {
            Text(
                text = stringResource(R.string.camera_timer_badge, timer.seconds),
                style = poseTextStyle(8.sp, FontWeight.Bold, PoseVioletPale),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 6.dp),
            )
        }
    }
}
