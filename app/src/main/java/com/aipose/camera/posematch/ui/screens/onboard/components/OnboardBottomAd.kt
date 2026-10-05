package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.aipose.camera.posematch.ads.OnboardScreenBottom
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsSlot

@Composable
internal fun OnboardBottomAd(
    onHeightChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size -> onHeightChanged(with(density) { size.height.toDp() }) },
    ) {
        AdsSlot(
            placement = AdPlacement.OnboardScreenBottom,
            modifier = Modifier.safeBottomSystemBarsPadding(),
        )
    }
}
