package com.example.solify.domain.usecases.auth

import com.example.solify.domain.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSessionStatusUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Flow<AuthState> =
        sessionManager.getUserIdFlow().map { userId ->
            if (userId != null) AuthState.Authorized else AuthState.Unauthorized
        }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Authorized : AuthState()
    data object Unauthorized : AuthState()
}