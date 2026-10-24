package com.aipose.camera.posematch.ui.firebaseRemote

import com.aipose.camera.posematch.ads.HomeInlineNative
import com.aipose.camera.posematch.ads.HomeScreenBottom
import com.aipose.camera.posematch.ads.LanguageScreenBottom
import com.aipose.camera.posematch.ads.OnboardScreenBottom
import com.aipose.camera.posematch.ads.OnboardingFullScreenNative
import com.aipose.camera.posematch.ads.PoseAlbumInlineFirst
import com.aipose.camera.posematch.ads.PoseAlbumInlineSecond
import com.aipose.camera.posematch.ads.SuccessScreenBottom
import com.aipose.camera.posematch.data.local.AdsRemoteDataStore
import com.aipose.camera.posematch.ui.firebaseRemote.AppFirebaseRemote.Companion.REMOTE_CONFIG_JSON_KEY
import com.example.ads.AdPlacement
import com.example.ads.AdRemoteStyles
import com.example.ads.AdSlotStyle
import com.example.ads.NativeAdDesign
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AdsRemoteConfig(
    @SerialName("splash_interstitial")
    val splashInterstitial: Boolean = false,
    @SerialName("home_screen_bottom_ad")
    val homeScreenBottomAd: BottomAdPosition = BottomAdPosition.Off,
    @SerialName("language_screen_bottom_ad")
    val languageScreenBottomAd: Int = AdRemoteStyles.OFF,
    @SerialName("onboard_screen_bottom_ad")
    val onboardScreenBottomAd: Int = AdRemoteStyles.OFF,
    @SerialName("success_screen_bottom_ad")
    val successScreenBottomAd: Int = AdRemoteStyles.OFF,
    @SerialName("pose_album_native_ad")
    val poseAlbumNativeAd: Int = AdRemoteStyles.OFF,
    @SerialName("home_inline_native_ad")
    val homeInlineNativeAd: Int = AdRemoteStyles.OFF,
    @SerialName("app_open_on_resume")
    val appOpenOnResume: Boolean = false,
    @SerialName("app_open_load_timeout_sec")
    val appOpenLoadTimeoutSeconds: Long = DEFAULT_APP_OPEN_LOAD_TIMEOUT_SECONDS,
    @SerialName("splash_ad_max_wait_sec")
    val splashAdMaxWaitSeconds: Long = DEFAULT_SPLASH_AD_MAX_WAIT_SECONDS,
    @SerialName("inner_interstitial")
    val innerInterstitial: Boolean = false,
    @SerialName("inner_interstitial_capping_sec")
    val innerInterstitialCappingSeconds: Long = DEFAULT_INNER_INTERSTITIAL_CAPPING_SECONDS,
    @SerialName("inner_interstitial_splash_fallback")
    val innerInterstitialSplashFallback: Boolean = true,
    @SerialName("photo_save_interstitial")
    val photoSaveInterstitial: Boolean = false,
    @SerialName("inter_load_wait_sec")
    val interLoadWaitSeconds: Long = DEFAULT_INTER_LOAD_WAIT_SECONDS,
    @SerialName("premium_feature_dialog")
    val premiumFeatureDialog: PremiumFeatureDialogMode = PremiumFeatureDialogMode.Off,
    @SerialName("premium_close_delay_sec")
    val premiumCloseDelaySeconds: Long = DEFAULT_PREMIUM_CLOSE_DELAY_SECONDS,
    @SerialName("premium_close_position")
    val premiumClosePosition: PremiumCloseButtonPosition = PremiumCloseButtonPosition.Right,
    @SerialName("show_onboarding_screen")
    val showOnboardingScreen: Boolean = true,
    @SerialName("first_session_premium")
    val firstSessionPremium: FirstSessionPremium = FirstSessionPremium.Off,
    @SerialName("returning_session_premium")
    val returningSessionPremium: ReturningSessionPremium = ReturningSessionPremium.Off,
    @SerialName("local_notifications_enabled")
    val localNotifications: Boolean = true,
    @SerialName("onboarding_full_native_ad")
    val onboardingNativeAd: OnboardingNativeAdPosition = OnboardingNativeAdPosition.Off,
    @SerialName("ad_units")
    val adUnits: AdUnitIds = AdUnitIds(),
    @SerialName("native_ad_colors")
    val nativeAdColors: NativeAdColorHexes = NativeAdColorHexes(),
    @SerialName("app_update")
    val appUpdate: AppUpdateRemoteConfig = AppUpdateRemoteConfig(),
) {
    fun slotStyleFor(placement: AdPlacement): AdSlotStyle = when (placement) {
        AdPlacement.HomeScreenBottom ->
            if (homeScreenBottomAd == BottomAdPosition.Off) {
                AdSlotStyle.Hidden
            } else {
                AdSlotStyle.Native(NativeAdDesign.SMALL)
            }
        AdPlacement.LanguageScreenBottom ->
            AdRemoteStyles.bannerOrNative(languageScreenBottomAd)

        AdPlacement.OnboardScreenBottom ->
            AdRemoteStyles.bannerOrNative(onboardScreenBottomAd)

        AdPlacement.SuccessScreenBottom ->
            AdRemoteStyles.bannerOrNative(successScreenBottomAd)

        AdPlacement.PoseAlbumInlineFirst,
        AdPlacement.PoseAlbumInlineSecond,
        -> AdRemoteStyles.native(poseAlbumNativeAd)

        AdPlacement.HomeInlineNative -> AdRemoteStyles.native(homeInlineNativeAd)

        AdPlacement.OnboardingFullScreenNative ->
            if (onboardingNativeAd == OnboardingNativeAdPosition.Off) {
                AdSlotStyle.Hidden
            } else {
                AdSlotStyle.Native(NativeAdDesign.FULL_SCREEN)
            }

        else -> AdSlotStyle.Hidden
    }

    val premiumCloseDelay: Int
        get() = premiumCloseDelaySeconds.coerceIn(0L, MAX_PREMIUM_CLOSE_DELAY_SECONDS).toInt()

    val appOpenLoadTimeoutMs: Long
        get() = appOpenLoadTimeoutSeconds
            .coerceIn(MIN_AD_WAIT_SECONDS, MAX_APP_OPEN_LOAD_TIMEOUT_SECONDS) * MILLIS_PER_SECOND

    val interLoadWaitMs: Long
        get() = interLoadWaitSeconds
            .coerceIn(MIN_AD_WAIT_SECONDS, MAX_INTER_LOAD_WAIT_SECONDS) * MILLIS_PER_SECOND

    val splashAdMaxWaitMs: Long
        get() = splashAdMaxWaitSeconds
            .coerceIn(MIN_AD_WAIT_SECONDS, MAX_SPLASH_AD_WAIT_SECONDS) * MILLIS_PER_SECOND

    fun toJson(): String = json.encodeToString(serializer(), this)

    companion object {
        const val DEFAULT_INNER_INTERSTITIAL_CAPPING_SECONDS = 45L
        const val DEFAULT_PREMIUM_CLOSE_DELAY_SECONDS = 0L
        const val DEFAULT_APP_OPEN_LOAD_TIMEOUT_SECONDS = 8L
        const val DEFAULT_SPLASH_AD_MAX_WAIT_SECONDS = 12L
        const val DEFAULT_INTER_LOAD_WAIT_SECONDS = 5L
        private const val MAX_PREMIUM_CLOSE_DELAY_SECONDS = 30L
        private const val MIN_AD_WAIT_SECONDS = 1L
        private const val MAX_APP_OPEN_LOAD_TIMEOUT_SECONDS = 20L
        private const val MAX_SPLASH_AD_WAIT_SECONDS = 30L
        private const val MAX_INTER_LOAD_WAIT_SECONDS = 15L
        private const val MILLIS_PER_SECOND = 1000L

        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            encodeDefaults = true
        }

        fun fromJsonOrNull(raw: String): AdsRemoteConfig? =
            runCatching { json.decodeFromString(serializer(), raw) }.getOrNull()
    }
}

class AdsRemoteConfigStore(
    private val dataStore: AdsRemoteDataStore,
) {

    private val _config = MutableStateFlow(AdsRemoteConfig())

    val config: StateFlow<AdsRemoteConfig> = _config.asStateFlow()

    val current: AdsRemoteConfig get() = _config.value

    private val stateLock = Mutex()

    private var hasLoadedState = false

    suspend fun restore() {
        stateLock.withLock {
            if (hasLoadedState) return
            val persisted = dataStore.getString(REMOTE_CONFIG_JSON_KEY, "")
            AdsRemoteConfig.fromJsonOrNull(persisted)?.let { _config.value = it }
            hasLoadedState = true
        }
    }

    suspend fun update(config: AdsRemoteConfig) {
        stateLock.withLock {
            _config.value = config
            hasLoadedState = true
        }
        dataStore.putString(REMOTE_CONFIG_JSON_KEY, config.toJson())
    }
}
