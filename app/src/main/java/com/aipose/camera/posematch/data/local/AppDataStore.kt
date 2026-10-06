package com.aipose.camera.posematch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
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

    fun getLanguageCode() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.LANGUAGE] ?: DEFAULT_LANGUAGE_CODE
    }

    suspend fun setLanguageCode(languageCode: String) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.LANGUAGE] = languageCode
        }
    }

    fun getKeepPoseOverlay() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.KEEP_POSE_OVERLAY] ?: true
    }

    suspend fun setKeepPoseOverlay(keepOverlay: Boolean) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.KEEP_POSE_OVERLAY] = keepOverlay
        }
    }

    fun getCaptureTimerSeconds() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.CAPTURE_TIMER_SECONDS] ?: 0
    }

    suspend fun setCaptureTimerSeconds(seconds: Int) {
        preferences.edit { stored ->
            stored[AppPreferencesKeys.CAPTURE_TIMER_SECONDS] = seconds
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

    fun getFavoritePoses() = preferences.data.map { stored ->
        stored[AppPreferencesKeys.FAVORITE_POSES].orEmpty().mapNotNull { it.toFavoriteEntry() }.toMap()
    }

    suspend fun setFavoritePose(poseId: Int, isFavorite: Boolean) {
        preferences.edit { stored ->
            val current = stored[AppPreferencesKeys.FAVORITE_POSES].orEmpty()
                .filterNot { it.toFavoriteEntry()?.first == poseId }
            stored[AppPreferencesKeys.FAVORITE_POSES] = if (isFavorite) {
                current.toSet() + "$poseId$FAVORITE_SEPARATOR${System.currentTimeMillis()}"
            } else {
                current.toSet()
            }
        }
    }

    private fun String.toFavoriteEntry(): Pair<Int, Long>? {
        val parts = split(FAVORITE_SEPARATOR)
        if (parts.size != 2) return null
        val poseId = parts[0].toIntOrNull() ?: return null
        val savedAtMillis = parts[1].toLongOrNull() ?: return null
        return poseId to savedAtMillis
    }

    private companion object {
        const val PREFERENCES_NAME = "pose_match_prefs"
        const val DEFAULT_LANGUAGE_CODE = "en"
        const val FAVORITE_SEPARATOR = ":"
    }
}
