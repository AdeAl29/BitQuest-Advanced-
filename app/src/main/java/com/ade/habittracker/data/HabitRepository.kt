package com.ade.habittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ade.habittracker.model.AppData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

private val Context.localDataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_preferences")
private val Context.cachedFirebaseStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_prefs")

class HabitRepository(private val context: Context) {

    private val appDataJsonKey = stringPreferencesKey("app_data_json")
    private val cachedActiveAccountIdKey = stringPreferencesKey("active_account_id")

    val appData: Flow<AppData?> = context.localDataStore.data
        .map { preferences ->
            preferences[appDataJsonKey]
                ?.takeIf { it.isNotBlank() }
                ?.let { AppData.fromJson(it) }
                ?.let(::normalizeLocalData)
                ?: defaultLocalData()
        }
        .onStart {
            emit(getCurrentAppData())
        }
        .distinctUntilChanged()

    suspend fun getCurrentAppData(): AppData {
        readLocalAppData()?.let { return it }

        val migrated = readLegacyCachedAppData()
        val resolved = normalizeLocalData(migrated ?: AppData())
        writeLocalAppData(resolved)
        return resolved
    }

    suspend fun saveAppData(appData: AppData) {
        writeLocalAppData(normalizeLocalData(appData))
    }

    private suspend fun readLocalAppData(): AppData? {
        val json = context.localDataStore.data.first()[appDataJsonKey]
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return AppData.fromJson(json).let(::normalizeLocalData)
    }

    private suspend fun readLegacyCachedAppData(): AppData? {
        val preferences = context.cachedFirebaseStore.data.first()
        val savedAccountId = preferences[cachedActiveAccountIdKey]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val candidateJson = listOfNotNull(
            savedAccountId?.let { preferences[accountDataKey(it)] },
            preferences[accountDataKey("guest_local")],
            preferences[appDataJsonKey],
            preferences.asMap().entries
                .firstOrNull { entry ->
                    entry.key.name.startsWith("app_data_json_account_") && entry.value is String
                }
                ?.value as? String
        ).firstOrNull { !it.isNullOrBlank() }

        return candidateJson
            ?.takeIf { it.isNotBlank() }
            ?.let { AppData.fromJson(it) }
    }

    private suspend fun writeLocalAppData(appData: AppData) {
        context.localDataStore.edit { preferences ->
            preferences[appDataJsonKey] = appData.toJson()
        }
    }

    private fun normalizeLocalData(appData: AppData): AppData {
        return appData.copy(isLoggedIn = true)
    }

    private fun defaultLocalData(): AppData = AppData(isLoggedIn = true)

    private fun accountDataKey(accountId: String): Preferences.Key<String> {
        val sanitized = accountId.map { ch ->
            if (ch.isLetterOrDigit() || ch == '_') ch else '_'
        }.joinToString("")
        return stringPreferencesKey("app_data_json_account_$sanitized")
    }
}
