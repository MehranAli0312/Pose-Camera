package com.aipose.camera.posematch.ui.firebaseRemote

import com.example.ads.AdFormat
import com.example.ads.AdPlacement
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdsRemoteConfigJsonTest {

    @Test
    fun firebaseTemplateMatchesDefaultsKeyForKey() {
        assertEquals(
            Json.parseToJsonElement(FIREBASE_TEMPLATE),
            Json.parseToJsonElement(AdsRemoteConfig().toJson()),
        )
        assertEquals(AdsRemoteConfig(), AdsRemoteConfig.fromJsonOrNull(FIREBASE_TEMPLATE))
    }

    @Test
    fun missingKeysFallBackToDefaults() {
        val config = AdsRemoteConfig.fromJsonOrNull(
            """{"splash_interstitial": true, "ad_units": {"rewarded": " ca-app-pub-1/2 "}}""",
        )

        assertEquals(
            AdsRemoteConfig(
                splashInterstitial = true,
                adUnits = AdUnitIds(rewarded = " ca-app-pub-1/2 "),
            ),
            config,
        )
    }

    @Test
    fun numericCodesMapToEnums() {
        val config = AdsRemoteConfig.fromJsonOrNull(
            """
            {
              "home_screen_bottom_ad": 2,
              "onboarding_full_native_ad": 1,
              "premium_feature_dialog": 1,
              "premium_close_position": 1,
              "first_session_premium": 3,
              "returning_session_premium": 1,
              "app_update": {"version_code": 42, "force_update": true}
            }
            """,
        )

        assertEquals(BottomAdPosition.AboveBottomBar, config?.homeScreenBottomAd)
        assertEquals(OnboardingNativeAdPosition.AfterFirstStep, config?.onboardingNativeAd)
        assertEquals(PremiumFeatureDialogMode.On, config?.premiumFeatureDialog)
        assertEquals(PremiumCloseButtonPosition.Left, config?.premiumClosePosition)
        assertEquals(FirstSessionPremium.AfterOnboarding, config?.firstSessionPremium)
        assertEquals(ReturningSessionPremium.AfterSplash, config?.returningSessionPremium)
        assertEquals(AppUpdateRemoteConfig(versionCode = 42L, forceUpdate = true), config?.appUpdate)
    }

    @Test
    fun unknownEnumCodeFallsBackAndUnknownKeysAreIgnored() {
        val config = AdsRemoteConfig.fromJsonOrNull(
            """{"home_screen_bottom_ad": 9, "premium_close_position": 7, "first_session_premium": 4, "future_flag": true}""",
        )

        assertEquals(AdsRemoteConfig(), config)
    }

    @Test
    fun quotedValuesAreAcceptedAndNullUsesDefault() {
        val config = AdsRemoteConfig.fromJsonOrNull(
            """{"inner_interstitial": "true", "inner_interstitial_capping_sec": "30", "splash_ad_max_wait_sec": null}""",
        )

        assertEquals(
            AdsRemoteConfig(innerInterstitial = true, innerInterstitialCappingSeconds = 30L),
            config,
        )
    }

    @Test
    fun brokenJsonIsRejected() {
        assertNull(AdsRemoteConfig.fromJsonOrNull(""))
        assertNull(AdsRemoteConfig.fromJsonOrNull("{\"splash_interstitial\": tru"))
        assertNull(AdsRemoteConfig.fromJsonOrNull("{\"splash_interstitial\": \"maybe\"}"))
    }

    @Test
    fun adUnitsAreTrimmedWhenResolved() {
        val units = AdUnitIds(rewarded = " ca-app-pub-1/2 ")

        assertEquals(
            "ca-app-pub-1/2",
            units.unitFor(AdFormat.REWARDED, AdPlacement("x")),
        )
    }

    private companion object {
        const val FIREBASE_TEMPLATE = """
        {
          "show_onboarding_screen": true,
          "first_session_premium": 0,
          "returning_session_premium": 0,
          "local_notifications_enabled": true,
          "splash_interstitial": false,
          "splash_ad_max_wait_sec": 12,
          "app_open_on_resume": false,
          "app_open_load_timeout_sec": 8,
          "inner_interstitial": false,
          "inner_interstitial_capping_sec": 45,
          "inner_interstitial_splash_fallback": true,
          "photo_save_interstitial": false,
          "inter_load_wait_sec": 5,
          "home_screen_bottom_ad": 0,
          "language_screen_bottom_ad": 0,
          "onboard_screen_bottom_ad": 0,
          "success_screen_bottom_ad": 0,
          "pose_album_native_ad": 0,
          "home_inline_native_ad": 0,
          "onboarding_full_native_ad": 0,
          "premium_feature_dialog": 0,
          "premium_close_delay_sec": 0,
          "premium_close_position": 0,
          "ad_units": {
            "splash_inter": "",
            "activity_inter": "",
            "activity_banner": "",
            "activity_native": "",
            "onboard_full_native": "",
            "app_open_on_resume": "",
            "rewarded": ""
          },
          "native_ad_colors": {
            "cta_bg": "#FFFFFF",
            "cta_text": "#077EFC",
            "label_bg": "#077EFC",
            "label_text": "#FFFFFF"
          },
          "app_update": {
            "version_code": 0,
            "force_update": false
          }
        }
        """
    }
}
