package com.aipose.camera.posematch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.aipose.camera.posematch.data.repository.AppRepositoryImpl
import com.aipose.camera.posematch.data.repository.IAppRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

// 1. DataStore Extension for Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pose_match_prefs")

// 2. Preferences Manager
class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
        val KEY_RETAIN_SKELETON = booleanPreferencesKey("retain_skeleton")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val appTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_THEME] ?: "Dark" // Premium Photography dark mode as default
    }

    val retainSkeleton: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_RETAIN_SKELETON] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
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

// 3. Pose Data landmark structure matching JSON
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

// Helper to load poses from assets
object DefaultPosesProvider {
    fun loadDefaultPoses(context: Context): List<PoseItem> {
        try {
            val jsonString = context.assets.open("default_poses.json").bufferedReader().use { it.readText() }
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, PoseItem::class.java)
            val adapter = moshi.adapter<List<PoseItem>>(listType)
            return adapter.fromJson(jsonString) ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }
    }
}

// 4. Monetization Architecture (AdManager Interface & Implementations)
interface AdManager {
    fun loadNativeAd(location: String)
    fun showInterstitialAd(onAdDismissed: () -> Unit)
    fun showRewardedAd(onRewardEarned: () -> Unit)
    fun isAdLoaded(location: String): Boolean
}

class ProductionAdManager : AdManager {
    private val loadedAds = mutableSetOf<String>()

    override fun loadNativeAd(location: String) {
        // Ready for easy AdMob Integration, simulating load
        loadedAds.add(location)
    }

    override fun showInterstitialAd(onAdDismissed: () -> Unit) {
        // Placeholder for Interstitial transitions
        onAdDismissed()
    }

    override fun showRewardedAd(onRewardEarned: () -> Unit) {
        // Placeholder for rewarded video unlocks
        onRewardEarned()
    }

    override fun isAdLoaded(location: String): Boolean {
        return true
    }
}

// 5. AppContainer (Central DI Container for clean MVVM Architecture)
class AppContainer(val context: Context) {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(context) }
    val adManager: AdManager by lazy { ProductionAdManager() }
    val repository: IAppRepository by lazy {
        AppRepositoryImpl(
            appDao = database.appDao(),
            preferencesManager = preferencesManager,
            context = context
        )
    }
}
