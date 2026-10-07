package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseTopBar

@Composable
internal fun SettingsHeader(modifier: Modifier = Modifier) {
    PoseTopBar(
        title = stringResource(R.string.item_settings),
        modifier = modifier,
    )
}
