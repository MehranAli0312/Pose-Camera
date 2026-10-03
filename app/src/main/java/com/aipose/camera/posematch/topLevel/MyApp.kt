package com.aipose.camera.posematch.topLevel

import android.app.Application
import com.example.ads.AdsManager
import com.example.ads.di.adsModule
import com.pdfutility.billing.BillingManager
import com.pdfutility.billing.presentation.states.PurchaseResult
import com.aipose.camera.posematch.ads.ProStatusForegroundObserver
import com.aipose.camera.posematch.ads.ProStatusRefresher
import com.aipose.camera.posematch.ads.ProStatusStore
import com.aipose.camera.posematch.ads.appAdsModule
import com.aipose.camera.posematch.ads.splashAdsLoads
import com.aipose.camera.posematch.di.localModule
import com.aipose.camera.posematch.di.repositoryModule
import com.aipose.camera.posematch.di.useCaseModule
import com.aipose.camera.posematch.di.viewModelModule
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.context.startKoin

class MyApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        try {
            val koin = startKoin {
                androidContext(this@MyApp)
                modules(
                    localModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule,
                    adsModule,
                    appAdsModule,
                )
            }.koin

            startAds(koin)
        } catch (_: Exception) {
        }
    }

    private fun startAds(koin: Koin) {
        runCatching {
            val adsManager = koin.get<AdsManager>()

            adsManager.initialize()

            splashAdsLoads(adsManager)

            koin.get<AppFirebaseRemote>().create()

            resolveProStatus(koin)
        }
    }

    private fun resolveProStatus(koin: Koin) {
        val proStatusStore = koin.get<ProStatusStore>()
        val billingManager = koin.get<BillingManager>()
        val proStatusRefresher = koin.get<ProStatusRefresher>()
        applicationScope.launch {
            runCatching {
                proStatusRefresher.applySavedStatus()
                proStatusRefresher.connectAndRefresh()
            }.onFailure { proStatusStore.resolveUnknownAsFree() }
        }
        ProStatusForegroundObserver(proStatusRefresher, applicationScope).register()
        applicationScope.launch {
            billingManager.purchaseResults.collect { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        proStatusStore.resolve(isPro = true)
                        proStatusRefresher.refresh()
                    }

                    PurchaseResult.AlreadyOwned -> proStatusRefresher.refresh()
                    else -> Unit
                }
            }
        }
    }
}
