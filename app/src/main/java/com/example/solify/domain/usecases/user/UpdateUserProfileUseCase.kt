package com.example.solify.domain.usecases.user

import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        name: String,
        surname: String,
        email: String
    ): Result<User> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(IllegalStateException("Not logged in"))
            return userRepository.updateUser(userId, name, surname, email)
        } catch (e: Exception) {
            Result.failure(Exception("Profile update failed: ${e.message}"))
        }
    }
}