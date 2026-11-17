package com.aipose.camera.posematch.ui.common

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
fun AppHorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = LocalAppPalette.current.cardBorder,
    )
}
