package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun CameraShutterBar(
    onShutterClick: () -> Unit,
    onToggleFacing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(100.dp)
            .background(Color.Black)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.size(48.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(6.dp)
                .bounceClick(onClick = onShutterClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(3.dp, Color.Black, CircleShape)
                    .background(Color.White)
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LocalAppPalette.current.card)
                .bounceClick(onClick = onToggleFacing),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = stringResource(R.string.camera_switch),
                tint = Color.White,
            )
        }
    }
}
