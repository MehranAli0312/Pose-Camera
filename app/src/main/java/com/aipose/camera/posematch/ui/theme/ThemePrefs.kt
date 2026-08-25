package com.aipose.camera.posematch.ui.theme

import android.content.Context

/**
 * Synchronous store for the Dark/Light choice. The value must be readable from
 * [android.app.Activity.attachBaseContext] (before any coroutine/DataStore is available), so it
 * lives in SharedPreferences. DataStore still mirrors it for the settings label via the ViewModel.
 */
object ThemePrefs {
    private const val PREFS = "posematch_ui"
    private const val KEY_DARK = "dark_theme"

    /** Dark is the default (the app shipped dark-only). */
    fun isDark(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DARK, true)

    fun setDark(context: Context, dark: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DARK, dark).apply()
    }
}
