package com.example.solify.presentation.screens.profile

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.R
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.usecases.auth.LogoutUserUseCase
import com.example.solify.domain.entities.progress.TestsCompletionOverview
import com.example.solify.domain.entities.progress.WeeklyActivityDay
import com.example.solify.domain.usecases.progress.ObserveTestsCompletionPercentUseCase
import com.example.solify.domain.usecases.progress.ObserveWeeklyActivityUseCase
import com.example.solify.domain.usecases.user.GetUserBadgeUseCase
import com.example.solify.domain.usecases.user.ObserveCurrentUserUseCase
import com.example.solify.domain.usecases.user.UpdateUserAvatarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    private val getUserBadgeUseCase: GetUserBadgeUseCase,
    private val observeWeeklyActivityUseCase: ObserveWeeklyActivityUseCase,
    private val observeTestsCompletionPercentUseCase: ObserveTestsCompletionPercentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val userFlow = observeCurrentUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val weeklyActivityFlow = userFlow
        .flatMapLatest { user ->
            if (user == null) {
                emptyFlow()
            } else {
                observeWeeklyActivityUseCase(user.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val testsCompletionFlow = userFlow
        .flatMapLatest { user ->
            if (user == null) {
                emptyFlow()
            } else {
                observeTestsCompletionPercentUseCase(user.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TestsCompletionOverview()
        )

    init {
        viewModelScope.launch {
            weeklyActivityFlow.collect { days ->
                _uiState.update { it.copy(weeklyActivityDays = days) }
            }
        }

        viewModelScope.launch {
            testsCompletionFlow.collect { overview ->
                _uiState.update {
                    it.copy(
                        testsCompletionPercent = overview.percent,
                        completedTestsCount = overview.completedTests
                    )
                }
            }
        }

        viewModelScope.launch {
            userFlow.collect { user ->
                if (user == null && _uiState.value.user != null) {
                    _uiState.update {
                        it.copy(
                            user = null,
                            isLoading = false,
                            isProfileReady = false,
                            isLoggedOut = true
                        )
                    }
                } else if (user != null) {
                    _uiState.update {
                        it.copy(
                            user = user,
                            isProfileReady = false
                        )
                    }
                    refreshBadge(user.id)
                }
            }

        }
    }

    private fun refreshBadge(userId: String) {
        viewModelScope.launch {
            getUserBadgeUseCase(userId).let { result ->
                _uiState.update {
                    it.copy(
                        userBadgeRes = result.badgeRes,
                        userLevel = result.levelName,
                        isProfileReady = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun uploadAvatarImage(imageUri: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            withContext(Dispatchers.IO) {
                updateUserAvatarUseCase(imageUri)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                error = error.message ?: "Failed to upload avatar",
                                isLoading = false
                            )
                        }
                    }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) {
                val result = logoutUserUseCase()
                result.onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            user = null,
                            isLoading = false,
                            isLoggedOut = true
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = error.message ?: "Logout failed",
                        )
                    }
                }
            }
        }
    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            is ProfileCommand.UploadAvatarImage -> uploadAvatarImage(command.imageUri)
            is ProfileCommand.OnLogoutClick -> logout()
        }
    }
}


@Stable
data class ProfileUiState(
    val user: User? = null,
    val avatarImage: String? = null,
    val isLoading: Boolean = true,
    val isProfileReady: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val userBadgeRes: Int = R.drawable.none_medal,
    val userLevel: String = "Let's try",
    val weeklyActivityDays: List<WeeklyActivityDay> = emptyList(),
    val testsCompletionPercent: Int = 0,
    val completedTestsCount: Int = 0
)

sealed class ProfileCommand {
    data class UploadAvatarImage(val imageUri: String) : ProfileCommand()
    object OnLogoutClick : ProfileCommand()
}