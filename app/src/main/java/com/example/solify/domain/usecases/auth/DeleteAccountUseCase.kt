package com.example.solify.domain.usecases.auth

import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import com.example.solify.domain.sync.UserDataSyncCoordinator
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val userDataSyncCoordinator: UserDataSyncCoordinator
) {
    suspend operator fun invoke(currentPassword: String): Result<Boolean> {
        val userId = sessionManager.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("Not logged in"))

        val result = userRepository.deleteUser(userId, currentPassword)

        if (result.isSuccess && result.getOrNull() == true) {
            sessionManager.clear()
            userDataSyncCoordinator.reset()
        }

        return result
    }
}

