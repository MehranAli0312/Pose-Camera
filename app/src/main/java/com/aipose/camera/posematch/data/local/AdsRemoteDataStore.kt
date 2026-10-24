package com.aipose.camera.posematch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.adsRemoteDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ads_remote_config",
)

class AdsRemoteDataStore(context: Context) {

    private val dataStore = context.applicationContext.adsRemoteDataStore

    suspend fun getString(key: String, default: String): String =
        dataStore.data.first()[stringPreferencesKey(key)] ?: default

    suspend fun putString(key: String, value: String) {
        dataStore.edit { preferences -> preferences[stringPreferencesKey(key)] = value }
    }
}
