package com.example.solify.domain.usecases.auth

import android.util.Log
import com.example.solify.domain.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetSessionStatusUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Flow<AuthState> = flow {
        try {
            Log.d("GetSessionStatusUseCase", "Start GetSessionStatusUseCase")
            val userId = sessionManager.getCurrentUserId()
            Log.d("GetSessionStatusUseCase", "userId $userId")
            if (userId != null) {
                Log.d("GetSessionStatusUseCase", "AuthState.Authorized()")
                emit(AuthState.Authorized)
            } else {
                Log.d("GetSessionStatusUseCase", "AuthState.Unauthorized 1")
                emit(AuthState.Unauthorized)
            }
        } catch (e: Exception) {
            Log.d("GetSessionStatusUseCase", "catch $e")
            emit(AuthState.Unauthorized)
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authorized : AuthState()
    object Unauthorized : AuthState()
}