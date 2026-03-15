package com.bigong.oguri.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.bigong.oguri.core.platform.OguriPlatformContextHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException

private const val TOKEN_DATA_STORE_FILE_NAME: String = "oguri_token.preferences_pb"
private const val KEY_ACCESS_TOKEN: String = "key_access_token"
private const val KEY_REFRESH_TOKEN: String = "key_refresh_token"

private object AndroidTokenLocalDataSource : TokenLocalDataSource {
    private val accessTokenPreferenceKey = stringPreferencesKey(KEY_ACCESS_TOKEN)
    private val refreshTokenPreferenceKey = stringPreferencesKey(KEY_REFRESH_TOKEN)
    private val dataStoreCoroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var tokenDataStoreFlow: Flow<Preferences>? = null

    @Volatile
    private var tokenDataStore: DataStore<Preferences>? = null

    @Synchronized
    override fun initialize() {
        if (tokenDataStore != null && tokenDataStoreFlow != null) {
            return
        }
        val applicationContext: Context = OguriPlatformContextHolder.applicationContext ?: return
        val dataStore =
            PreferenceDataStoreFactory.create(
                corruptionHandler = null,
                migrations = emptyList(),
                scope = dataStoreCoroutineScope,
            ) {
                applicationContext.preferencesDataStoreFile(TOKEN_DATA_STORE_FILE_NAME)
            }
        tokenDataStore = dataStore
        tokenDataStoreFlow =
            dataStore.data.catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
    }

    override fun readAccessToken(): String? {
        initialize()
        val preferencesFlow: Flow<Preferences> = tokenDataStoreFlow ?: return null
        return runBlocking(Dispatchers.IO) {
            preferencesFlow.first()[accessTokenPreferenceKey]
        }
    }

    override fun readRefreshToken(): String? {
        initialize()
        val preferencesFlow: Flow<Preferences> = tokenDataStoreFlow ?: return null
        return runBlocking(Dispatchers.IO) {
            preferencesFlow.first()[refreshTokenPreferenceKey]
        }
    }

    override fun writeTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        initialize()
        val dataStore = tokenDataStore ?: return
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[accessTokenPreferenceKey] = accessToken
                preferences[refreshTokenPreferenceKey] = refreshToken
            }
        }
    }

    override fun clearTokens() {
        initialize()
        val dataStore = tokenDataStore ?: return
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences.remove(accessTokenPreferenceKey)
                preferences.remove(refreshTokenPreferenceKey)
            }
        }
    }
}

actual fun provideTokenLocalDataSource(): TokenLocalDataSource = AndroidTokenLocalDataSource
