package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TopScrimHeight = 300.dp
private val BottomScrimHeight = 304.dp

@Composable
internal fun BoxScope.CameraScrims() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(TopScrimHeight)
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.7f),
                    0.32f to Color.Black.copy(alpha = 0.42f),
                    0.68f to Color.Black.copy(alpha = 0.14f),
                    1f to Color.Transparent,
                )
            )
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(BottomScrimHeight)
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.35f to Color.Black.copy(alpha = 0.45f),
                    0.7f to Color.Black.copy(alpha = 0.75f),
                    1f to Color.Black.copy(alpha = 0.94f),
                )
            )
    )
}
