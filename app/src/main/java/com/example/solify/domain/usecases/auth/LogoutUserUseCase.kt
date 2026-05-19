package com.example.solify.domain.usecases.auth

import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userRepository.logoutUser().getOrThrow()
                sessionManager.clear()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Logout failed: ${e.message}", e))
            }
        }
    }
}
