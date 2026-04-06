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
        name: String?,
        surname: String?,
        email: String?
    ): Result<User> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(IllegalStateException("Not logged in"))

            val currentUser = userRepository.getUserById(userId).getOrNull()
                ?: return Result.failure(IllegalStateException("User not found"))

            if (email != null && email != currentUser.email) {
                val emailExists = userRepository.isEmailExists(email).getOrNull() ?: false
                if (emailExists) {
                    return Result.failure(IllegalArgumentException("Email already in use"))
                }
            }

            val updatedUser = User(
                id = currentUser.id,
                name = name ?: currentUser.name,
                surname = surname ?: currentUser.surname,
                email = email ?: currentUser.email,
                avatarUrl = currentUser.avatarUrl,
                passwordHash = currentUser.passwordHash
            )

            userRepository.updateUser(updatedUser).getOrNull()
                ?: return Result.failure(Exception("Failed to save profile changes"))

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception("Profile update failed: ${e.message}"))
        }
    }
}