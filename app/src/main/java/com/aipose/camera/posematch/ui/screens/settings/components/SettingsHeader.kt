package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TitleSize = 26.sp

@Composable
internal fun SettingsHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.item_settings),
        style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth(),
    )
}
