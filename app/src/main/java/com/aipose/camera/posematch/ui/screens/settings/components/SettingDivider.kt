package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.AppTheme

@Composable
internal fun SettingDivider() {
    HorizontalDivider(
        color = AppTheme.extendedColors.cardBorder,
        thickness = 1.dp,
    )
}
