package com.aipose.camera.posematch.ui.activity

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.example.ads.AdsManager
import com.example.common.Constants
import com.example.common.update.InAppUpdateManager
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.data.local.NetworkConnectivityChecker
import com.aipose.camera.posematch.ui.common.applyAppImmersiveSystemBars
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote
import com.aipose.camera.posematch.ui.screens.MainScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val adsManager: AdsManager by inject()
    private val appFirebaseRemote: AppFirebaseRemote by inject()
    private val networkConnectivityChecker: NetworkConnectivityChecker by inject()
    private val inAppUpdateManager = InAppUpdateManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val darkBar = ContextCompat.getColor(this, R.color.background_dark)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(darkBar),
            navigationBarStyle = SystemBarStyle.dark(darkBar),
        )
        window.applyAppImmersiveSystemBars()
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.applyAppImmersiveSystemBars()
        }
    }

    private fun goToNext() {
        setContent {
            MainScreen()
        }
    }
}
