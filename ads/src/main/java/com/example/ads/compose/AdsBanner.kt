package com.example.ads.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdPlacement
import com.example.ads.bannerStyleOr
import com.example.ads.AdsManager
import com.example.ads.BannerStyle
import com.example.ads.internal.BannerAdController
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import org.koin.compose.koinInject

@Composable
fun AdsBanner(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
    style: BannerStyle? = null,
    showPlaceholderWhileLoading: Boolean = true,
    onFailed: () -> Unit = {},
) {
    val adsManager: AdsManager = koinInject()
    val controller: BannerAdController = koinInject()

    val isPro by adsManager.isPro.collectAsState()
    val isReady by adsManager.isReady.collectAsState()

    if (isPro || LocalInspectionMode.current) return

    val resolvedStyle = adsManager.styleFor(placement).bannerStyleOr(style) ?: return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()

        var request by remember(placement, resolvedStyle) { mutableStateOf<BannerAdRequest?>(null) }
        var failed by remember(placement, resolvedStyle) { mutableStateOf(false) }
        var loaded by remember(placement, resolvedStyle) { mutableStateOf(false) }

        LaunchedEffect(placement, resolvedStyle, widthDp, isReady) {
            if (!isReady || widthDp <= 0) return@LaunchedEffect
            if (request == null) {
                request = controller.buildRequest(placement, resolvedStyle, widthDp)
            }
        }

        val currentRequest = request
        val slotHeight = remember(resolvedStyle, widthDp) {
            controller.adSizeFor(resolvedStyle, widthDp).height.dp
        }

        when {
            failed -> Unit

            currentRequest != null -> Box(
                modifier = Modifier.fillMaxWidth().height(slotHeight),
                contentAlignment = Alignment.Center,
            ) {
                key(currentRequest) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            AdView(context).apply {
                                loadAd(
                                    currentRequest,
                                    object : AdLoadCallback<BannerAd> {
                                        override fun onAdLoaded(ad: BannerAd) {
                                            controller.onLoaded(placement)
                                            loaded = true
                                        }

                                        override fun onAdFailedToLoad(adError: LoadAdError) {
                                            controller.onFailed(placement, adError.message)
                                            failed = true
                                            onFailed()
                                        }
                                    },
                                )
                            }
                        },
                        onRelease = { adView -> adView.destroy() },
                    )
                }
                if (!loaded && showPlaceholderWhileLoading) {
                    AdSlotPlaceholder(height = slotHeight)
                }
            }

            showPlaceholderWhileLoading -> AdSlotPlaceholder(height = slotHeight)
        }
    }
}
