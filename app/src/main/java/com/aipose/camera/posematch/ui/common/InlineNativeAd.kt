package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsPreparedSlot

@Composable
fun InlineNativeAd(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
) {
    AdsPreparedSlot(
        placement = placement,
        modifier = modifier.fillMaxWidth(),
        keepUntilReleased = true,
    )
}
