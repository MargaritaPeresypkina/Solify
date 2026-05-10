package com.example.solify.domain.usecases.auth

import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(currentPassword: String): Result<Boolean> {
        val userId = sessionManager.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("Not logged in"))

<<<<<<< Updated upstream
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
=======
        val result = userRepository.deleteUser(userId, currentPassword)

        if (result.isSuccess && result.getOrNull() == true) {
            sessionManager.clear()
>>>>>>> Stashed changes
        }

        return result
    }
}

