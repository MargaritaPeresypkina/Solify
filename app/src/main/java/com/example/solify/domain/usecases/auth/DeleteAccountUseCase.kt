package com.example.solify.domain.usecases.auth

import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import com.example.solify.domain.utils.verifyPassword
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(currentPassword: String): Result<Boolean> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(IllegalStateException("Not logged in"))

            val user = userRepository.getUserById(userId).getOrNull()
                ?: return Result.failure(IllegalStateException("User not found del"))

            if (!verifyPassword(currentPassword, user.passwordHash)) {
                return Result.success(false)
            }

            userRepository.deleteUser(userId).getOrNull()
                ?: return Result.failure(Exception("Failed to delete account"))

            sessionManager.clear()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Account deletion failed: ${e.message}"))
        }
    }
}

