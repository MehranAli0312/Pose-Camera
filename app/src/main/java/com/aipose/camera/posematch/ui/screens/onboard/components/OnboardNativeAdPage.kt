package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ads.OnboardingFullScreenNative
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsPreparedSlot

@Composable
internal fun OnboardNativeAdPage(
    modifier: Modifier = Modifier,
    bottomReserved: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = bottomReserved)
            .safeTopSystemBarsPadding()
            .safeBottomSystemBarsPadding(),
    ) {
        AdsPreparedSlot(
            placement = AdPlacement.OnboardingFullScreenNative,
            modifier = Modifier.fillMaxSize(),
            keepUntilReleased = true,
        )
    }
}
