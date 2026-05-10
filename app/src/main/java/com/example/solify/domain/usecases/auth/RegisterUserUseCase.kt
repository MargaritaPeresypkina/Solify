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
        val result = userRepository.registerUser(email, password, name, surname)

        result.onSuccess { user ->
            sessionManager.saveUserId(user.id)
        }

        return result
    }
}