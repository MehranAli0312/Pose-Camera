package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.ProAppBarAction
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TitleSize = 26.sp

@Composable
internal fun SettingsHeader(onOpenPro: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.item_settings),
            style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
            modifier = Modifier.weight(1f),
        )
        ProAppBarAction(onClick = onOpenPro)
    }
}
