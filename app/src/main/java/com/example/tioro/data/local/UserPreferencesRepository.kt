package com.example.tioro.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    val userNameFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    private val NOTIFICATIONS_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("notifications_enabled")
    
    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true // true por defecto
    }

    private val LAST_RECAP_WEEK_KEY = stringPreferencesKey("last_recap_week")

    val lastRecapWeekFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LAST_RECAP_WEEK_KEY]
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }
    
    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveLastRecapWeek(weekMondayDate: String) {
        dataStore.edit { preferences ->
            preferences[LAST_RECAP_WEEK_KEY] = weekMondayDate
        }
    }
}
