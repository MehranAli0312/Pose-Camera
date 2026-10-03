package com.example.ads.compose.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ads.NativeAdColors
import com.example.ads.NativeAdDesign
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd

internal object NativeAdTemplateRegistry {

    @Composable
    fun Render(
        design: NativeAdDesign,
        nativeAd: NativeAd,
        colors: NativeAdColors,
        modifier: Modifier = Modifier,
    ) {
        NativeAdContainer(nativeAd = nativeAd, modifier = modifier) {
            when (design) {
                NativeAdDesign.SMALL -> SmallNativeTemplate(nativeAd, colors)
                NativeAdDesign.MEDIUM -> MediumNativeTemplate(nativeAd, colors)
                NativeAdDesign.LARGE -> LargeNativeTemplate(nativeAd, colors)
            }
        }
    }

    fun placeholderHeightDp(design: NativeAdDesign): Int = when (design) {
        NativeAdDesign.SMALL -> 72
        NativeAdDesign.MEDIUM -> 260
        NativeAdDesign.LARGE -> 320
    }
}
