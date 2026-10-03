package com.aipose.camera.posematch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.adsRemoteDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ads_remote_config",
)

class AdsRemoteDataStore(context: Context) {

    private val dataStore = context.applicationContext.adsRemoteDataStore

    suspend fun getBoolean(key: String, default: Boolean): Boolean =
        read(booleanPreferencesKey(key)) ?: default

    suspend fun getLong(key: String, default: Long): Long =
        read(longPreferencesKey(key)) ?: default

    suspend fun getString(key: String, default: String): String =
        read(stringPreferencesKey(key)) ?: default

    suspend fun putBoolean(key: String, value: Boolean) {
        write(booleanPreferencesKey(key), value)
    }

    suspend fun putLong(key: String, value: Long) {
        write(longPreferencesKey(key), value)
    }

    suspend fun putString(key: String, value: String) {
        write(stringPreferencesKey(key), value)
    }

    private suspend fun <T> read(key: Preferences.Key<T>): T? = dataStore.data.first()[key]

    private suspend fun <T> write(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences -> preferences[key] = value }
    }
}
