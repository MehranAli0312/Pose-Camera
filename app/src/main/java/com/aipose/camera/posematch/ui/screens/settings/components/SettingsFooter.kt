package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PoseTextDim
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val FooterSize = 9.5.sp

@Composable
internal fun SettingsFooter(versionName: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.settings_footer, versionName),
        style = poseTextStyle(FooterSize, FontWeight.Bold, PoseTextDim),
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}
