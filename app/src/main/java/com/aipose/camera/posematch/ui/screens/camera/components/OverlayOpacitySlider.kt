package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private const val PERCENT_FACTOR = 100

@Composable
internal fun OverlayOpacitySlider(
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.camera_opacity),
                color = Color.Gray,
                fontSize = 11.sp,
            )
            Text(
                text = stringResource(
                    R.string.score_percent,
                    (opacity * PERCENT_FACTOR).toInt()
                ),
                color = Color.White,
                fontSize = 11.sp,
            )
        }
        Slider(
            value = opacity,
            onValueChange = onOpacityChange,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = Color.DarkGray,
            ),
            modifier = Modifier.height(18.dp),
        )
    }
}
