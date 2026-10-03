package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private const val COUNTDOWN_ALPHA = 0.85f

@Composable
internal fun CaptureCountdown(
    seconds: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = seconds.toString(),
            fontSize = 120.sp,
            fontWeight = FontWeight.Black,
            color = LocalAppPalette.current.accent,
            modifier = Modifier.alpha(COUNTDOWN_ALPHA),
        )
    }
}
