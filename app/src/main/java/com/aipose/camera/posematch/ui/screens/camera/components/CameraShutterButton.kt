package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick

private val ShutterSize = 74.dp
private val ShutterStroke = 4.dp
private val CameraGlyphSize = DpSize(28.dp, 19.5.dp)

@Composable
internal fun CameraShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ShutterSize)
            .border(ShutterStroke, Color.White, CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_camera_fab),
            contentDescription = stringResource(R.string.camera_shutter),
            modifier = Modifier.size(CameraGlyphSize),
        )
    }
}
