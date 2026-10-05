package com.aipose.camera.posematch.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.example.ads.AdPlacement
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

private const val NO_AD_PAGE = -1

@Stable
class OnboardingNativeAd internal constructor(
    private val ads: ScreenAds,
    private val store: AdsRemoteConfigStore,
    initialPageIndex: Int?,
) {

    var pageIndex: Int? by mutableStateOf(initialPageIndex)
        private set

    suspend fun insertWhenReady(isStillAhead: (pageIndex: Int) -> Boolean) {
        if (pageIndex != null) return
        val candidate = store.current.onboardingNativeAd.afterStepCount ?: return
        if (!ads.awaitAdsAllowed()) return
        ads.prepareSlot(AdPlacement.OnboardingFullScreenNative)
        combine(
            ads.isSlotPrepared(AdPlacement.OnboardingFullScreenNative),
            snapshotFlow { isStillAhead(candidate) },
        ) { prepared, ahead -> prepared && ahead }.first { it }
        pageIndex = candidate
    }

    fun release() = ads.releaseSlot(AdPlacement.OnboardingFullScreenNative)
}

@Composable
fun rememberOnboardingNativeAd(): OnboardingNativeAd {
    val ads = rememberScreenAds()
    val store: AdsRemoteConfigStore = koinInject()

    val nativeAd = rememberSaveable(
        ads,
        store,
        saver = Saver<OnboardingNativeAd, Int>(
            save = { it.pageIndex ?: NO_AD_PAGE },
            restore = { saved -> OnboardingNativeAd(ads, store, saved.takeIf { it != NO_AD_PAGE }) },
        ),
    ) { OnboardingNativeAd(ads, store, initialPageIndex = null) }

    DisposableEffect(nativeAd) { onDispose { nativeAd.release() } }

    return nativeAd
}
