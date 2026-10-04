package com.aipose.camera.posematch.admob_ads.remote

import android.util.Log
import com.aipose.camera.posematch.BuildConfig
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.aipose.camera.posematch.admob_ads.remote.NativeAdStyleManager.loadFromRemote
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object RemoteConfig {

    private const val TAG = "Remotes"

    const val NATIVE_MODE = "native"
    const val BANNER_MODE = "banner"
    private fun normalizedSplashAdType(): String {
        return splashInterOrAppOpen
            .trim()
            .lowercase()
            .replace(Regex("[_\\s]"), "")
    }

    fun isSplashInterEnabled(): Boolean {
        return normalizedSplashAdType() in setOf(
            "inter",
            "interstitial"
        )
    }

    fun isSplashAppOpenEnabled(): Boolean {
        return normalizedSplashAdType() == "appopen"
    }

    var openAd: Boolean = false
    // Master switch. TRUE by default on purpose: a fresh install has no cached Remote Config, so
    // its first run uses these Kotlin defaults — with false, first-launch users would never get the
    // splash interstitial. Everything else below stays off, so "true" here means splash inter only.
    var enabledAllAds: Boolean = true
    var enableInterBack: Boolean = false
    var enableInter: Boolean = false
    var reloadBannerAds: Boolean = false
    var reloadNativeAds: Boolean = false
    var nativeAdReloadTime: Long = 30000L
    var interAdsCounter: Int = 2

    // Native modes / flags
    var nativeFullSrc1: Boolean = false
    // "off" like every other native default: a fresh install has no cached Remote Config, so a
    // "native" default here would fire a NativeSplash request on first run regardless of console.
    var nativeSplashMode: String = "off"

    // PoseMatch per-screen bottom ad mode: "native" | "banner" | "off". One native id (NativeAll)
    // and one banner id (Banner_Ad) shared across all these screens.
    var nativeHome: String = "off"
    var nativeCollection: String = "off"
    var nativeSetting: String = "off"

    // Post-capture + preview screens. Same shared ids (NativeAll / Banner_Ad) as above;
    // only the mode and the layout variant differ per screen.
    // Camera defaults to "off": a bottom ad on a live viewfinder sits near the shutter and
    // is the one placement here with real accidental-click exposure. Flip to "banner" (or
    // "native") from the console once you have decided that trade-off.
    var nativeCamera: String = "off"
    var nativeEditor: String = "off"
    var nativeDetail: String = "off"
    var nativeSuccess: String = "off"

    var nativeLanguageMode: String = "off"
    var nativeLanguageHighEnable: Boolean = false
    var nativeLanguageDupMode: String = "off"

    var nativeOnboard1Mode: String = "off"
    var nativeOnboard2Mode: String = "off"
    var nativeOnboard3Mode: String = "off"
    var nativeOnboard4Mode: String = "off"
    var nativeSettings: String = "off"
    var nativeReports: String = "off"
    var nativeTamplateSettings: String = "off"
    var nativeTamplate: String = "off"

    var nativeLocations: String = "off"
    var nativeVoiceRecord: String = "off"
    var bannerFolderDetail: String = "off"
    var bannerMediaPreview: String = "off"
    var bannerSaveMedia: String = "off"
    var bannerImageMapPreview: String = "off"
    var bannerAddManualLocation: String = "off"
    var bannerEditManual: String = "off"
    var bannerImageMap: String = "off"
    var bannerCreateNotes: String = "off"
    var nativeMainBottom: String = "off"
    var nativeExitMode: String = "off"

    // ---- Interstitials ----------------------------------------------------------------------
    // Splash full-screen type (uses the SEPARATE InterSplash ad id):
    //   "inter" = splash interstitial | "appopen" = splash app-open | anything else = none.
    var splashInterOrAppOpen: String = "inter"

    // All IN-APP interstitials share ONE ad id (InterHome) and one frequency counter (interAdsCounter).
    // Each placement has its own key so it can be toggled independently.
    // FORWARD (click) placements — additionally gated by enableInter:
    var interCollectionClick: Boolean = false
    var interSettingClick: Boolean = false
    var interCameraClick: Boolean = false
    // Capture -> editor. Defaults OFF: the user just pressed the shutter and is waiting to
    // see their own photo, so an ad there reads as a malfunction. Enable remotely if wanted.
    var interCameraCapture: Boolean = false
    var interEditorSave: Boolean = false
    var interCollectionDetail: Boolean = false
    var interSuccessHome: Boolean = false
    // BACK-press placements — additionally gated by enableInterBack:
    var interCollectionBack: Boolean = false
    var interSettingBack: Boolean = false
    var interCameraBack: Boolean = false
    var interEditorBack: Boolean = false
    var interDetailBack: Boolean = false

    /**
     * Is ANY in-app interstitial placement actually reachable right now?
     *
     * The master switches alone are NOT a sufficient test: `enableInter` can be true while every
     * individual placement key is false, and in that state loading InterHome means paying for an ad
     * request that can never result in a show — it inflates the request count in the AdMob console
     * and drags the match rate down.
     *
     * Every InterHome load path funnels through this, so turning the placements off really does
     * stop the traffic. The splash interstitial uses a separate id and is NOT covered here.
     */
    fun anyInAppInterEnabled(): Boolean {
        if (!enabledAllAds) return false
        val forward = enableInter && (
            interCollectionClick || interSettingClick || interCameraClick ||
                interCameraCapture || interEditorSave || interCollectionDetail || interSuccessHome
            )
        val back = enableInterBack && (
            interCollectionBack || interSettingBack || interCameraBack ||
                interEditorBack || interDetailBack
            )
        return forward || back
    }

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    fun get(): FirebaseRemoteConfig = remoteConfig

    // -------------------- FORCE FRESH FETCH --------------------

    /**
     * Applies the values activated in a PREVIOUS session immediately (no network, no waiting) and
     * then refreshes in the background.
     *
     * Firebase persists the last activated config to disk, so `cfg.getString(...)` is already
     * populated the moment the process starts. The slow part is only the network `fetch()` — which
     * cost ~2s on the splash critical path and directly hurt the ad show rate. The refresh below
     * still runs every launch; its values apply to this session as soon as they land, and are
     * guaranteed to be in place for the next launch.
     *
     * Consequence to be aware of: a change made in the Firebase console reaches a given user one
     * launch later than before, and a brand-new install's very first run uses the Kotlin defaults
     * declared above.
     */
    fun applyCachedThenRefresh(callBack: (Boolean) -> Unit) {
        applyRemoteValues(remoteConfig, callBack)
        refreshInBackground()
    }

    private fun refreshInBackground() {
        val interval = if (BuildConfig.DEBUG) 0L else 3600L
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(interval)
            .build()

        // Chained, not fire-and-forget: fetchAndActivate must not race the settings task, otherwise
        // the minimum fetch interval may not be applied to this fetch.
        remoteConfig.setConfigSettingsAsync(configSettings).addOnCompleteListener {
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "RemoteConfig background refresh successful")
                    // Apply straight away too — harmless mid-session, and lets a fresh install pick
                    // up real values without a restart.
                    applyRemoteValues(remoteConfig) {}
                } else {
                    Log.w(
                        TAG,
                        "RemoteConfig refresh failed (offline or throttled); cached values stand",
                        task.exception
                    )
                }
            }
        }
    }


    // -------------------- APPLY VALUES --------------------
    private fun applyRemoteValues(
        cfg: FirebaseRemoteConfig, callBack: (Boolean) -> Unit
    ) {
        try {
            Log.d(TAG, "applyRemoteValues:cfg$cfg ")
            val raw = cfg.getString("key_all_ads_json")
            val nativeLayout = cfg.getString("key_native_layouts")

            loadFromRemote(nativeLayout)

            if (raw.isBlank() || raw == "{}") {
                Log.d(TAG, "all_ads_json empty, defaults applied")
                callBack(true)
                return
            }

            val jsonObj = JsonParser.parseString(raw).asJsonObject

            // ---------------- GLOBAL ----------------
            openAd = readBoolean(jsonObj, "openAd", openAd)
            enabledAllAds = readBoolean(jsonObj, "enabledAllAds", enabledAllAds)
            enableInterBack = readBoolean(jsonObj, "enableInterBack", enableInterBack)
            enableInter = readBoolean(jsonObj, "enableInter", enableInter)
            reloadBannerAds = readBoolean(jsonObj, "reloadBannerAds", reloadBannerAds)
            reloadNativeAds = readBoolean(jsonObj, "reloadNativeAds", reloadNativeAds)
            nativeAdReloadTime = readLong(jsonObj, "nativeAdReloadTime", nativeAdReloadTime)
            interAdsCounter = readInt(jsonObj, "interAdsCounter", interAdsCounter)

            // ---------------- NATIVE FLAGS ----------------
            nativeFullSrc1 = readBoolean(jsonObj, "nativeFullSrc1", nativeFullSrc1)
            nativeLanguageHighEnable =
                readBoolean(jsonObj, "nativeLanguageHigh", nativeLanguageHighEnable)

            // ---------------- NATIVE MODES ----------------
            nativeSplashMode = readMode(jsonObj, "nativeSplash", nativeSplashMode)
            // PoseMatch per-screen bottom ad modes
            nativeHome = readMode(jsonObj, "nativeHome", nativeHome)
            nativeCollection = readMode(jsonObj, "nativeCollection", nativeCollection)
            nativeSetting = readMode(jsonObj, "nativeSetting", nativeSetting)
            nativeCamera = readMode(jsonObj, "nativeCamera", nativeCamera)
            nativeEditor = readMode(jsonObj, "nativeEditor", nativeEditor)
            nativeDetail = readMode(jsonObj, "nativeDetail", nativeDetail)
            nativeSuccess = readMode(jsonObj, "nativeSuccess", nativeSuccess)
            nativeLanguageMode = readMode(jsonObj, "nativeLanguage", nativeLanguageMode)
            nativeLanguageDupMode = readMode(jsonObj, "nativeLanguageDup", nativeLanguageDupMode)

            nativeOnboard1Mode = readMode(jsonObj, "nativeOnboard1", nativeOnboard1Mode)
            nativeOnboard2Mode = readMode(jsonObj, "nativeOnboard2", nativeOnboard2Mode)
            nativeOnboard3Mode = readMode(jsonObj, "nativeOnboard3", nativeOnboard3Mode)
            nativeOnboard4Mode = readMode(jsonObj, "nativeOnboard4", nativeOnboard4Mode)

            nativeSettings = readMode(jsonObj, "nativeSettings", nativeSettings)
            nativeReports = readMode(jsonObj, "nativeReports", nativeReports)
            nativeTamplateSettings =
                readMode(jsonObj, "nativeTamplateSettings", nativeTamplateSettings)
            nativeTamplate = readMode(jsonObj, "nativeTamplate", nativeTamplate)
            nativeLocations = readMode(jsonObj, "nativeLocations", nativeLocations)
            nativeVoiceRecord = readMode(jsonObj, "nativeVoiceRecord", nativeVoiceRecord)
            bannerFolderDetail = readMode(jsonObj, "bannerFolderDetail", bannerFolderDetail)
            bannerMediaPreview = readMode(jsonObj, "bannerMediaPreview", bannerMediaPreview)
            bannerSaveMedia = readMode(jsonObj, "bannerSaveMedia", bannerSaveMedia)
            bannerImageMapPreview =
                readMode(jsonObj, "bannerImageMapPreview", bannerImageMapPreview)
            bannerAddManualLocation =
                readMode(jsonObj, "bannerAddManualLocation", bannerAddManualLocation)
            bannerEditManual = readMode(jsonObj, "bannerEditManual", bannerEditManual)
            bannerImageMap = readMode(jsonObj, "bannerImageMap", bannerImageMap)
            bannerCreateNotes = readMode(jsonObj, "bannerCreateNotes", bannerCreateNotes)
            nativeMainBottom = readMode(jsonObj, "nativeMainBottom", nativeMainBottom)
            nativeExitMode = readMode(jsonObj, "nativeExit", nativeExitMode)
            // ---------------- INTERSTITIAL FLAGS ----------------
            splashInterOrAppOpen =
                readString(jsonObj, "splashInterOrAppOpen", splashInterOrAppOpen)
            // Forward (click) placements
            interCollectionClick = readBoolean(jsonObj, "interCollectionClick", interCollectionClick)
            interSettingClick = readBoolean(jsonObj, "interSettingClick", interSettingClick)
            interCameraClick = readBoolean(jsonObj, "interCameraClick", interCameraClick)
            interCameraCapture = readBoolean(jsonObj, "interCameraCapture", interCameraCapture)
            interEditorSave = readBoolean(jsonObj, "interEditorSave", interEditorSave)
            interCollectionDetail = readBoolean(jsonObj, "interCollectionDetail", interCollectionDetail)
            interSuccessHome = readBoolean(jsonObj, "interSuccessHome", interSuccessHome)
            // Back-press placements
            interCollectionBack = readBoolean(jsonObj, "interCollectionBack", interCollectionBack)
            interSettingBack = readBoolean(jsonObj, "interSettingBack", interSettingBack)
            interCameraBack = readBoolean(jsonObj, "interCameraBack", interCameraBack)
            interEditorBack = readBoolean(jsonObj, "interEditorBack", interEditorBack)
            interDetailBack = readBoolean(jsonObj, "interDetailBack", interDetailBack)


            Log.d(TAG, "RemoteConfig values fully applied.")
            callBack(true)

        } catch (e: Exception) {
            Log.e(TAG, "Error applying remote config", e)
            callBack(false)
        }
    }


    // -------------------- HELPERS --------------------
    private fun readMode(json: JsonObject, key: String, defaultMode: String): String = try {
        if (!json.has(key)) defaultMode
        else {
            val el = json.get(key)
            when {
                el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString
                el.isJsonPrimitive && el.asJsonPrimitive.isBoolean -> if (el.asBoolean) NATIVE_MODE else "off"

                else -> defaultMode
            }
        }
    } catch (ex: Exception) {
        defaultMode
    }

    private fun readBoolean(json: JsonObject, key: String, default: Boolean): Boolean = try {
        if (!json.has(key)) default
        else {
            val el = json.get(key)
            when {
                el.isJsonPrimitive && el.asJsonPrimitive.isBoolean -> el.asBoolean
                el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString.equals(
                    "true", true
                )

                else -> default
            }
        }
    } catch (ex: Exception) {
        default
    }

    private fun readInt(json: JsonObject, key: String, default: Int): Int = try {
        if (!json.has(key)) default
        else {
            val el = json.get(key)
            when {
                el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> el.asInt
                el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString.toIntOrNull()
                    ?: default

                else -> default
            }
        }
    } catch (ex: Exception) {
        default
    }

    private fun readLong(json: JsonObject, key: String, default: Long): Long = try {
        if (!json.has(key)) default
        else {
            val el = json.get(key)
            when {
                el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> el.asLong
                el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString.toLongOrNull()
                    ?: default

                else -> default
            }
        }
    } catch (ex: Exception) {
        default
    }

    private fun readString(json: JsonObject, key: String, default: String): String = try {
        if (!json.has(key)) default
        else {
            val el = json.get(key)
            when {
                el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString
                else -> default
            }
        }
    } catch (ex: Exception) {
        default
    }
}
