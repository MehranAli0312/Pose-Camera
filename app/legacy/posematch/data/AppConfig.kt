package com.aipose.camera.posematch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.aipose.camera.posematch.data.repository.AppRepositoryImpl
import com.aipose.camera.posematch.data.repository.IAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pose_match_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
        val KEY_RETAIN_SKELETON = booleanPreferencesKey("retain_skeleton")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_LANGUAGE] ?: "English"
    }

    val appTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_THEME] ?: "Dark"
    }

    val retainSkeleton: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_RETAIN_SKELETON] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_LANGUAGE] = language
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_THEME] = theme
        }
    }

    suspend fun setRetainSkeleton(retain: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RETAIN_SKELETON] = retain
        }
    }
}

/** Pose template as stored in assets/default_poses.json. */
@Serializable
data class PoseItem(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val difficulty: String,
    val tags: List<String>,
    val image: String,
    val landmarks: Map<String, Float>
)

object DefaultPosesProvider {
    // ignoreUnknownKeys so a future key added to default_poses.json does not
    // start throwing before PoseItem is updated to match.
    private val json = Json { ignoreUnknownKeys = true }

    fun loadDefaultPoses(context: Context): List<PoseItem> {
        try {
            val jsonString = context.assets.open("default_poses.json").bufferedReader().use { it.readText() }
            return json.decodeFromString<List<PoseItem>>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }
    }
}

/** Manual DI container: owns the singletons the app graph is built from. */
class AppContainer(private val context: Context) {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    private val preferencesManager: PreferencesManager by lazy { PreferencesManager(context) }
    val repository: IAppRepository by lazy {
        AppRepositoryImpl(
            appDao = database.appDao(),
            preferencesManager = preferencesManager,
            context = context
        )
    }
}
