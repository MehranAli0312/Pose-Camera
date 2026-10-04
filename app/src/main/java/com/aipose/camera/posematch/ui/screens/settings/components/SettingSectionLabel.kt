package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.theme.PoseTextFaint
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val LabelSize = 9.sp
private val LabelStartPadding = 4.dp

@Composable
internal fun SettingSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = poseTextStyle(LabelSize, FontWeight.Bold, PoseTextFaint),
        modifier = modifier.padding(start = LabelStartPadding),
    )
}
