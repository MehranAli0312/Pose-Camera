package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R

private val SimulatedBackground = Brush.verticalGradient(
    listOf(Color(0xFF2C1919), Color(0xFF191F2C))
)

@Composable
internal fun CameraSimulatedSurface(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(SimulatedBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.NoPhotography,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(56.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.camera_sim_title),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.camera_sim_hint),
                color = Color.Gray,
                fontSize = 11.sp,
            )
        }
    }
}
