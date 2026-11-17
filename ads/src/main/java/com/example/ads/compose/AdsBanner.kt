package com.example.ads.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ads.AdPlacement
import com.example.ads.AdsManager
import com.example.ads.BannerStyle
import com.example.ads.bannerStyleOr
import com.example.ads.internal.AdRetryState
import com.example.ads.internal.BannerAdController
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import org.koin.compose.koinInject

@Composable
fun AdsBanner(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
    style: BannerStyle? = null,
    showPlaceholderWhileLoading: Boolean = true,
    containerColor: Color = Color.Unspecified,
    topSpacing: Dp = 0.dp,
    onFailed: () -> Unit = {},
) {
    val adsManager: AdsManager = koinInject()
    val controller: BannerAdController = koinInject()
    val isPro by adsManager.isPro.collectAsState()
    val isReady by adsManager.isReady.collectAsState()

    if (isPro || LocalInspectionMode.current) return

    val resolvedStyle = adsManager.styleFor(placement).bannerStyleOr(style) ?: return
    val slotColor = containerColor.takeOrElse { AdSlotDefaults.colors.container }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()
        val slotHeight = remember(resolvedStyle, widthDp) {
            controller.adSizeFor(resolvedStyle, widthDp).height.dp
        }
        var bannerAd by remember(placement, resolvedStyle, widthDp) {
            mutableStateOf<BannerAd?>(null)
        }
        var failed by remember(placement, resolvedStyle, widthDp) { mutableStateOf(false) }
        val retryState = remember(placement, resolvedStyle, widthDp) { AdRetryState() }

        AdRetryEffect(failed = failed, retryState = retryState) { failed = false }

        LaunchedEffect(placement, resolvedStyle, widthDp, isReady, retryState.attempt) {
            if (!isReady || widthDp <= 0 || bannerAd != null || failed) return@LaunchedEffect
            bannerAd = controller.load(placement, resolvedStyle, widthDp)
            if (bannerAd == null) {
                retryState.markFailed()
                failed = true
                onFailed()
            }
        }

        bannerAd?.let { ad ->
            key(ad) {
                DisposableEffect(ad) { onDispose { controller.recycle(ad) } }
                Box(
                    modifier = Modifier
                        .padding(top = topSpacing)
                        .fillMaxWidth()
                        .height(slotHeight)
                        .background(slotColor),
                    contentAlignment = Alignment.Center,
                ) {
                    BannerAdView(ad = ad, modifier = Modifier.fillMaxWidth())
                }
            }
        } ?: run {
            if (showPlaceholderWhileLoading && !failed) {
                AdSlotPlaceholder(
                    height = slotHeight,
                    modifier = Modifier.padding(top = topSpacing),
                )
            } else Unit
        }
    }
}
