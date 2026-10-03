package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import com.aipose.camera.posematch.ui.theme.commonShape

@Composable
internal fun SettingGroupCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(commonShape())
            .background(MaterialTheme.colorScheme.secondaryContainer),
    ) {
        content()
    }
}
