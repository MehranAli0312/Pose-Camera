package com.example.ads.di

import com.example.ads.AdsManager
import com.example.ads.internal.AdGate
import com.example.ads.internal.AdsLog
import com.example.ads.internal.AdsRuntimeState
import com.example.ads.internal.BannerAdController
import com.example.ads.internal.ConsentManager
import com.example.ads.internal.DefaultAdsManager
import com.example.ads.internal.FullScreenAdController
import com.example.ads.internal.NativeAdController
import com.example.ads.internal.NetworkChecker
import com.example.ads.internal.PreparedSlotController
import com.example.ads.internal.SdkInitializer
import com.example.ads.lifecycle.AdsAppLifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val AdsScopeQualifier = named("ads-scope")

val adsModule = module {

    single(AdsScopeQualifier) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single { AdsLog(get()) }
    single { AdsRuntimeState() }
    single { NetworkChecker(androidContext()) }

    single { AdGate(get(), get(), get(), get(), get(), get()) }

    single { ConsentManager(androidContext(), get()) }
    single { SdkInitializer(androidContext(), get(), get(), get()) }

    single { BannerAdController(androidContext(), get(), get()) }
    single { NativeAdController(get(), get(), get()) }
    single { FullScreenAdController(get(), get(), get(), get()) }
    single { PreparedSlotController(get(AdsScopeQualifier), get(), get(), get()) }

    single<AdsManager> {
        DefaultAdsManager(
            scope = get(AdsScopeQualifier),
            state = get(),
            initializer = get(),
            consentManager = get(),
            fullScreenAds = get(),
            nativeAds = get(),
            preparedSlots = get(),
            slotStyles = get(),
            proStatusProvider = get(),
            log = get(),
        )
    }

    single {
        AdsAppLifecycleObserver(
            adsManager = get(),
            state = get(),
            scope = get(AdsScopeQualifier),
        )
    }
}
