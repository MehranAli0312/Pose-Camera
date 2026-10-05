package com.aipose.camera.posematch.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

internal object AppPreferencesKeys {
    val IS_ON_SPLASH_FIRST_RUN = booleanPreferencesKey("IS_ON_SPLASH_FIRST_RUN")
    val APP_THEME = stringPreferencesKey("app_theme")
    val LANGUAGE = stringPreferencesKey("Language")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("NOTIFICATIONS_ENABLED")
    val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("NOTIFICATION_PERMISSION_ASKED")
    val RATE_US_SUBMITTED = booleanPreferencesKey("RATE_US_SUBMITTED")
    val RATE_US_PROMPT_COUNT = intPreferencesKey("RATE_US_PROMPT_COUNT")
    val PRO_ENTITLED = booleanPreferencesKey("pro_entitled")
    val PRO_LIFETIME = booleanPreferencesKey("pro_lifetime")
    val PRO_VERIFIED_AT = longPreferencesKey("pro_verified_at")
    val RETAIN_SKELETON = booleanPreferencesKey("retain_skeleton_overlay")
    val CAMERA_COACH_SEEN = booleanPreferencesKey("camera_coach_seen")
    val CAPTURE_TIMER_SECONDS = intPreferencesKey("capture_timer_seconds")
    val LEGACY_PREFERENCES_IMPORTED = booleanPreferencesKey("legacy_preferences_imported")
    val FAVORITE_POSES = stringSetPreferencesKey("favorite_poses")
}
