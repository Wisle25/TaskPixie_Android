package com.example.taskpixie.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private const val TAG = "UserPreferences"
        val ACCESS_TOKEN = stringPreferencesKey("ACCESS_TOKEN")
        val REFRESH_TOKEN = stringPreferencesKey("REFRESH_TOKEN")
        val IDENTITY = stringPreferencesKey("IDENTITY")
        val PASSWORD = stringPreferencesKey("PASSWORD")
        val USER_ID = stringPreferencesKey("USER_ID")
        val USERNAME = stringPreferencesKey("USERNAME")
    }

    val accessToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    val identity: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[IDENTITY]
        }

    val password: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PASSWORD]
        }

    val userId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME]
        }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveCredentials(identity: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[IDENTITY] = identity
            preferences[PASSWORD] = password
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }

    private suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = ""
            preferences[REFRESH_TOKEN] = ""
        }
    }

    private suspend fun clearCredentials() {
        context.dataStore.edit { preferences ->
            preferences[IDENTITY] = ""
            preferences[PASSWORD] = ""
        }
    }

    private suspend fun clearUserId() {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = ""
        }
    }

    private suspend fun clearUsername() {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = ""
        }
    }

    suspend fun clear() {
        clearUsername()
        clearCredentials()
        clearTokens()
        clearUserId()
    }
}
