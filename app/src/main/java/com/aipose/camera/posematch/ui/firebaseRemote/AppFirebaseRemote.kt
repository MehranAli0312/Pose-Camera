package com.aipose.camera.posematch.ui.firebaseRemote

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.aipose.camera.posematch.BuildConfig
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.onPlacementConfigChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.example.ads.AdsManager
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AppFirebaseRemote(
    private val store: AdsRemoteConfigStore,
    private val adsManager: AdsManager,
) {

    private val remoteConfig = Firebase.remoteConfig
    private val started = AtomicBoolean(false)
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
                    _appUpdateConfig.value = readAppUpdateConfig()
                    publish(RemoteConfigResolution.DefaultsOrPreviouslyActivatedApplied)
                    fetch()
                }
            }
    }

    fun refreshIfFetchFailed() {
        if (_resolution.value != RemoteConfigResolution.FetchFailed) return
        fetch()
    }

    private fun fetch() {
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                scope.launch {
                    store.update(readRemoteConfig())
                    _appUpdateConfig.value = readAppUpdateConfig()
                    publish(RemoteConfigResolution.FetchResolved)
                }
            }
            .addOnFailureListener { _resolution.value = RemoteConfigResolution.FetchFailed }
    }

    private fun readRemoteConfig() = AdsRemoteConfig(
        splashInterstitial = remoteConfig.getBoolean(SPLASH_INTERSTITIAL_AD_KEY),
        homeScreenBottomAd = remoteConfig.getLong(HOME_SCREEN_BOTTOM_AD_KEY).toInt(),
        homeScreenBottomAdPosition = BottomAdPosition.fromRemote(
            remoteConfig.getLong(HOME_SCREEN_BOTTOM_AD_POSITION_KEY),
        ),
        languageScreenBottomAd = remoteConfig.getLong(LANGUAGE_SCREEN_BOTTOM_AD_KEY).toInt(),
        onboardScreenBottomAd = remoteConfig.getLong(ONBOARD_SCREEN_BOTTOM_AD_KEY).toInt(),
        appOpenOnResume = remoteConfig.getBoolean(APP_OPEN_ON_RESUME_AD_KEY),
        appOpenLoadTimeoutSeconds = remoteConfig.getLong(APP_OPEN_LOAD_TIMEOUT_SECONDS_KEY),
        splashAdMaxWaitSeconds = remoteConfig.getLong(SPLASH_AD_MAX_WAIT_SECONDS_KEY),
        innerInterstitial = remoteConfig.getBoolean(INNER_INTERSTITIAL_AD_KEY),
        innerInterstitialCappingSeconds = remoteConfig.getLong(INNER_INTERSTITIAL_CAPPING_KEY),
        innerInterstitialSplashFallback = remoteConfig.getBoolean(
            INNER_INTERSTITIAL_SPLASH_FALLBACK_KEY,
        ),
        premiumFeatureDialog = PremiumFeatureDialogMode.fromRemote(
            remoteConfig.getLong(PREMIUM_FEATURE_DIALOG_KEY),
        ),
        premiumCloseDelaySeconds = remoteConfig.getLong(PREMIUM_CLOSE_DELAY_SECONDS_KEY),
        premiumClosePosition = PremiumCloseButtonPosition.fromRemote(
            remoteConfig.getLong(PREMIUM_CLOSE_POSITION_KEY),
        ),
        premiumAnnualPlan = remoteConfig.getBoolean(PREMIUM_ANNUAL_PLAN_KEY),
        showOnboardingScreen = remoteConfig.getBoolean(SHOW_ONBOARDING_SCREEN_KEY),
        splashToPremium = remoteConfig.getBoolean(SPLASH_TO_PREMIUM_KEY),
        onboardingNativeAd = OnboardingNativeAdPosition.fromRemote(
            remoteConfig.getLong(ONBOARDING_NATIVE_AD_KEY),
        ),
        adUnits = AdUnitIds.read(remoteConfig::getString),
        nativeAdColors = NativeAdColorHexes.read(remoteConfig::getString),
    )

    private fun readAppUpdateConfig() = AppUpdateRemoteConfig(
        versionCode = remoteConfig.getLong(APP_UPDATE_VERSION_CODE_KEY),
        forceUpdate = remoteConfig.getBoolean(APP_UPDATE_FORCE_KEY),
    )

    private fun publish(target: RemoteConfigResolution) {
        _resolution.value = target
        onPlacementConfigChanged(adsManager)
    }

    companion object {

        const val SHOW_ONBOARDING_SCREEN_KEY = "show_onboarding_screen"
        const val SPLASH_TO_PREMIUM_KEY = "splash_to_premium"
        const val ONBOARDING_NATIVE_AD_KEY = "onboarding_full_native_ad_key"

        const val SPLASH_INTERSTITIAL_AD_KEY = "splash_interstitial_ad_key"
        const val INNER_INTERSTITIAL_AD_KEY = "inner_interstitial_ad_key"
        const val INNER_INTERSTITIAL_CAPPING_KEY = "inner_interstitial_capping_sec"
        const val INNER_INTERSTITIAL_SPLASH_FALLBACK_KEY = "inner_inter_splash_fallback"

        const val APP_OPEN_ON_RESUME_AD_KEY = "app_open_on_resume_ad_key"
        const val APP_OPEN_LOAD_TIMEOUT_SECONDS_KEY = "app_open_load_timeout_sec"
        const val SPLASH_AD_MAX_WAIT_SECONDS_KEY = "splash_ad_max_wait_sec"

        const val HOME_SCREEN_BOTTOM_AD_KEY = "home_screen_bottom_ad_key"
        const val HOME_SCREEN_BOTTOM_AD_POSITION_KEY = "home_screen_bottom_ad_position_key"
        const val LANGUAGE_SCREEN_BOTTOM_AD_KEY = "language_screen_bottom_ad_key"
        const val ONBOARD_SCREEN_BOTTOM_AD_KEY = "onboard_screen_bottom_ad_key"

        const val PREMIUM_FEATURE_DIALOG_KEY = "premium_feature_dialog_key"
        const val PREMIUM_CLOSE_DELAY_SECONDS_KEY = "premium_close_delay_sec"
        const val PREMIUM_CLOSE_POSITION_KEY = "premium_close_position_key"
        const val PREMIUM_ANNUAL_PLAN_KEY = "premium_annual_plan_key"

        const val NATIVE_AD_CTA_BG_COLOR = "native_ad_cta_bg_color"
        const val NATIVE_AD_CTA_TEXT_COLOR = "native_ad_cta_text_color"
        const val NATIVE_AD_LABEL_BG_COLOR = "native_ad_label_bg_color"
        const val NATIVE_AD_LABEL_TEXT_COLOR = "native_ad_label_text_color"

        const val SPLASH_INTER_AD_UNIT = "splash_inter_ad_unit"
        const val ACTIVITY_INTER_AD_UNIT = "activity_inter_ad_unit"
        const val ACTIVITY_BANNER_AD_UNIT = "activity_banner_ad_unit"
        const val ACTIVITY_NATIVE_AD_UNIT = "activity_native_ad_unit"
        const val APP_OPEN_ON_RESUME_AD_UNIT = "app_open_on_resume_ad_unit"
        const val REWARDED_AD_UNIT = "rewarded_ad_unit"

        const val APP_UPDATE_VERSION_CODE_KEY = "app_update_version_code"
        const val APP_UPDATE_FORCE_KEY = "app_update_force"

        private const val RELEASE_MIN_FETCH_INTERVAL_SECONDS = 3600L
    }
}
