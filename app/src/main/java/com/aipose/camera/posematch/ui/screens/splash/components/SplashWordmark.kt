package com.aipose.camera.posematch.ui.screens.splash.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BrandSize = 34.sp

@Composable
internal fun SplashWordmark(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.splash_brand_pose),
        style = poseTextStyle(BrandSize, FontWeight.Bold, Color.White),
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
