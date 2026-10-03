package com.example.ads.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdPlacement
import com.example.ads.AdsManager
import com.example.ads.compose.nativead.NativeAdTemplateRegistry
import com.example.ads.internal.PreparedSlotAd
import com.example.ads.internal.PreparedSlotController
import org.koin.compose.koinInject

@Composable
fun AdsPreparedSlot(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
) {
    val adsManager: AdsManager = koinInject()
    val controller: PreparedSlotController = koinInject()

    val isPro by adsManager.isPro.collectAsState()
    if (isPro || LocalInspectionMode.current) return

    val slotState = remember(controller, placement) { controller.state(placement) }
    val prepared by slotState.collectAsState()

    LaunchedEffect(adsManager, placement) { adsManager.prepareSlot(placement) }

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    DisposableEffect(controller, placement, activity) {
        onDispose {
            if (activity == null || activity.isFinishing) controller.abandon(placement)
        }
    }

    when (val ad = prepared) {
        PreparedSlotAd.Empty -> Unit

        is PreparedSlotAd.Loading -> AdSlotPlaceholder(height = ad.heightDp.dp, modifier = modifier)

        is PreparedSlotAd.Banner -> {
            ConsumeWhenHidden(controller, placement, ad)
            PreparedBanner(ad, modifier)
        }

        is PreparedSlotAd.Native -> {
            ConsumeWhenHidden(controller, placement, ad)
            NativeAdTemplateRegistry.Render(
                design = ad.design,
                nativeAd = ad.ad,
                colors = adsManager.nativeAdColors(),
                modifier = modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConsumeWhenHidden(
    controller: PreparedSlotController,
    placement: AdPlacement,
    shown: PreparedSlotAd,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    DisposableEffect(controller, placement, shown, activity) {
        onDispose {
            val hostFinishing = activity == null || activity.isFinishing
            controller.consume(placement, shown, prepareNext = !hostFinishing)
        }
    }
}

@Composable
private fun PreparedBanner(banner: PreparedSlotAd.Banner, modifier: Modifier) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() } ?: return
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.size(banner.widthDp.dp, banner.heightDp.dp),
            factory = {
                val view = banner.ad.getView(activity)
                (view.parent as? ViewGroup)?.removeView(view)
                view
            },
            onRelease = { view -> (view.parent as? ViewGroup)?.removeView(view) },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
