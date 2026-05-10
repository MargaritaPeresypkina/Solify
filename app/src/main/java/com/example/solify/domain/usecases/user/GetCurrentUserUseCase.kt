package com.example.solify.domain.usecases.user

import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<User> {
        val userId = sessionManager.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("Not logged in"))

        return userRepository.getUserById(userId)
    }
}