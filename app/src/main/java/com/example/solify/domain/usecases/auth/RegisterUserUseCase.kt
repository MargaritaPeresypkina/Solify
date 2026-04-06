package com.example.solify.domain.usecases.auth

import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import com.example.solify.domain.utils.hashPassword
import java.util.UUID
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        name: String,
        surname: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val existingUser = userRepository.getUserByEmail(email).getOrNull()
            if (existingUser != null) {
                return Result.failure(IllegalArgumentException("Email already registered"))
            }

            if (password.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            }

            val passwordHash = hashPassword(password)

            val user = User(
                id = UUID.randomUUID().toString(),
                name = name,
                surname = surname,
                email = email,
                passwordHash = passwordHash,
                avatarUrl = null
            )

            userRepository.registerUser(user).getOrNull()
                ?: return Result.failure(Exception("Failed to save user"))

            sessionManager.saveUserId(user.id)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed: ${e.message}"))
        }
    }
}