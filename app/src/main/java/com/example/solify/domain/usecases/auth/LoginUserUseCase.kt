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
                val authResult = userRepository.loginUser(email, password)

                authResult.onSuccess { user ->
                    sessionManager.saveUserId(user.id)
                }

                return@withContext authResult
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}