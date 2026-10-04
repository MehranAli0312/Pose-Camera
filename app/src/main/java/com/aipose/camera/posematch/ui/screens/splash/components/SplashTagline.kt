package com.aipose.camera.posematch.ui.screens.splash.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PoseTextSubtle
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TaglineSize = 10.sp
private val TaglineTracking = 1.sp

@Composable
internal fun SplashTagline(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.splash_tagline),
        style = poseTextStyle(TaglineSize, FontWeight.Bold, PoseTextSubtle)
            .copy(letterSpacing = TaglineTracking),
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
