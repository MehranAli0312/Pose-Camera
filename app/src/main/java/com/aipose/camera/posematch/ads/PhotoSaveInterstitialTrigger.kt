package com.aipose.camera.posematch.ads

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.koinInject

@Stable
class PhotoSaveInterstitialTrigger internal constructor(
    private val ads: PhotoSaveInterstitialAds,
    private val activity: ComponentActivity?,
) {
    fun prepare(poseId: Int?) = ads.prepare(poseId)

    fun showThen(poseId: Int?, onContinue: () -> Unit) = ads.showThen(activity, poseId, onContinue)
}

@Composable
fun rememberPhotoSaveInterstitial(): PhotoSaveInterstitialTrigger {
    val ads: PhotoSaveInterstitialAds = koinInject()
    val context = LocalContext.current

    return remember(ads, context) {
        PhotoSaveInterstitialTrigger(ads, context.findComponentActivity())
    }
}
