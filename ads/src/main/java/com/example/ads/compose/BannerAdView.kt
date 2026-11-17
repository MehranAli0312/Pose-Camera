package com.example.ads.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd

@Composable
internal fun BannerAdView(
    ad: BannerAd,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() } ?: return
    AndroidView(
        modifier = modifier,
        factory = {
            ad.getView(activity).also { view ->
                (view.parent as? ViewGroup)?.removeView(view)
            }
        },
        onRelease = { view -> (view.parent as? ViewGroup)?.removeView(view) },
    )
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
