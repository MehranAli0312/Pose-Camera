package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.OnboardingFullScreenNative
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsPreparedSlot

private val AdTopGap = 8.dp
private val AdBottomGap = 16.dp

@Composable
internal fun OnboardNativeAdPage(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    bottomReserved: Dp = 0.dp,
) {
    OnboardFrame(modifier = modifier, bottomReserved = bottomReserved) {
        Spacer(modifier = Modifier.height(OnboardChromeMetrics.TopBarTop))
        OnboardTopBar(
            labelRes = R.string.onboard_next,
            onClick = onContinue,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = AdTopGap, bottom = AdBottomGap),
        ) {
            AdsPreparedSlot(
                placement = AdPlacement.OnboardingFullScreenNative,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
