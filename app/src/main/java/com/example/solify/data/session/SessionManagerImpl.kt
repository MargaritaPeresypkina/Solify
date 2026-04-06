package com.example.solify.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.solify.domain.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): SessionManager {
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    //Save Id current user
    override suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    //Get Flow with Id current user
    override fun getUserIdFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    // Get Id current user
    override suspend fun getCurrentUserId(): String? {
        return withContext(Dispatchers.IO) {
            dataStore.data.map { preferences ->
                preferences[USER_ID_KEY]
            }.firstOrNull()
        }
    }

    // Check user auth
    fun isLoggedIn(): Flow<Boolean> {
        return getUserIdFlow().map { it != null }
    }

    // Clear session
    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
}