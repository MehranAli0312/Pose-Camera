package com.aipose.camera.posematch.ui.firebaseRemote

import com.aipose.camera.posematch.BuildConfig
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.SplashAdLog
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AppFirebaseRemote(
    private val store: AdsRemoteConfigStore,
) {

    private val remoteConfig = Firebase.remoteConfig
    private val started = AtomicBoolean(false)
    private val liveUpdatesStarted = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _resolution = MutableStateFlow(RemoteConfigResolution.Pending)
    val resolution: StateFlow<RemoteConfigResolution> = _resolution.asStateFlow()

    private val _appUpdateConfig = MutableStateFlow(AppUpdateRemoteConfig())
    val appUpdateConfig: StateFlow<AppUpdateRemoteConfig> = _appUpdateConfig.asStateFlow()

    @Volatile
    var hasEverActivatedRemoteConfig: Boolean = false
        private set

    fun create() {
        if (!started.compareAndSet(false, true)) return

        hasEverActivatedRemoteConfig = remoteConfig.info.fetchTimeMillis > 0

        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds =
                    if (BuildConfig.DEBUG) 0L else RELEASE_MIN_FETCH_INTERVAL_SECONDS
            },
        )

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            .addOnCompleteListener {
                scope.launch {
                    store.restore()
                    if (hasEverActivatedRemoteConfig) readRemoteConfig()?.let { store.update(it) }
                    publish(RemoteConfigResolution.DefaultsOrPreviouslyActivatedApplied)
                    fetch()
                    observeLiveUpdates()
                }
            }
    }

    fun refreshIfFetchFailed() {
        if (_resolution.value != RemoteConfigResolution.FetchFailed) return
        fetch()
    }

    private fun observeLiveUpdates() {
        if (!liveUpdatesStarted.compareAndSet(false, true)) return
        remoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    remoteConfig.activate().addOnSuccessListener { activated ->
                        if (activated) applyActivatedConfig()
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) = Unit
            },
        )
    }

    private fun fetch() {
        SplashAdLog.d("remote config: fetch started")
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener { activated ->
                SplashAdLog.d("remote config: fetch success, activated=$activated")
                applyActivatedConfig()
            }
            .addOnFailureListener { error ->
                SplashAdLog.d("remote config: fetch FAILED ${error.message}")
                _resolution.value = RemoteConfigResolution.FetchFailed
            }
    }

    private fun applyActivatedConfig() {
        scope.launch {
            val config = readRemoteConfig()
            if (config == null) {
                SplashAdLog.d("remote config: invalid $REMOTE_CONFIG_JSON_KEY, keeping previous config")
            } else {
                store.update(config)
            }
            publish(RemoteConfigResolution.FetchResolved)
        }
    }

    private fun readRemoteConfig(): AdsRemoteConfig? =
        AdsRemoteConfig.fromJsonOrNull(remoteConfig.getString(REMOTE_CONFIG_JSON_KEY))

    private fun publish(target: RemoteConfigResolution) {
        SplashAdLog.d(
            "remote config: $target splashInterstitial=${store.current.splashInterstitial} " +
                    "splashMaxWaitMs=${store.current.splashAdMaxWaitMs}",
        )
        _appUpdateConfig.value = store.current.appUpdate
        _resolution.value = target
    }

    companion object {

        const val REMOTE_CONFIG_JSON_KEY = "remote_confiq_json_key"

        private const val RELEASE_MIN_FETCH_INTERVAL_SECONDS = 300L
    }
}
