package com.aipose.camera.posematch.ui.firebaseRemote

import com.example.ads.AdPlacement
import com.example.ads.AdRemoteStyles
import com.example.ads.AdSlotStyle
import com.example.ads.NativeAdDesign
import com.aipose.camera.posematch.ads.HomeScreenBottom
import com.aipose.camera.posematch.ads.LanguageScreenBottom
import com.aipose.camera.posematch.ads.OnboardScreenBottom
import com.aipose.camera.posematch.ads.OnboardingFullScreenNative
import com.aipose.camera.posematch.data.local.AdsRemoteDataStore
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.APP_OPEN_LOAD_TIMEOUT_SECONDS_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.APP_OPEN_ON_RESUME_AD_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.HOME_SCREEN_BOTTOM_AD_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.HOME_SCREEN_BOTTOM_AD_POSITION_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.INNER_INTERSTITIAL_AD_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.INNER_INTERSTITIAL_CAPPING_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.INNER_INTERSTITIAL_SPLASH_FALLBACK_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.LANGUAGE_SCREEN_BOTTOM_AD_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ONBOARDING_NATIVE_AD_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.ONBOARD_SCREEN_BOTTOM_AD_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.PREMIUM_ANNUAL_PLAN_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.PREMIUM_CLOSE_DELAY_SECONDS_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.PREMIUM_CLOSE_POSITION_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.PREMIUM_FEATURE_DIALOG_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.SHOW_ONBOARDING_SCREEN_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.SPLASH_AD_MAX_WAIT_SECONDS_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.SPLASH_INTERSTITIAL_AD_KEY
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.SPLASH_TO_PREMIUM_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdsRemoteConfig(
    val splashInterstitial: Boolean = false,
    val homeScreenBottomAd: Int = AdRemoteStyles.OFF,
    val homeScreenBottomAdPosition: BottomAdPosition = BottomAdPosition.Off,
    val languageScreenBottomAd: Int = AdRemoteStyles.OFF,
    val onboardScreenBottomAd: Int = AdRemoteStyles.OFF,
    val appOpenOnResume: Boolean = false,
    val appOpenLoadTimeoutSeconds: Long = DEFAULT_APP_OPEN_LOAD_TIMEOUT_SECONDS,
    val splashAdMaxWaitSeconds: Long = DEFAULT_SPLASH_AD_MAX_WAIT_SECONDS,
    val innerInterstitial: Boolean = false,
    val innerInterstitialCappingSeconds: Long = DEFAULT_INNER_INTERSTITIAL_CAPPING_SECONDS,
    val innerInterstitialSplashFallback: Boolean = true,
    val premiumFeatureDialog: PremiumFeatureDialogMode = PremiumFeatureDialogMode.Off,
    val premiumCloseDelaySeconds: Long = DEFAULT_PREMIUM_CLOSE_DELAY_SECONDS,
    val premiumClosePosition: PremiumCloseButtonPosition = PremiumCloseButtonPosition.Right,
    val premiumAnnualPlan: Boolean = true,
    val showOnboardingScreen: Boolean = true,
    val splashToPremium: Boolean = false,
    val onboardingNativeAd: OnboardingNativeAdPosition = OnboardingNativeAdPosition.Off,
    val adUnits: AdUnitIds = AdUnitIds(),
    val nativeAdColors: NativeAdColorHexes = NativeAdColorHexes(),
) {
    fun slotStyleFor(placement: AdPlacement): AdSlotStyle = when (placement) {
        AdPlacement.HomeScreenBottom -> AdRemoteStyles.bannerOrSmallNative(homeScreenBottomAd)
        AdPlacement.LanguageScreenBottom ->
            AdRemoteStyles.bannerOrSmallNative(languageScreenBottomAd)
        AdPlacement.OnboardScreenBottom ->
            AdRemoteStyles.bannerOrSmallNative(onboardScreenBottomAd)
        AdPlacement.OnboardingFullScreenNative ->
            if (onboardingNativeAd == OnboardingNativeAdPosition.Off) {
                AdSlotStyle.Hidden
            } else {
                AdSlotStyle.Native(NativeAdDesign.FULL_SCREEN)
            }
        else -> AdSlotStyle.Hidden
    }

    fun positionFor(placement: AdPlacement): BottomAdPosition = when (placement) {
        AdPlacement.HomeScreenBottom -> homeScreenBottomAdPosition
        else -> BottomAdPosition.Off
    }

    val premiumCloseDelay: Int
        get() = premiumCloseDelaySeconds.coerceIn(0L, MAX_PREMIUM_CLOSE_DELAY_SECONDS).toInt()

    val appOpenLoadTimeoutMs: Long
        get() = appOpenLoadTimeoutSeconds
            .coerceIn(MIN_AD_WAIT_SECONDS, MAX_APP_OPEN_LOAD_TIMEOUT_SECONDS) * MILLIS_PER_SECOND

    val splashAdMaxWaitMs: Long
        get() = splashAdMaxWaitSeconds
            .coerceIn(MIN_AD_WAIT_SECONDS, MAX_SPLASH_AD_WAIT_SECONDS) * MILLIS_PER_SECOND

    companion object {
        const val DEFAULT_INNER_INTERSTITIAL_CAPPING_SECONDS = 45L
        const val DEFAULT_PREMIUM_CLOSE_DELAY_SECONDS = 0L
        const val DEFAULT_APP_OPEN_LOAD_TIMEOUT_SECONDS = 8L
        const val DEFAULT_SPLASH_AD_MAX_WAIT_SECONDS = 12L
        private const val MAX_PREMIUM_CLOSE_DELAY_SECONDS = 30L
        private const val MIN_AD_WAIT_SECONDS = 1L
        private const val MAX_APP_OPEN_LOAD_TIMEOUT_SECONDS = 20L
        private const val MAX_SPLASH_AD_WAIT_SECONDS = 30L
        private const val MILLIS_PER_SECOND = 1000L
    }
}

class AdsRemoteConfigStore(
    private val dataStore: AdsRemoteDataStore,
) {

    private val defaults = AdsRemoteConfig()

    private val _config = MutableStateFlow(defaults)

    val config: StateFlow<AdsRemoteConfig> = _config.asStateFlow()

    val current: AdsRemoteConfig get() = _config.value

    suspend fun restore() {
        _config.value = AdsRemoteConfig(
            splashInterstitial = dataStore.getBoolean(
                SPLASH_INTERSTITIAL_AD_KEY,
                defaults.splashInterstitial,
            ),
            homeScreenBottomAd = dataStore.getLong(
                HOME_SCREEN_BOTTOM_AD_KEY,
                defaults.homeScreenBottomAd.toLong(),
            ).toInt(),
            homeScreenBottomAdPosition = BottomAdPosition.fromRemote(
                dataStore.getLong(
                    HOME_SCREEN_BOTTOM_AD_POSITION_KEY,
                    defaults.homeScreenBottomAdPosition.remoteValue,
                ),
            ),
            languageScreenBottomAd = dataStore.getLong(
                LANGUAGE_SCREEN_BOTTOM_AD_KEY,
                defaults.languageScreenBottomAd.toLong(),
            ).toInt(),
            onboardScreenBottomAd = dataStore.getLong(
                ONBOARD_SCREEN_BOTTOM_AD_KEY,
                defaults.onboardScreenBottomAd.toLong(),
            ).toInt(),
            appOpenOnResume = dataStore.getBoolean(
                APP_OPEN_ON_RESUME_AD_KEY,
                defaults.appOpenOnResume,
            ),
            appOpenLoadTimeoutSeconds = dataStore.getLong(
                APP_OPEN_LOAD_TIMEOUT_SECONDS_KEY,
                defaults.appOpenLoadTimeoutSeconds,
            ),
            splashAdMaxWaitSeconds = dataStore.getLong(
                SPLASH_AD_MAX_WAIT_SECONDS_KEY,
                defaults.splashAdMaxWaitSeconds,
            ),
            innerInterstitial = dataStore.getBoolean(
                INNER_INTERSTITIAL_AD_KEY,
                defaults.innerInterstitial,
            ),
            innerInterstitialCappingSeconds = dataStore.getLong(
                INNER_INTERSTITIAL_CAPPING_KEY,
                defaults.innerInterstitialCappingSeconds,
            ),
            innerInterstitialSplashFallback = dataStore.getBoolean(
                INNER_INTERSTITIAL_SPLASH_FALLBACK_KEY,
                defaults.innerInterstitialSplashFallback,
            ),
            premiumFeatureDialog = PremiumFeatureDialogMode.fromRemote(
                dataStore.getLong(
                    PREMIUM_FEATURE_DIALOG_KEY,
                    defaults.premiumFeatureDialog.remoteValue,
                ),
            ),
            premiumCloseDelaySeconds = dataStore.getLong(
                PREMIUM_CLOSE_DELAY_SECONDS_KEY,
                defaults.premiumCloseDelaySeconds,
            ),
            premiumClosePosition = PremiumCloseButtonPosition.fromRemote(
                dataStore.getLong(
                    PREMIUM_CLOSE_POSITION_KEY,
                    defaults.premiumClosePosition.remoteValue,
                ),
            ),
            premiumAnnualPlan = dataStore.getBoolean(
                PREMIUM_ANNUAL_PLAN_KEY,
                defaults.premiumAnnualPlan,
            ),
            showOnboardingScreen = dataStore.getBoolean(
                SHOW_ONBOARDING_SCREEN_KEY,
                defaults.showOnboardingScreen,
            ),
            splashToPremium = dataStore.getBoolean(
                SPLASH_TO_PREMIUM_KEY,
                defaults.splashToPremium,
            ),
            onboardingNativeAd = OnboardingNativeAdPosition.fromRemote(
                dataStore.getLong(
                    ONBOARDING_NATIVE_AD_KEY,
                    defaults.onboardingNativeAd.remoteValue,
                ),
            ),
            adUnits = AdUnitIds.read { key -> dataStore.getString(key, "") },
            nativeAdColors = NativeAdColorHexes.read { key -> dataStore.getString(key, "") },
        )
    }

    suspend fun update(config: AdsRemoteConfig) {
        _config.value = config
        dataStore.putBoolean(SPLASH_INTERSTITIAL_AD_KEY, config.splashInterstitial)
        dataStore.putLong(HOME_SCREEN_BOTTOM_AD_KEY, config.homeScreenBottomAd.toLong())
        dataStore.putLong(
            HOME_SCREEN_BOTTOM_AD_POSITION_KEY,
            config.homeScreenBottomAdPosition.remoteValue,
        )
        dataStore.putLong(LANGUAGE_SCREEN_BOTTOM_AD_KEY, config.languageScreenBottomAd.toLong())
        dataStore.putLong(ONBOARD_SCREEN_BOTTOM_AD_KEY, config.onboardScreenBottomAd.toLong())
        dataStore.putBoolean(APP_OPEN_ON_RESUME_AD_KEY, config.appOpenOnResume)
        dataStore.putLong(APP_OPEN_LOAD_TIMEOUT_SECONDS_KEY, config.appOpenLoadTimeoutSeconds)
        dataStore.putLong(SPLASH_AD_MAX_WAIT_SECONDS_KEY, config.splashAdMaxWaitSeconds)
        dataStore.putBoolean(INNER_INTERSTITIAL_AD_KEY, config.innerInterstitial)
        dataStore.putLong(INNER_INTERSTITIAL_CAPPING_KEY, config.innerInterstitialCappingSeconds)
        dataStore.putBoolean(
            INNER_INTERSTITIAL_SPLASH_FALLBACK_KEY,
            config.innerInterstitialSplashFallback,
        )
        dataStore.putLong(PREMIUM_FEATURE_DIALOG_KEY, config.premiumFeatureDialog.remoteValue)
        dataStore.putLong(PREMIUM_CLOSE_DELAY_SECONDS_KEY, config.premiumCloseDelaySeconds)
        dataStore.putLong(PREMIUM_CLOSE_POSITION_KEY, config.premiumClosePosition.remoteValue)
        dataStore.putBoolean(PREMIUM_ANNUAL_PLAN_KEY, config.premiumAnnualPlan)
        dataStore.putBoolean(SHOW_ONBOARDING_SCREEN_KEY, config.showOnboardingScreen)
        dataStore.putBoolean(SPLASH_TO_PREMIUM_KEY, config.splashToPremium)
        dataStore.putLong(ONBOARDING_NATIVE_AD_KEY, config.onboardingNativeAd.remoteValue)
        config.adUnits.byKey().forEach { (key, unitId) -> dataStore.putString(key, unitId) }
        config.nativeAdColors.byKey().forEach { (key, hex) -> dataStore.putString(key, hex) }
    }
}
