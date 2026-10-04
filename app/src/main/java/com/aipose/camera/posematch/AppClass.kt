package com.aipose.camera.posematch

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import com.aipose.camera.posematch.admob_ads.GoogleMobileAdsConsentManager
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class AppClass : Application() {
    val isMobileAdsInitializeCalled = AtomicBoolean(false)
    val isMobileAdsInitialized = AtomicBoolean(false)

    private val isMobileAdsInitializing = AtomicBoolean(false)
    private val mobileAdsInitCallbacks = CopyOnWriteArrayList<() -> Unit>()
    private val mainHandler = Handler(Looper.getMainLooper())

    // MobileAds.initialize must run off the main thread (Google: "failure to do so may cause an ANR").
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var nativeOnboard1: MutableLiveData<NativeAd?> = MutableLiveData()
    var nativeOnboard2: MutableLiveData<NativeAd?> = MutableLiveData()
    var nativeOnboard3: MutableLiveData<NativeAd?> = MutableLiveData()
    var nativeFullSrc: MutableLiveData<NativeAd?> = MutableLiveData()
    var languageNativeAd: MutableLiveData<NativeAd?> = MutableLiveData()
    var languageNativeAdDup: MutableLiveData<NativeAd?> = MutableLiveData()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Mobile Ads init took ~2.8s on the splash critical path. UMP caches the consent decision,
        // and canRequestAds reads that cached state synchronously — so on every launch AFTER the
        // one where consent was answered (and in regions where no form is required) we can start
        // the slow init here, in parallel with everything else, instead of making the splash wait
        // for the consent round trip first. This is Google's documented pattern.
        // First launch still initializes from SplashFragment after gatherConsent().
        if (GoogleMobileAdsConsentManager.getInstance(this).canRequestAds) {
            Log.d(TAG, "onCreate: consent already allows ads - starting SDK init early")
            initializeMobileAdsSdk()
        }
    }

    fun initializeMobileAdsSdk(onInitializeComplete: (() -> Unit)? = null) {
        Log.d(TAG, "initializeMobileAdsSdk() called")

        if (isMobileAdsInitialized.get()) {
            Log.d(TAG, "Mobile Ads already initialized")

            onInitializeComplete?.let { callback ->
                mainHandler.post {
                    callback.invoke()
                }
            }
            return
        }

        onInitializeComplete?.let { callback ->
            Log.d(TAG, "Adding callback to pending list")
            mobileAdsInitCallbacks.add(callback)
        }

        if (!isMobileAdsInitializing.compareAndSet(false, true)) {
            Log.d(TAG, "Mobile Ads initialization already running")
            return
        }

        isMobileAdsInitializeCalled.set(true)

        Log.d(TAG, "Starting Mobile Ads initialization")

        appScope.launch {
            MobileAds.initialize(
                this@AppClass,
                InitializationConfig.Builder(getString(R.string.admob_app_id))
                    .setNativeValidatorDisabled()
                    .build()
            ) {
                Log.d(TAG, "Mobile Ads initialization completed")

                mainHandler.post {
                    isMobileAdsInitialized.set(true)
                    isMobileAdsInitializing.set(false)

                    val pendingCallbacks = mobileAdsInitCallbacks.toList()
                    mobileAdsInitCallbacks.clear()

                    Log.d(TAG, "Pending callbacks count: ${pendingCallbacks.size}")

                    pendingCallbacks.forEachIndexed { index, callback ->
                        Log.d(TAG, "Executing callback #$index")
                        callback.invoke()
                    }

                    Log.d(TAG, "All callbacks executed")
                }
            }
        }
    }

    companion object {
        lateinit var instance: AppClass
            private set

        private const val TAG = "MobileAdsInit  okayIsInitialized"
    }
}