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
import kotlinx.serialization.json.Json
import java.io.IOException

private const val RECENT_SEARCH_KEYWORD_DATA_STORE_FILE_NAME = "oguri_recent_search_keyword.preferences_pb"
private const val KEY_RECENT_SEARCH_KEYWORDS = "key_recent_search_keywords"

@Volatile
private var recentSearchKeywordLocalDataSourceInstance: RecentSearchKeywordLocalDataSource? = null

private class AndroidRecentSearchKeywordLocalDataSource(
    private val applicationContext: Context,
) : RecentSearchKeywordLocalDataSource {
    private val recentSearchKeywordPreferenceKey = stringPreferencesKey(KEY_RECENT_SEARCH_KEYWORDS)
    private val dataStoreCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json

    @Volatile
    private var recentSearchKeywordDataStoreFlow: Flow<Preferences>? = null

    @Volatile
    private var recentSearchKeywordDataStore: DataStore<Preferences>? = null

    @Synchronized
    override fun initialize() {
        if (recentSearchKeywordDataStore != null && recentSearchKeywordDataStoreFlow != null) {
            return
        }
        val dataStore =
            PreferenceDataStoreFactory.create(
                corruptionHandler = null,
                migrations = emptyList(),
                scope = dataStoreCoroutineScope,
            ) {
                applicationContext.preferencesDataStoreFile(RECENT_SEARCH_KEYWORD_DATA_STORE_FILE_NAME)
            }
        recentSearchKeywordDataStore = dataStore
        recentSearchKeywordDataStoreFlow =
            dataStore.data.catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
    }

    override fun readRecentSearchKeywords(): List<String> {
        initialize()
        val preferencesFlow = recentSearchKeywordDataStoreFlow ?: return emptyList()
        return runBlocking(Dispatchers.IO) {
            val encodedKeywords = preferencesFlow.first()[recentSearchKeywordPreferenceKey] ?: return@runBlocking emptyList()
            runCatching {
                json.decodeFromString<List<String>>(encodedKeywords)
            }.getOrDefault(emptyList())
        }
    }

    override fun writeRecentSearchKeywords(keywords: List<String>) {
        initialize()
        val dataStore = recentSearchKeywordDataStore ?: return
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[recentSearchKeywordPreferenceKey] = json.encodeToString(keywords)
            }
        }
    }
}

actual fun provideRecentSearchKeywordLocalDataSource(): RecentSearchKeywordLocalDataSource {
    val existingInstance = recentSearchKeywordLocalDataSourceInstance
    if (existingInstance != null) {
        return existingInstance
    }

    val applicationContext =
        OguriPlatformContextHolder.applicationContext
            ?: error("Application context is not initialized for recent search keyword local data source.")
    val newInstance = AndroidRecentSearchKeywordLocalDataSource(applicationContext = applicationContext)
    recentSearchKeywordLocalDataSourceInstance = newInstance
    return newInstance
}
