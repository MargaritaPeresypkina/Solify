package com.example.solify.domain.usecases.user

import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import javax.inject.Inject

class CheckEmailAvailabilityUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String): Result<Boolean> {
        return try {
            val currentUserId = sessionManager.getCurrentUserId()
            val isEmailExists = userRepository.isEmailExists(
                email = email,
                excludeUserId = currentUserId
            ).getOrNull() ?: false
            
            Result.success(!isEmailExists)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check email availability: ${e.message}"))
        }
    }
}