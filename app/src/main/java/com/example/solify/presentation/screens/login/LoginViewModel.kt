package com.example.solify.presentation.screens.login

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.usecases.auth.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun processCommand(command: LoginCommand) {
        when (command) {
            is LoginCommand.OnEmailChanged -> updateEmail(command.email)
            is LoginCommand.OnPasswordChanged -> updatePassword(command.password)
            is LoginCommand.OnLoginClick -> login()
        }
    }

    private fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    private fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    private fun login() {
        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(error = "Email is required") }
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(error = "Password is required") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = loginUserUseCase(currentState.email, currentState.password)

                result.onSuccess { user ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isSuccess = true,
                            user = user
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = error.message ?: "Login failed"
                        )
                    }
                }

        }
    }
}

@Stable
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: User? = null
)

sealed class LoginCommand {
    data class OnEmailChanged(val email: String) : LoginCommand()
    data class OnPasswordChanged(val password: String) : LoginCommand()
    object OnLoginClick : LoginCommand()
}