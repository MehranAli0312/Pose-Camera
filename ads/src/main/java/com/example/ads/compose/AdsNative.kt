package com.example.ads.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.example.ads.AdPlacement
import com.example.ads.nativeDesignOr
import com.example.ads.AdsManager
import com.example.ads.NativeAdDesign
import com.example.ads.compose.nativead.NativeAdTemplateRegistry
import com.example.ads.internal.NativeAdController
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import org.koin.compose.koinInject

@Composable
fun AdsNative(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
    design: NativeAdDesign? = null,
    showPlaceholderWhileLoading: Boolean = true,
) {
    val adsManager: AdsManager = koinInject()
    val controller: NativeAdController = koinInject()

    val isPro by adsManager.isPro.collectAsState()
    val isReady by adsManager.isReady.collectAsState()
    val isPreview = LocalInspectionMode.current

    if (isPro || isPreview) return

    val resolvedDesign = adsManager.styleFor(placement).nativeDesignOr(design) ?: return

    var nativeAd by remember(placement, resolvedDesign) { mutableStateOf<NativeAd?>(null) }
    var loadRequested by remember(placement, resolvedDesign) { mutableStateOf(false) }
    var loadFailed by remember(placement, resolvedDesign) { mutableStateOf(false) }
    val currentAd by rememberUpdatedState(nativeAd)

    LaunchedEffect(placement, resolvedDesign, isReady) {
        if (!isReady) return@LaunchedEffect
        if (currentAd != null || loadRequested) return@LaunchedEffect
        loadRequested = true
        nativeAd = controller.load(placement)
        loadFailed = nativeAd == null
    }

    DisposableEffect(placement, resolvedDesign) {
        onDispose {
            currentAd?.destroy()
            nativeAd = null
        }
    }

    val ad = nativeAd
    if (ad != null) {
        NativeAdTemplateRegistry.Render(
            design = resolvedDesign,
            nativeAd = ad,
            colors = adsManager.nativeAdColors(),
            modifier = modifier.fillMaxWidth(),
        )
    } else if (showPlaceholderWhileLoading && !loadFailed) {
        AdSlotPlaceholder(
            height = NativeAdTemplateRegistry.placeholderHeightDp(resolvedDesign).dp,
            modifier = modifier,
        )
    }
}
