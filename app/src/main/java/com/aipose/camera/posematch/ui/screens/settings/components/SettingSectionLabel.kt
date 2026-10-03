package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun SettingSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            color = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier,
    )
}
