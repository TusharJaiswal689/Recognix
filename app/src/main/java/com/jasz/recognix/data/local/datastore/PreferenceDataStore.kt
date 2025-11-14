package com.jasz.recognix.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recognix_preferences")

@Singleton
class PreferenceDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val SCAN_FOLDER_PATH = stringPreferencesKey("scan_folder_path")
    }

    val scanFolderPath: Flow<String?> = context.dataStore.data
        .map {
            it[PreferencesKeys.SCAN_FOLDER_PATH]
        }

    suspend fun setScanFolderPath(path: String?) {
        context.dataStore.edit {
            if (path == null) {
                it.remove(PreferencesKeys.SCAN_FOLDER_PATH)
            } else {
                it[PreferencesKeys.SCAN_FOLDER_PATH] = path
            }
        }
    }

    suspend fun getLastScanTimestamp(folderPath: String): Long {
        val key = longPreferencesKey(folderPath)
        return context.dataStore.data.first()[key] ?: 0L
    }

    suspend fun setLastScanTimestamp(folderPath: String, timestamp: Long) {
        val key = longPreferencesKey(folderPath)
        context.dataStore.edit {
            it[key] = timestamp
        }
    }
}
