package com.example.solify.domain.usecases.auth

import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import com.example.solify.domain.sync.UserDataSyncCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val userDataSyncCoordinator: UserDataSyncCoordinator
) {
    suspend operator fun invoke(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userRepository.logoutUser().getOrThrow()
                sessionManager.clear()
                userDataSyncCoordinator.reset()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Logout failed: ${e.message}", e))
            }
        }
    }
}
