package com.aipose.camera.posematch.ads

import com.example.ads.AdPlacement
import com.example.ads.AdSlotStyle
import com.example.ads.AdSlotStyleProvider
import com.example.ads.AdUnitProvider
import com.example.ads.AdsConfig
import com.example.ads.FullscreenAdStyle
import com.example.ads.NativeAdColors
import com.example.ads.ProStatusProvider
import com.aipose.camera.posematch.BuildConfig
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote
import org.koin.dsl.module

val appAdsModule = module {

    single { ProStatusStore() }
    single { MissedSplashAd() }
    single { ProStatusRefresher(billingManager = get(), proStatusStore = get(), appDataStore = get()) }
    single { AdsRemoteConfigStore(get()) }

    single { AppFirebaseRemote(store = get(), adsManager = get()) }

    single {
        AdsConfig(
            isDebugBuild = BuildConfig.DEBUG,
            preloadBufferSize = 2,
            interstitialFrequency = 1,
        )
    }

    single<ProStatusProvider> {
        val store: ProStatusStore = get()
        ProStatusProvider { store.status }
    }

    single<AdSlotStyleProvider> { AppAdSlotStyleProvider(get()) }

    single<AdUnitProvider> { RemoteAdUnitProvider(get()) }
}

internal class AppAdSlotStyleProvider(
    private val store: AdsRemoteConfigStore,
) : AdSlotStyleProvider {

    override fun styleFor(placement: AdPlacement): AdSlotStyle =
        store.current.slotStyleFor(placement)

    override fun fullscreenStyleFor(placement: AdPlacement): FullscreenAdStyle =
        when (placement) {
            AdPlacement.SplashFullscreen -> store.current.splashInterstitial.toInterstitialStyle()
            AdPlacement.AppOpenResume -> store.current.appOpenOnResume.toAppOpenStyle()
            AdPlacement.InnerInterstitial -> store.current.innerInterstitial.toInterstitialStyle()
            else -> FullscreenAdStyle.Hidden
        }

    override fun nativeAdColors(): NativeAdColors = store.current.nativeAdColors.toNativeAdColors()

    private fun Boolean.toInterstitialStyle(): FullscreenAdStyle =
        if (this) FullscreenAdStyle.Interstitial else FullscreenAdStyle.Hidden

    private fun Boolean.toAppOpenStyle(): FullscreenAdStyle =
        if (this) FullscreenAdStyle.AppOpen else FullscreenAdStyle.Hidden
}
