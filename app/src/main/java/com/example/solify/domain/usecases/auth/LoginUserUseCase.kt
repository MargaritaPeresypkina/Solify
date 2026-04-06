package com.example.solify.domain.usecases.auth

import com.example.solify.domain.utils.verifyPassword
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userRepository.getUserByEmail(email).getOrNull()
                    ?: return@withContext Result.failure(IllegalArgumentException("User not found"))

                if (!verifyPassword(password, user.passwordHash)) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid password"))
                }

                sessionManager.saveUserId(user.id)
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}