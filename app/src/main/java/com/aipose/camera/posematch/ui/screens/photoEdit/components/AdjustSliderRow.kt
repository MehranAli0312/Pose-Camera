package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private const val ADJUST_MINIMUM = -1f
private const val ADJUST_MAXIMUM = 1f
private const val PERCENT_FACTOR = 100

@Composable
internal fun AdjustSliderRow(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = ADJUST_MINIMUM..ADJUST_MAXIMUM,
            colors = SliderDefaults.colors(
                thumbColor = palette.accent,
                activeTrackColor = palette.accent,
                inactiveTrackColor = palette.card,
            ),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = (value * PERCENT_FACTOR).toInt().toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
        )
    }
}
