package com.aipose.camera.posematch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.domain.models.ProEntitlement
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFERENCES_NAME,
        produceMigrations = { listOf(LegacyPreferencesMigration) }
    )

    private val preferences: DataStore<Preferences>
        get() = context.applicationContext.dataStore

    suspend fun getProEntitlement(): ProEntitlement =
        preferences.data.map { stored ->
            ProEntitlement(
                isPro = stored[AppPreferencesKeys.PRO_ENTITLED] ?: false,
                isLifetime = stored[AppPreferencesKeys.PRO_LIFETIME] ?: false,
                verifiedAtMillis = stored[AppPreferencesKeys.PRO_VERIFIED_AT] ?: 0L,
            )
        }.first()

    suspend fun setProEntitlement(entitlement: ProEntitlement) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.PRO_ENTITLED] = entitlement.isPro
            stored[AppPreferencesKeys.PRO_LIFETIME] = entitlement.isLifetime
            stored[AppPreferencesKeys.PRO_VERIFIED_AT] = entitlement.verifiedAtMillis
        }
    }

    fun readOnBoardStatus() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.IS_ON_SPLASH_FIRST_RUN] ?: true
    }

    suspend fun writeOnBoardStatus() {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.IS_ON_SPLASH_FIRST_RUN] = false
        }
    }

    fun getThemeOption() = preferences.data.map { stored ->
        AppThemeOption.fromKey(stored[AppPreferencesKeys.APP_THEME])
    }

    suspend fun setThemeOption(option: AppThemeOption) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.APP_THEME] = option.key
        }
    }

    fun getLanguageCode() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.LANGUAGE] ?: DEFAULT_LANGUAGE_CODE
    }

    suspend fun setLanguageCode(languageCode: String) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.LANGUAGE] = languageCode
        }
    }

    fun getRetainSkeleton() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.RETAIN_SKELETON] ?: true
    }

    suspend fun setRetainSkeleton(retain: Boolean) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.RETAIN_SKELETON] = retain
        }
    }

    fun isCameraCoachSeen() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.CAMERA_COACH_SEEN] ?: false
    }

    suspend fun markCameraCoachSeen() {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.CAMERA_COACH_SEEN] = true
        }
    }

    fun getNotificationsEnabled() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun isNotificationsEnabled(): Boolean = getNotificationsEnabled().first()

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    fun getNotificationPermissionAsked() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.NOTIFICATION_PERMISSION_ASKED] ?: false
    }

    suspend fun setNotificationPermissionAsked(asked: Boolean) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.NOTIFICATION_PERMISSION_ASKED] = asked
        }
    }

    fun getRateUsSubmitted() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.RATE_US_SUBMITTED] ?: false
    }

    suspend fun setRateUsSubmitted(submitted: Boolean) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.RATE_US_SUBMITTED] = submitted
        }
    }

    fun getRateUsPromptCount() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.RATE_US_PROMPT_COUNT] ?: 0
    }

    suspend fun incrementRateUsPromptCount() {
        preferences.edit { stored ->
            val current = stored[AppPreferencesKeys.RATE_US_PROMPT_COUNT] ?: 0
            stored[AppPreferencesKeys.RATE_US_PROMPT_COUNT] = current + 1
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "pose_match_prefs"
        const val DEFAULT_LANGUAGE_CODE = "en"
    }
}
