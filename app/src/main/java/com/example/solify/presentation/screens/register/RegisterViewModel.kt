package com.example.solify.presentation.screens.register

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.usecases.auth.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun processCommand(command: RegisterCommand) {
        when (command) {
            is RegisterCommand.OnNameChanged -> updateName(command.name)
            is RegisterCommand.OnSurnameChanged -> updateSurname(command.surname)
            is RegisterCommand.OnEmailChanged -> updateEmail(command.email)
            is RegisterCommand.OnPasswordChanged -> updatePassword(command.password)
            is RegisterCommand.OnRegisterClick -> register()
        }
    }

    private fun updateName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    private fun updateSurname(surname: String) {
        _uiState.update { it.copy(surname = surname, error = null) }
    }

    private fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    private fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    private fun register() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        if (currentState.surname.isBlank()) {
            _uiState.update { it.copy(error = "Surname is required") }
            return
        }
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(error = "Email is required") }
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(error = "Password is required") }
            return
        }
        if (currentState.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = registerUserUseCase(
                name = currentState.name,
                surname = currentState.surname,
                email = currentState.email,
                password = currentState.password
            )

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
                        error = error.message ?: "Registration failed"
                    )
                }
            }
        }
    }
}

@Stable
data class RegisterUiState(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: User? = null
)

sealed class RegisterCommand {
    data class OnNameChanged(val name: String) : RegisterCommand()
    data class OnSurnameChanged(val surname: String) : RegisterCommand()
    data class OnEmailChanged(val email: String) : RegisterCommand()
    data class OnPasswordChanged(val password: String) : RegisterCommand()
    object OnRegisterClick : RegisterCommand()
}