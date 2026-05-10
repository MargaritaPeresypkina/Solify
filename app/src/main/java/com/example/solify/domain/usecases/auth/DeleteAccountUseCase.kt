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

        val result = userRepository.deleteUser(userId, currentPassword)

        if (result.isSuccess && result.getOrNull() == true) {
            sessionManager.clear()
        }

        return result
    }
}

