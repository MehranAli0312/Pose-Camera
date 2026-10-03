package com.aipose.camera.posematch.ui.screens.splash.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R

@Composable
internal fun SplashTagline(
    mutedColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.splash_tagline),
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium,
            color = mutedColor,
            letterSpacing = 1.sp,
        ),
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
