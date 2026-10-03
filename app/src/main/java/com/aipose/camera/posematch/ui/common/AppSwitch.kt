package com.aipose.camera.posematch.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.ui.theme.CardBorder
import com.aipose.camera.posematch.ui.theme.InactiveInput

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        modifier = modifier.scale(0.6f),
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = InactiveInput,
            uncheckedTrackColor = CardBorder,
            uncheckedBorderColor = InactiveInput,
        ),
    )
}
