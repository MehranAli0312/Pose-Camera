package com.example.ads.internal

import android.content.Context
import com.example.ads.AdsConfig
import com.example.ads.R
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SdkInitializer(
    private val context: Context,
    private val config: AdsConfig,
    private val state: AdsRuntimeState,
    private val log: AdsLog,
) {

    private val completion = CompletableDeferred<Unit>()

    @Volatile
    private var started = false

    suspend fun initialize() {
        val shouldStart = synchronized(this) {
            if (started) false else { started = true; true }
        }

        if (shouldStart) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val requestConfiguration = RequestConfiguration.Builder()
                        .setTestDeviceIds(config.testDeviceIds)
                        .build()

                    MobileAds.initialize(
                        context,
                        InitializationConfig.Builder(context.getString(R.string.admob_app_id))
                            .setRequestConfiguration(requestConfiguration)
                            .build(),
                    ) {
                        log.d("GMA Next-Gen SDK initialised")
                        state.setSdkInitialized(true)
                        completion.complete(Unit)
                    }
                }.onFailure { error ->
                    log.w("SDK initialisation failed", error)
                    completion.complete(Unit)
                }
            }
        }

        completion.await()
    }
}
