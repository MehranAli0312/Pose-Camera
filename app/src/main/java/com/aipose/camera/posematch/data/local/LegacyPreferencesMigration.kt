package com.aipose.camera.posematch.data.local

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey

object LegacyPreferencesMigration : DataMigration<Preferences> {

    private val LEGACY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val LEGACY_SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    private val LEGACY_RETAIN_SKELETON = booleanPreferencesKey("retain_skeleton")

    private val LEGACY_LANGUAGE_CODES = mapOf(
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

    private val LEGACY_KEYS = listOf(
        LEGACY_ONBOARDING_COMPLETED,
        LEGACY_SELECTED_LANGUAGE,
        LEGACY_RETAIN_SKELETON
    )

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        if (currentData[AppPreferencesKeys.LEGACY_PREFERENCES_IMPORTED] == true) return false
        return LEGACY_KEYS.any { currentData.contains(it) }
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val migrated = currentData.mutableCopy()

        currentData[LEGACY_ONBOARDING_COMPLETED]?.let { completed ->
            migrated[AppPreferencesKeys.IS_ON_SPLASH_FIRST_RUN] = !completed
        }
        currentData[LEGACY_SELECTED_LANGUAGE]?.let { languageName ->
            LEGACY_LANGUAGE_CODES[languageName]?.let { code ->
                migrated[AppPreferencesKeys.LANGUAGE] = code
            }
        }
        currentData[LEGACY_RETAIN_SKELETON]?.let { retain ->
            migrated[AppPreferencesKeys.KEEP_POSE_OVERLAY] = retain
        }
        migrated[AppPreferencesKeys.LEGACY_PREFERENCES_IMPORTED] = true

        return migrated.toPreferences()
    }

    override suspend fun cleanUp() = Unit

    @Suppress("UNCHECKED_CAST")
    private fun Preferences.mutableCopy(): MutablePreferences {
        val copy = mutablePreferencesOf()
        asMap().forEach { (key, value) -> copy[key as Preferences.Key<Any>] = value }
        return copy
    }
}
