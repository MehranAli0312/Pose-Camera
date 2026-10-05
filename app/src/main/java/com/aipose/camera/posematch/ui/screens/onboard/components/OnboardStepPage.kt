package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardStep

@Composable
internal fun OnboardStepPage(
    step: OnboardStep,
    modifier: Modifier = Modifier,
    bottomReserved: Dp = 0.dp,
) {
    OnboardFrame(modifier = modifier, bottomReserved = bottomReserved) {
        Spacer(modifier = Modifier.height(OnboardChromeMetrics.TopHeight))
        OnboardContentPage(
            step = step,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(OnboardChromeMetrics.BottomHeight))
    }
}
