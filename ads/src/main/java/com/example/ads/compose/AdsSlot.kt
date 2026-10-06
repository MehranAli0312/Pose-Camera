package com.example.ads.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.ads.AdPlacement
import com.example.ads.AdSlotStyle
import com.example.ads.AdsManager
import org.koin.compose.koinInject

@Composable
fun AdsSlot(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
    style: AdSlotStyle? = null,
    showPlaceholderWhileLoading: Boolean = true,
    topSpacing: Dp = AdSlotDefaults.topSpacing,
) {
    val adsManager: AdsManager = koinInject()
    val isPro by adsManager.isPro.collectAsState()
    if (isPro) return

    when (val resolved = style ?: adsManager.styleFor(placement)) {
        AdSlotStyle.Hidden -> Unit

        is AdSlotStyle.Banner -> AdsBanner(
            placement = placement,
            modifier = modifier,
            style = resolved.style,
            showPlaceholderWhileLoading = showPlaceholderWhileLoading,
            topSpacing = topSpacing,
        )

        is AdSlotStyle.Native -> AdsNative(
            placement = placement,
            modifier = modifier,
            design = resolved.design,
            showPlaceholderWhileLoading = showPlaceholderWhileLoading,
            topSpacing = topSpacing,
        )

        is AdSlotStyle.BannerWithNativeBackfill -> {
            var bannerFailed by remember(placement, resolved) { mutableStateOf(false) }
            if (bannerFailed) {
                AdsNative(
                    placement = placement,
                    modifier = modifier,
                    design = resolved.backfill,
                    showPlaceholderWhileLoading = showPlaceholderWhileLoading,
                    topSpacing = topSpacing,
                )
            } else {
                AdsBanner(
                    placement = placement,
                    modifier = modifier,
                    style = resolved.style,
                    showPlaceholderWhileLoading = showPlaceholderWhileLoading,
                    topSpacing = topSpacing,
                    onFailed = { bannerFailed = true },
                )
            }
        }
    }
}
