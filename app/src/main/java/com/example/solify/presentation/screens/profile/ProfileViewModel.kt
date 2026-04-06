package com.example.solify.presentation.screens.profile

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.usecases.auth.LogoutUserUseCase
import com.example.solify.domain.usecases.auth.ObserveCurrentUserUseCase
import com.example.solify.domain.usecases.auth.RefreshCurrentUserUseCase
import com.example.solify.domain.usecases.user.UpdateUserAvatarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val refreshCurrentUserUseCase: RefreshCurrentUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {

        Log.d("ProfileScreen", "init")
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase(viewModelScope).collect { user ->
                Log.d("ProfileViewModel", "User updated: $user")
                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            refreshCurrentUserUseCase().onSuccess { user ->
                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false
                    )
                }
            }
        }

    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            is ProfileCommand.UploadAvatarImage -> uploadAvatarImage(command.imageUri)
            is ProfileCommand.OnLogoutClick -> logout()
            is ProfileCommand.ResetState -> resetState()
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

    private fun resetState() {
        _uiState.update { state ->
            state.copy(isLoggedOut = false)
        }
    }
}

@Stable
data class ProfileUiState(
    val user: User? = null,
    val avatarImage: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

sealed class ProfileCommand {
    data class UploadAvatarImage(val imageUri: String) : ProfileCommand()
    object OnLogoutClick : ProfileCommand()
    object ResetState : ProfileCommand()
}