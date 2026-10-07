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

private const val PRELOAD_BUFFER_SIZE = 1
private const val INTERSTITIAL_FREQUENCY = 1

val appAdsModule = module {

    single { ProStatusStore() }
    single {
        ProStatusRefresher(
            billingManager = get(),
            proStatusStore = get(),
            appDataStore = get(),
        )
    }
    single { AdsRemoteConfigStore(get()) }
    single { AppOpenLoaderState() }
    single { AppOpenRouteGate() }
    single { MissedSplashAd() }
    single { InnerInterstitialAds(adsManager = get(), store = get()) }
    single { RewardedUnlockSession() }
    single {
        PhotoSaveInterstitialAds(
            adsManager = get(),
            innerInterstitialAds = get(),
            rewardedUnlockSession = get(),
        )
    }

    single { AppFirebaseRemote(store = get(), adsManager = get()) }

    single {
        AdsConfig(
            isDebugBuild = BuildConfig.DEBUG,
            preloadBufferSize = PRELOAD_BUFFER_SIZE,
            interstitialFrequency = INTERSTITIAL_FREQUENCY,
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
            AdPlacement.PhotoSaveInterstitial ->
                store.current.photoSaveInterstitial.toInterstitialStyle()
            AdPlacement.PremiumRewarded -> FullscreenAdStyle.Rewarded
            else -> FullscreenAdStyle.Hidden
        }

    override fun nativeAdColors(): NativeAdColors = store.current.nativeAdColors.toNativeAdColors()

    private fun Boolean.toInterstitialStyle(): FullscreenAdStyle =
        if (this) FullscreenAdStyle.Interstitial else FullscreenAdStyle.Hidden

    private fun Boolean.toAppOpenStyle(): FullscreenAdStyle =
        if (this) FullscreenAdStyle.AppOpen else FullscreenAdStyle.Hidden
}
