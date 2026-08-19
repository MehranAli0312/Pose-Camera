package com.aipose.camera.posematch.data

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies the user's chosen language as a per-app locale. Mirrors the selection into a small
 * synchronous SharedPreferences so it can be applied in Activity.attachBaseContext (before Compose).
 * RTL languages (Urdu, Arabic) also flip layout direction.
 */
object LocaleHelper {
    private const val PREF = "locale_pref"
    private const val KEY_LANG = "language"

    val languageTags = mapOf(
        "English" to "en",
        "Hindi" to "hi",
        "Urdu" to "ur",
        "Arabic" to "ar",
        "Spanish" to "es",
        "Turkish" to "tr",
        "Bangla" to "bn",
        "French" to "fr",
        "Portuguese" to "pt",
        "Russian" to "ru",
        "Filipino" to "fil",
        "German" to "de"
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun persistLanguage(context: Context, language: String) {
        prefs(context).edit().putString(KEY_LANG, language).apply()
    }

    fun currentLanguage(context: Context): String =
        prefs(context).getString(KEY_LANG, "English") ?: "English"

    /** Wraps a base context with the selected locale; call from attachBaseContext. */
    fun wrap(context: Context): Context {
        val tag = languageTags[currentLanguage(context)] ?: "en"
        val locale = Locale(tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
