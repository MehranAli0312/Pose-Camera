package com.example.ads.compose

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ads.AdPlacement
import com.example.ads.AdsManager
import com.example.ads.NativeAdDesign
import com.example.ads.compose.nativead.NativeAdTemplateRegistry
import com.example.ads.internal.AdRetryState
import com.example.ads.internal.NativeAdController
import com.example.ads.nativeDesignOr
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import org.koin.compose.koinInject

@Composable
fun AdsNative(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
    design: NativeAdDesign? = null,
    showPlaceholderWhileLoading: Boolean = true,
    topSpacing: Dp = 0.dp,
) {
    val adsManager: AdsManager = koinInject()
    val controller: NativeAdController = koinInject()
    val isPro by adsManager.isPro.collectAsState()
    val isReady by adsManager.isReady.collectAsState()

    if (isPro || LocalInspectionMode.current) return

    val resolvedDesign = adsManager.styleFor(placement).nativeDesignOr(design) ?: return
    var nativeAd by remember(placement, resolvedDesign) { mutableStateOf<NativeAd?>(null) }
    var loadFailed by remember(placement, resolvedDesign) { mutableStateOf(false) }
    val retryState = remember(placement, resolvedDesign) { AdRetryState() }

    AdRetryEffect(failed = loadFailed, retryState = retryState) { loadFailed = false }

    LaunchedEffect(placement, resolvedDesign, isReady, retryState.attempt) {
        if (!isReady || nativeAd != null || loadFailed) return@LaunchedEffect
        nativeAd = controller.load(placement)
        if (nativeAd == null) {
            retryState.markFailed()
            loadFailed = true
        }
    }

    nativeAd?.let { ad ->
        key(ad) {
            DisposableEffect(ad) { onDispose { controller.recycle(ad) } }
            NativeAdTemplateRegistry.Render(
                design = resolvedDesign,
                nativeAd = ad,
                colors = adsManager.nativeAdColors(),
                modifier = modifier
                    .padding(top = topSpacing)
                    .fillMaxWidth(),
            )
        }
    } ?: run {
        if (showPlaceholderWhileLoading && !loadFailed) {
            AdSlotPlaceholder(
                height = NativeAdTemplateRegistry.placeholderHeightDp(resolvedDesign).dp,
                modifier = modifier.padding(top = topSpacing),
            )
        } else Unit
    }
}
