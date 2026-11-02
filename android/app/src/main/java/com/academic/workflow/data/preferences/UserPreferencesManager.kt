package com.academic.workflow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AI_MODEL = stringPreferencesKey("preferred_ai_model")
        private val KEY_ATTENDANCE_TARGET = floatPreferencesKey("attendance_target")
        private val KEY_API_BASE_URL = stringPreferencesKey("api_base_url")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        
        const val DEFAULT_BASE_URL = "http://127.0.0.1:8000"
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME]
    }

    val preferredAiModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AI_MODEL] ?: "gpt-4o"
    }

    val attendanceTarget: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_ATTENDANCE_TARGET] ?: 75.0f
    }

    val apiBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_BASE_URL] ?: DEFAULT_BASE_URL
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_DARK_MODE] = enabled }
    }

    suspend fun setUserInfo(email: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_NAME] = name
        }
    }

    suspend fun setPreferredAiModel(model: String) {
        context.dataStore.edit { prefs -> prefs[KEY_AI_MODEL] = model }
    }

    suspend fun setAttendanceTarget(target: Float) {
        context.dataStore.edit { prefs -> prefs[KEY_ATTENDANCE_TARGET] = target }
    }

    suspend fun setApiBaseUrl(url: String) {
        context.dataStore.edit { prefs -> prefs[KEY_API_BASE_URL] = url }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_NAME)
        }
    }
}
