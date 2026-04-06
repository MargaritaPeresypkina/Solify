package com.example.solify.domain.usecases.auth

import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import javax.inject.Inject

class RefreshCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<User> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(IllegalStateException("Not logged in"))

            val user = userRepository.getUserById(userId).getOrNull()
                ?: return Result.failure(IllegalStateException("User not found"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}