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
        email: String,
        password: String? = null
    ): Result<User> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(IllegalStateException("Not logged in"))

            val currentUser = userRepository.getUserById(userId).getOrNull()
            if (currentUser?.email != email) {
                val isEmailExists = userRepository.isEmailExists(
                    email = email,
                    excludeUserId = userId
                ).getOrThrow()

                if (isEmailExists) {
                    return Result.failure(IllegalArgumentException("Email address already exists."))
                }
            }

            userRepository.updateUser(userId, name, surname, email, password)
        } catch (e: Exception) {
            Result.failure(Exception("Profile update failed: ${e.message}"))
        }
    }
}