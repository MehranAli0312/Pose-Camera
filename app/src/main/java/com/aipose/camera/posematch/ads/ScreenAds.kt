package com.aipose.camera.posematch.ads

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdsManager
import com.example.ads.FullscreenAdStyle
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class ScreenAds internal constructor(
    private val adsManager: AdsManager,
    private val activity: ComponentActivity?,
) {

    fun styleFor(placement: AdPlacement): FullscreenAdStyle =
        adsManager.fullscreenStyleFor(placement)

    suspend fun awaitAdsAllowed(): Boolean =
        adsManager.proStatus.first { it.isResolved }.isEligibleForAds

    suspend fun awaitReady() {
        adsManager.isReady.first { it }
    }

    fun isAvailable(placement: AdPlacement): Boolean {
        val format = styleFor(placement).format ?: return false
        return adsManager.isAvailable(format, placement)
    }

    fun isAdsAllowedNow(): Boolean = adsManager.proStatus.value.isEligibleForAds

    fun fullscreen(
        placement: AdPlacement,
        continueWhenShown: Boolean = false,
        onResult: (AdResult) -> Unit = {},
        onDone: () -> Unit,
    ) {
        val activity = activity
        if (activity == null) {
            onDone()
            return
        }

        activity.lifecycleScope.launch {
            val done = AtomicBoolean(false)
            fun doneOnce() {
                if (!done.compareAndSet(false, true)) return
                activity.runOnUiThread(onDone)
            }

            val onShown = if (continueWhenShown) ({ doneOnce() }) else null
            val result = runCatching { adsManager.showFullscreen(activity, placement, onShown) }
                .getOrElse { error -> AdResult.Failed(error.message.orEmpty()) }
            onResult(result)
            doneOnce()
        }
    }
}

@Composable
fun rememberScreenAds(): ScreenAds {
    val adsManager: AdsManager = koinInject()
    val context = LocalContext.current

    return remember(adsManager, context) {
        ScreenAds(adsManager, context.findComponentActivity())
    }
}

internal tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
