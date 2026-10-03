package com.example.ads.compose.nativead

import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesView
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

internal val LocalNativeAd = staticCompositionLocalOf<NativeAd?> { null }

internal val LocalNativeAdView = staticCompositionLocalOf<NativeAdView?> { null }

internal val LocalMediaViewRegister = staticCompositionLocalOf<(MediaView?) -> Unit> { {} }

@Composable
internal fun NativeAdContainer(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val nativeAdViewRef = remember { mutableStateOf<NativeAdView?>(null) }
    val mediaViewRef = remember { mutableStateOf<MediaView?>(null) }

    AndroidView(
        factory = { context ->
            val composeView = ComposeView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            NativeAdView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                addView(composeView)
                nativeAdViewRef.value = this
            }
        },
        modifier = modifier,
        update = { view ->
            val composeView = view.getChildAt(0) as? ComposeView
            composeView?.setContent {
                val registerMediaView: (MediaView?) -> Unit =
                    remember { { mediaView -> mediaViewRef.value = mediaView } }
                CompositionLocalProvider(
                    LocalNativeAdView provides view,
                    LocalNativeAd provides nativeAd,
                    LocalMediaViewRegister provides registerMediaView,
                ) {
                    content()
                }
            }
        },
    )

    val currentNativeAd by rememberUpdatedState(nativeAd)
    val currentNativeAdView = nativeAdViewRef.value
    val currentMediaView = mediaViewRef.value

    DisposableEffect(currentNativeAd, currentNativeAdView, currentMediaView) {
        currentNativeAdView?.register(currentNativeAd, currentMediaView)
        onDispose { }
    }
}

private fun NativeAdView.register(nativeAd: NativeAd, mediaView: MediaView?) {
    if (mediaView != null) {
        mediaView.post { registerNativeAd(nativeAd, mediaView) }
    } else {
        registerNativeAd(nativeAd, null)
    }
}

@Composable
internal fun NativeAdHeadlineView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: return
    AndroidView(
        factory = { context -> ComposeView(context) },
        modifier = modifier,
        update = { view ->
            nativeAdView.headlineView = view
            view.setContent(content)
        },
    )
}

@Composable
internal fun NativeAdBodyView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: return
    AndroidView(
        factory = { context -> ComposeView(context) },
        modifier = modifier,
        update = { view ->
            nativeAdView.bodyView = view
            view.setContent(content)
        },
    )
}

@Composable
internal fun NativeAdCallToActionView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val nativeAdView = LocalNativeAdView.current ?: return
    AndroidView(
        factory = { context -> ComposeView(context) },
        modifier = modifier,
        update = { view ->
            nativeAdView.callToActionView = view
            view.setContent(content)
        },
    )
}

@Composable
internal fun NativeAdIconView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: return
    AndroidView(
        factory = { context -> ComposeView(context) },
        modifier = modifier,
        update = { view ->
            nativeAdView.iconView = view
            view.setContent(content)
        },
    )
}

@Composable
internal fun NativeAdAdvertiserView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: return
    AndroidView(
        factory = { context -> ComposeView(context) },
        modifier = modifier,
        update = { view ->
            nativeAdView.advertiserView = view
            view.setContent(content)
        },
    )
}

@Composable
internal fun NativeAdMediaView(
    modifier: Modifier = Modifier,
    scaleType: ImageView.ScaleType? = ImageView.ScaleType.CENTER_CROP,
) {
    val registerMediaView = LocalMediaViewRegister.current
    AndroidView(
        factory = { context -> MediaView(context) },
        modifier = modifier,
        update = { view ->
            registerMediaView(view)
            scaleType?.let { view.imageScaleType = it }
        },
    )
    DisposableEffect(Unit) { onDispose { registerMediaView(null) } }
}

@Composable
internal fun NativeAdChoicesView(modifier: Modifier = Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: return
    AndroidView(
        factory = { context ->
            AdChoicesView(context).apply {
                minimumWidth = 15
                minimumHeight = 15
            }
        },
        modifier = modifier,
        update = { view -> nativeAdView.adChoicesView = view },
    )
}
