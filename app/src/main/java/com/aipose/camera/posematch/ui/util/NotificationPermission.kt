package com.aipose.camera.posematch.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.aipose.camera.posematch.analytics.Analytics

/**
 * POST_NOTIFICATIONS runtime handling (Android 13 / API 33+).
 *
 * Below API 33 the permission is granted at install time, so everything here no-ops.
 *
 * Google's guidance is to ask in context rather than at cold start, and to ask ONCE — a second
 * denial on Android 13+ permanently locks the dialog out, so we only auto-prompt a single time and
 * remember that we did.
 */
object NotificationPermission {

    private const val PREFS = "posematch_permissions"
    private const val KEY_ASKED = "post_notifications_asked"

    private val required: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isGranted(context: Context): Boolean {
        if (!required) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun alreadyAsked(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ASKED, false)

    private fun markAsked(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_ASKED, true) }
    }

    /**
     * Registers the launcher. Must be called during fragment construction / onCreate, before the
     * fragment reaches STARTED, or ActivityResult throws.
     */
    fun register(fragment: Fragment): ActivityResultLauncher<String> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Analytics.permission("post_notifications", granted)
        }

    /**
     * Asks once, if needed. Safe to call on every entry to a screen — it self-suppresses when the
     * permission isn't required, is already granted, or has been requested before.
     */
    fun requestIfNeeded(fragment: Fragment, launcher: ActivityResultLauncher<String>) {
        if (!required) return
        val context = fragment.context ?: return
        if (isGranted(context) || alreadyAsked(context)) return

        markAsked(context)
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
