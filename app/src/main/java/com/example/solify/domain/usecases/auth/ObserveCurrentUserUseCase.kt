package com.example.solify.domain.usecases.auth

import com.example.solify.domain.entities.user.User
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    private var cachedFlow: StateFlow<User?>? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(scope: CoroutineScope): StateFlow<User?> {
        if (cachedFlow == null) {
            cachedFlow = sessionManager.getUserIdFlow().flatMapLatest { userId ->
                if (userId != null) {
                    userRepository.observeUserById(userId)
                } else {
                    flowOf(null)
                }
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
        }
        return cachedFlow!!
    }
}