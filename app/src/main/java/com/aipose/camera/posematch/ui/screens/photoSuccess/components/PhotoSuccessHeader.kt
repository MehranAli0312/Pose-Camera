package com.aipose.camera.posematch.ui.screens.photoSuccess.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.poseTextStyle

@Composable
internal fun PhotoSuccessHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.saved_title),
        style = poseTextStyle(25.sp, FontWeight.Bold, Color.White),
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 20.dp),
    )
}
