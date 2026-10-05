package com.example.ads.compose

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.example.ads.AdPlacement
import com.example.ads.nativeDesignOr
import com.example.ads.AdsManager
import com.example.ads.NativeAdDesign
import com.example.ads.compose.nativead.NativeAdTemplateRegistry
import com.example.ads.internal.AdRetryState
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
    var loadFailed by remember(placement, resolvedDesign) { mutableStateOf(false) }
    val retryState = remember(placement, resolvedDesign) { AdRetryState() }

    AdRetryEffect(failed = loadFailed, retryState = retryState) {
        loadFailed = false
    }

    LaunchedEffect(placement, resolvedDesign, isReady, retryState.attempt) {
        if (!isReady) return@LaunchedEffect
        if (nativeAd != null || loadFailed) return@LaunchedEffect
        val loadedAd = controller.load(placement)
        if (loadedAd == null) {
            retryState.markFailed()
            loadFailed = true
        } else {
            nativeAd = loadedAd
        }
    }

    val ad = nativeAd
    if (ad != null) {
        key(ad) {
            DisposableEffect(ad) { onDispose { ad.destroy() } }
            NativeAdTemplateRegistry.Render(
                design = resolvedDesign,
                nativeAd = ad,
                colors = adsManager.nativeAdColors(),
                modifier = modifier.fillMaxWidth(),
            )
        }
    } else if (showPlaceholderWhileLoading && !loadFailed) {
        AdSlotPlaceholder(
            height = NativeAdTemplateRegistry.placeholderHeightDp(resolvedDesign).dp,
            modifier = modifier,
        )
    }
}
