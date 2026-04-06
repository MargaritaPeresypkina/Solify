package com.example.solify.domain.session

import kotlinx.coroutines.flow.Flow

interface SessionManager {

    suspend fun saveUserId(userId: String)

    fun getUserIdFlow(): Flow<String?>

    suspend fun getCurrentUserId(): String?

    suspend fun clear()
}