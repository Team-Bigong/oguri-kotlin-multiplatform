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
import com.bigong.oguri.domain.model.DisplayThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException

private const val DISPLAY_THEME_MODE_DATA_STORE_FILE_NAME = "oguri_display_theme_mode.preferences_pb"
private const val KEY_DISPLAY_THEME_MODE = "key_display_theme_mode"

@Volatile
private var displayThemeModeLocalDataSourceInstance: DisplayThemeModeLocalDataSource? = null

private class AndroidDisplayThemeModeLocalDataSource(
    private val applicationContext: Context,
) : DisplayThemeModeLocalDataSource {
    private val displayThemeModePreferenceKey = stringPreferencesKey(KEY_DISPLAY_THEME_MODE)
    private val dataStoreCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var displayThemeModeDataStoreFlow: Flow<Preferences>? = null

    @Volatile
    private var displayThemeModeDataStore: DataStore<Preferences>? = null

    @Synchronized
    override fun initialize() {
        if (displayThemeModeDataStore != null && displayThemeModeDataStoreFlow != null) {
            return
        }
        val dataStore =
            PreferenceDataStoreFactory.create(
                corruptionHandler = null,
                migrations = emptyList(),
                scope = dataStoreCoroutineScope,
            ) {
                applicationContext.preferencesDataStoreFile(DISPLAY_THEME_MODE_DATA_STORE_FILE_NAME)
            }
        displayThemeModeDataStore = dataStore
        displayThemeModeDataStoreFlow =
            dataStore.data.catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
    }

    override fun readDisplayThemeMode(): DisplayThemeMode? {
        initialize()
        val preferencesFlow = displayThemeModeDataStoreFlow ?: return null
        val displayThemeModeName =
            runBlocking(Dispatchers.IO) {
                preferencesFlow.first()[displayThemeModePreferenceKey]
            }

        if (displayThemeModeName.isNullOrBlank()) {
            return null
        }

        return runCatching { DisplayThemeMode.valueOf(displayThemeModeName) }.getOrNull()
    }

    override fun writeDisplayThemeMode(displayThemeMode: DisplayThemeMode) {
        initialize()
        val dataStore = displayThemeModeDataStore ?: return
        runBlocking(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[displayThemeModePreferenceKey] = displayThemeMode.name
            }
        }
    }
}

actual fun provideDisplayThemeModeLocalDataSource(): DisplayThemeModeLocalDataSource {
    val existingInstance = displayThemeModeLocalDataSourceInstance
    if (existingInstance != null) {
        return existingInstance
    }

    val applicationContext =
        OguriPlatformContextHolder.applicationContext
            ?: error(
                "Application context is not initialized for display theme mode local data source.",
            )
    val newInstance = AndroidDisplayThemeModeLocalDataSource(applicationContext = applicationContext)
    displayThemeModeLocalDataSourceInstance = newInstance
    return newInstance
}
