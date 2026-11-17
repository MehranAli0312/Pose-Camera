package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.ads.AdPlacement

@Composable
fun AdSlotReleaseEffect(placement: AdPlacement) {
    val ads = rememberScreenAds()
    val context = LocalContext.current
    val activity = remember(context) { context.findComponentActivity() }
    DisposableEffect(ads, placement, activity) {
        onDispose {
            if (activity == null || activity.isFinishing || !activity.isChangingConfigurations) {
                ads.releaseSlot(placement)
            }
        }
    }
}
