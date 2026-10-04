package com.aipose.camera.posematch.admob_ads.remote


import android.util.Log
import com.aipose.camera.posematch.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val NATIVE_SPLASH = "native_splash"
const val NATIVE_LANGUAGE = "native_language"
const val NATIVE_PERMISSION = "native_permission"
const val NATIVE_ONBOARD1 = "native_onboard1"
const val NATIVE_ONBOARD2 = "native_onboard2"
const val NATIVE_ONBOARD3 = "native_onboard3"
const val NATIVE_ONBOARD4 = "native_onboard4"
const val NATIVE_MAIN_BOTTOM= "native_home_bottom"
const val NATIVE_COLLECTION = "native_collection"
const val NATIVE_SETTINGS = "native_setting"
const val NATIVE_CAMERA = "native_camera"
const val NATIVE_EDITOR = "native_editor"
const val NATIVE_DETAIL = "native_detail"
const val NATIVE_SUCCESS = "native_success"
const val NATIVE_REPORTS = "native_reports"
const val NATIVE_VOICE_RECORD = "native_voice_record"
const val NATIVE_LOCATION = "native_location"
const val NATIVE_TAMP_SETTINGS = "native_tamp_settings"
const val NATIVE_TAMP = "native_tamp"
const val NATIVE_EXIT = "native_exit"

object NativeAdStyleManager {

    private const val TAG = "NativeAdStyleManager"


    // Map of adName -> variant (Int)
    private var adStyleMap: Map<String, Int> = emptyMap()

    // Store layouts as properties instead of local variables in init
    private val listLayouts: List<Int> = listOf(
        R.layout.native_layout1,
        R.layout.native_layout1,
        R.layout.native_layout2,
        R.layout.native_layout3,
        R.layout.native_layout4,
        R.layout.native_layout5
    )

    private val listLayoutsLoad: List<Int> = listOf(
        R.layout.native_layout1_loading,
        R.layout.native_layout1_loading,
        R.layout.native_layout2_loading,
        R.layout.native_layout3_loading,
        R.layout.native_layout4_loading,
        R.layout.native_layout5_loading
    )

    // ✅ Fixed JSON (quoted keys, no trailing commas)
    private val defaultAdStyleJson = """
        {
          "$NATIVE_LANGUAGE": 5,
          "$NATIVE_MAIN_BOTTOM": 1,
          "$NATIVE_SETTINGS": 2,
          "$NATIVE_CAMERA": 1,
          "$NATIVE_EDITOR": 1,
          "$NATIVE_DETAIL": 1,
          "$NATIVE_SUCCESS": 3,
          "$NATIVE_ONBOARD1": 5,
          "$NATIVE_ONBOARD2": 5,
          "$NATIVE_ONBOARD3": 5,
          "$NATIVE_ONBOARD4": 5,
          "$NATIVE_SPLASH": 3,
          "$NATIVE_REPORTS": 3,
          "$NATIVE_VOICE_RECORD": 3,
          "$NATIVE_LOCATION": 2,
          "$NATIVE_TAMP_SETTINGS": 2,
          "$NATIVE_TAMP": 2,
          "$NATIVE_EXIT": 3
        }
    """.trimIndent()

    fun loadFromRemote(remoteJson: String?) {
        Log.d(TAG, "loadFromRemote: $remoteJson")
        val jsonToUse = if (remoteJson.isNullOrBlank() || remoteJson == "{}") defaultAdStyleJson else remoteJson
        adStyleMap = try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            Gson().fromJson<Map<String, Int>>(jsonToUse, type)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse ad style config, using default", e)
            val type = object : TypeToken<Map<String, Int>>() {}.type
            Gson().fromJson<Map<String, Int>>(defaultAdStyleJson, type)
        } ?: emptyMap()

        Log.d(TAG, "Ad style config loaded: $adStyleMap")
    }

    fun getAdLayouts(adName: String): Pair<Int, Int> {
        val variant = adStyleMap[adName] ?: 1

        val defaultContent = R.layout.native_layout1
        val defaultShimmer = R.layout.native_layout1_loading

        val contentLayout = listLayouts.getOrNull(variant) ?: defaultContent
        val shimmerLayout = listLayoutsLoad.getOrNull(variant) ?: defaultShimmer

        Log.d(TAG, "getAdLayouts: adName=$adName variant=$variant content=$contentLayout shimmer=$shimmerLayout")

        return Pair(contentLayout, shimmerLayout)
    }
}
