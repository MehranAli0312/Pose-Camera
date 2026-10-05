package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.poseScreenTitleStyle

@Composable
internal fun SettingsHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.item_settings),
        style = poseScreenTitleStyle(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth(),
    )
}
