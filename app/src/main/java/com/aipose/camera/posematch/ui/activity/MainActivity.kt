package com.aipose.camera.posematch.ui.activity

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.example.ads.AdsManager
import com.example.ads.lifecycle.AdsAppLifecycleObserver
import com.example.common.Constants
import com.example.common.update.InAppUpdateManager
import com.aipose.camera.posematch.data.local.NetworkConnectivityChecker
import com.aipose.camera.posematch.ui.common.AppSystemBars
import com.aipose.camera.posematch.ui.common.LocalAppSystemBars
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote
import com.aipose.camera.posematch.ui.screens.MainScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val systemBars by lazy { AppSystemBars(window) }
    private val adsManager: AdsManager by inject()
    private val adsLifecycleObserver: AdsAppLifecycleObserver by inject()
    private val appFirebaseRemote: AppFirebaseRemote by inject()
    private val networkConnectivityChecker: NetworkConnectivityChecker by inject()
    private val inAppUpdateManager = InAppUpdateManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) Constants.splashEnd = false
        enableEdgeToEdge()
        systemBars.applyImmersiveBehavior()
        startAdsForFirstActivity()
        observeAppUpdateConfig()

        goToNext()
    }

    private fun observeAppUpdateConfig() {
        lifecycleScope.launch {
            Constants.splashEnded.first { ended -> ended }
            appFirebaseRemote.appUpdateConfig
                .filter { config -> config.versionCode > 0 }
                .distinctUntilChanged()
                .collect { config ->
                    withResumed {
                        inAppUpdateManager.checkForUpdate(
                            requiredVersionCode = config.versionCode,
                            forceUpdate = config.forceUpdate,
                        )
                    }
                }
        }
    }

    private fun startAdsForFirstActivity() {
        lifecycleScope.launch {
            runCatching { adsManager.requestConsent(this@MainActivity) }
        }

        lifecycleScope.launch {
            runCatching {
                networkConnectivityChecker.observeActiveInternet()
                    .drop(1)
                    .filter { isOnline -> isOnline }
                    .collect { appFirebaseRemote.refreshIfFetchFailed() }
            }
        }
    }

    @Deprecated("Routed through the Activity Result API")
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        adsLifecycleObserver.onExternalScreenLaunched()
        super.startActivityForResult(intent, requestCode, options)
    }

    @Deprecated("Routed through the Activity Result API")
    override fun startIntentSenderForResult(
        intent: IntentSender,
        requestCode: Int,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int,
        options: Bundle?,
    ) {
        adsLifecycleObserver.onExternalScreenLaunched()
        super.startIntentSenderForResult(
            intent,
            requestCode,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            options,
        )
    }

    override fun startActivities(intents: Array<out Intent>?, options: Bundle?) {
        adsLifecycleObserver.onExternalScreenLaunched()
        super.startActivities(intents, options)
    }

    private fun goToNext() {
        setContent {
            CompositionLocalProvider(LocalAppSystemBars provides systemBars) {
                MainScreen()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) systemBars.applyImmersiveBehavior()
    }
}
