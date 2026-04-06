package com.example.solify.domain.usecases.auth

import com.example.solify.domain.session.SessionManager
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            sessionManager.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Logout failed: ${e.message}"))
        }
    }
}