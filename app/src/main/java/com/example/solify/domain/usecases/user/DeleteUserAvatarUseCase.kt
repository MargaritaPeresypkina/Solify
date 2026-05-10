package com.example.solify.domain.usecases.user

import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import javax.inject.Inject

class DeleteUserAvatarUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(IllegalStateException("Not logged in"))

            userRepository.deleteUserAvatar(userId)
        } catch (e: Exception) {
            Result.failure(Exception("Avatar deletion failed: ${e.message}"))
        }
    }
}