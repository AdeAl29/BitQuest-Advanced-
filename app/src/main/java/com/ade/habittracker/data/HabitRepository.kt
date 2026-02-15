package com.ade.habittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ade.habittracker.model.AppData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Inisialisasi DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_prefs")

class HabitRepository(private val context: Context) {

    private val LEGACY_APP_DATA_KEY = stringPreferencesKey("app_data_json")
    private val ACTIVE_ACCOUNT_ID_KEY = stringPreferencesKey("active_account_id")

    val appData: Flow<AppData?> = context.dataStore.data
        .map { preferences ->
            val activeAccountId = resolveActiveAccountId(preferences)
            val accountJson = preferences[accountDataKey(activeAccountId)]
            val legacyJson = preferences[LEGACY_APP_DATA_KEY]
            val jsonString = accountJson ?: legacyJson

            if (jsonString != null) {
                AppData.fromJson(jsonString)
            } else {
                AppData() // Buat AppData default
            }
        }

    suspend fun getCurrentAppData(): AppData {
        val preferences = context.dataStore.data.first()
        val activeAccountId = resolveActiveAccountId(preferences)
        val accountJson = preferences[accountDataKey(activeAccountId)]
        val legacyJson = preferences[LEGACY_APP_DATA_KEY]
        return when {
            accountJson != null -> AppData.fromJson(accountJson)
            legacyJson != null -> AppData.fromJson(legacyJson)
            else -> AppData()
        }
    }

    suspend fun switchActiveAccount(accountId: String?) {
        val resolvedAccountId = accountId
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: GUEST_ACCOUNT_ID

        context.dataStore.edit { preferences ->
            preferences[ACTIVE_ACCOUNT_ID_KEY] = resolvedAccountId
            migrateLegacyDataIfNeeded(preferences, resolvedAccountId)
        }
    }

    suspend fun saveAppData(appData: AppData) {
        val jsonString = appData.toJson()
        context.dataStore.edit { preferences ->
            val activeAccountId = resolveActiveAccountId(preferences)
            migrateLegacyDataIfNeeded(preferences, activeAccountId)
            preferences[accountDataKey(activeAccountId)] = jsonString
        }
    }

    private fun resolveActiveAccountId(preferences: Preferences): String {
        val firebaseAccountId = getCurrentFirebaseAccountId()
        if (firebaseAccountId != null) {
            return firebaseAccountId
        }

        val savedAccountId = preferences[ACTIVE_ACCOUNT_ID_KEY]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        if (savedAccountId != null) return savedAccountId
        return GUEST_ACCOUNT_ID
    }

    private fun getCurrentFirebaseAccountId(): String? {
        return runCatching { FirebaseAuth.getInstance().currentUser?.uid }
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun accountDataKey(accountId: String): Preferences.Key<String> {
        return stringPreferencesKey("app_data_json_account_${sanitizeAccountId(accountId)}")
    }

    private fun sanitizeAccountId(accountId: String): String {
        return accountId.map { ch ->
            if (ch.isLetterOrDigit() || ch == '_') ch else '_'
        }.joinToString("")
    }

    private fun migrateLegacyDataIfNeeded(preferences: MutablePreferences, activeAccountId: String) {
        if (activeAccountId == GUEST_ACCOUNT_ID) return
        val legacyJson = preferences[LEGACY_APP_DATA_KEY] ?: return
        val accountKey = accountDataKey(activeAccountId)
        if (preferences[accountKey].isNullOrBlank()) {
            preferences[accountKey] = legacyJson
        }
        preferences.remove(LEGACY_APP_DATA_KEY)
    }

    companion object {
        const val GUEST_ACCOUNT_ID = "guest_local"
    }
}
