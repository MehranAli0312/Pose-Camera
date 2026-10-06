package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.aipose.camera.posematch.ads.HomeScreenBottom
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsSlot

@Composable
internal fun DashboardBottomAd(
    space: DashboardBottomAdSpace,
    modifier: Modifier = Modifier,
) {
    if (!space.isLive) {
        Spacer(modifier = modifier.fillMaxWidth().height(space.height))
        return
    }
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size -> space.onHeightChanged(with(density) { size.height.toDp() }) },
    ) {
        AdsSlot(placement = AdPlacement.HomeScreenBottom)
    }
}
