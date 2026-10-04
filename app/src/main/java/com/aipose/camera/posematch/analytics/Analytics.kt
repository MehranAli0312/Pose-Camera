package com.aipose.camera.posematch.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.Firebase

/**
 * Single entry point for Firebase Analytics.
 *
 * Follows Google's current guidance: prefer the PREDEFINED events/params
 * (`FirebaseAnalytics.Event` / `.Param`) wherever one fits, and use the KTX `logEvent { param(..) }`
 * DSL rather than building Bundles by hand. Custom names stay snake_case and within Firebase's
 * limits (event name <= 40 chars, param name <= 40, string value <= 100).
 *
 * Ad events deliberately do NOT reuse `Event.AD_IMPRESSION` — that one is reserved for real ad
 * REVENUE reporting (it expects ad_platform/value/currency from a paid-event callback). Sending it
 * without revenue data pollutes the revenue reports, so the ad lifecycle uses custom `ad_*` events.
 */
object Analytics {

    private val fa: FirebaseAnalytics by lazy { Firebase.analytics }

    // ---- Screens ---------------------------------------------------------------------------

    /**
     * Logs a screen view. This app is single-Activity, so Firebase's automatic screen tracking
     * only ever reports "MainActivity" — every screen must report itself.
     */
    fun screen(name: String) {
        fa.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, name)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, name)
        }
    }

    // ---- Interactions ----------------------------------------------------------------------

    /** A tap on something meaningful. [item] is the control, [screen] is where it lives. */
    fun click(item: String, screen: String) {
        fa.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, screen)
            param(FirebaseAnalytics.Param.ITEM_ID, item)
        }
    }

    /** Custom app event with optional string params. */
    fun event(name: String, vararg params: Pair<String, String>) {
        fa.logEvent(name, Bundle().apply { params.forEach { (k, v) -> putString(k, v.take(100)) } })
    }

    // ---- Ads -------------------------------------------------------------------------------

    /**
     * One funnel for every ad placement: requested -> loaded/failed -> shown -> clicked/dismissed.
     * Wired inside the ad helper classes, so every placement is covered without per-screen code.
     *
     * @param format  "interstitial" | "native" | "banner" | "app_open"
     * @param placement logical slot, e.g. "splash", "home", "settings", "collections"
     */
    fun ad(action: AdAction, format: String, placement: String, detail: String? = null) {
        fa.logEvent("ad_${action.suffix}") {
            param("ad_format", format)
            param("ad_placement", placement)
            detail?.let { param("ad_detail", it.take(100)) }
        }
    }

    enum class AdAction(val suffix: String) {
        REQUESTED("requested"),
        LOADED("loaded"),
        FAILED("failed"),
        SHOWN("shown"),
        CLICKED("clicked"),
        DISMISSED("dismissed"),
        IMPRESSION("impression")
    }

    // ---- Consent / permissions -------------------------------------------------------------

    fun permission(name: String, granted: Boolean) {
        event("permission_result", "permission" to name, "granted" to granted.toString())
    }

    object Screen {
        const val SPLASH = "splash"
        const val ONBOARDING = "onboarding"
        const val HOME = "home"
        const val COLLECTIONS = "collections"
        const val SETTINGS = "settings"
        const val CAMERA = "camera"
        const val EDITOR = "editor"
        const val SUCCESS = "success"
    }
}
