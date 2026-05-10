package com.example.solify.presentation.screens.edit_profile

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solify.domain.entities.user.User
import com.example.solify.domain.usecases.auth.DeleteAccountUseCase
import com.example.solify.domain.usecases.user.DeleteUserAvatarUseCase
import com.example.solify.domain.usecases.user.GetCurrentUserUseCase
import com.example.solify.domain.usecases.user.UpdateUserAvatarUseCase
import com.example.solify.domain.usecases.user.UpdateUserProfileUseCase
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
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    private val deleteUserAvatarUseCase: DeleteUserAvatarUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            getCurrentUserUseCase().onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        originalUser = user,
                        name = user.name,
                        surname = user.surname,
                        email = user.email,
                        avatarUrl = user.avatarUrl
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load user"
                    )
                }
            }
        }
    }

    fun processCommand(command: EditProfileCommand) {
        when (command) {
            is EditProfileCommand.OnNameChanged -> updateName(command.name)
            is EditProfileCommand.OnSurnameChanged -> updateSurname(command.surname)
            is EditProfileCommand.OnEmailChanged -> updateEmail(command.email)
            is EditProfileCommand.OnSaveClick -> saveChanges()
            is EditProfileCommand.OnUploadAvatar -> uploadAvatar(command.imageUri)
            is EditProfileCommand.OnDeleteAvatar -> deleteAvatar()
            is EditProfileCommand.OnDeleteAccountClick -> showDeleteAccountDialog()
            is EditProfileCommand.OnConfirmDeleteAccount -> deleteAccount(command.password)
            is EditProfileCommand.OnDismissDeleteDialog -> dismissDeleteDialog()
            is EditProfileCommand.OnResetError -> resetError()
            is EditProfileCommand.OnUpdateDeletePassword -> updateDeletePassword(command.password)
        }
    }

    private fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    private fun updateSurname(surname: String) {
        _uiState.update { it.copy(surname = surname, surnameError = null) }
    }

    private fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    private fun saveChanges() {
        val currentState = _uiState.value
        val originalUser = currentState.originalUser

        // Валидация
        var hasError = false

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }

        if (currentState.surname.isBlank()) {
            _uiState.update { it.copy(surnameError = "Surname is required") }
            hasError = true
        }

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required") }
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            hasError = true
        }

        if (hasError) return

        // Check changes
        val hasChanges = originalUser?.let {
            it.name != currentState.name ||
                    it.surname != currentState.surname ||
                    it.email != currentState.email
        } ?: false

        if (!hasChanges) {
            _uiState.update { it.copy(onNavigateBack = true) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = updateUserProfileUseCase(
                name = currentState.name,
                surname = currentState.surname,
                email = currentState.email
            )

            withContext(Dispatchers.Main) {
                result.onSuccess { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            onNavigateBack = true,
                            updatedUser = updatedUser
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to update profile"
                        )
                    }
                }
            }
        }
    }

    private fun uploadAvatar(imageUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = updateUserAvatarUseCase(imageUri)

            withContext(Dispatchers.Main) {
                result.onSuccess { avatarUrl ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            avatarUrl = avatarUrl,
                            originalUser = it.originalUser?.copy(avatarUrl = avatarUrl)
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to upload avatar"
                        )
                    }
                }
            }
        }
    }

    private fun deleteAvatar() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = deleteUserAvatarUseCase()

            withContext(Dispatchers.Main) {
                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            avatarUrl = null,
                            originalUser = it.originalUser?.copy(avatarUrl = null)
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete avatar"
                        )
                    }
                }
            }
        }
    }

    private fun showDeleteAccountDialog() {
        _uiState.update {
            it.copy(
                showDeleteAccountDialog = true,
                deletePassword = "",
                deletePasswordError = null
            )
        }
    }

    private fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteAccountDialog = false,
                deletePassword = "",
                deletePasswordError = null
            )
        }
    }

    private fun deleteAccount(password: String) {
        if (password.isBlank()) {
            _uiState.update { it.copy(deletePasswordError = "Password is required") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = deleteAccountUseCase(password)

            withContext(Dispatchers.Main) {
                result.onSuccess { success ->
                    if (success) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showDeleteAccountDialog = false,
                                accountDeleted = true
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                deletePasswordError = "Invalid password"
                            )
                        }
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete account"
                        )
                    }
                }
            }
        }
    }

    private fun resetError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun updateDeletePassword(password: String) {
        _uiState.update { it.copy(deletePassword = password, deletePasswordError = null) }
    }
}

@Stable
data class EditProfileUiState(
    val originalUser: User? = null,
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val onNavigateBack: Boolean = false,
    val updatedUser: User? = null,
    val accountDeleted: Boolean = false,

    // Validation errors
    val nameError: String? = null,
    val surnameError: String? = null,
    val emailError: String? = null,

    // Delete account dialog
    val showDeleteAccountDialog: Boolean = false,
    val deletePassword: String = "",
    val deletePasswordError: String? = null
)

sealed class EditProfileCommand {
    data class OnNameChanged(val name: String) : EditProfileCommand()
    data class OnSurnameChanged(val surname: String) : EditProfileCommand()
    data class OnEmailChanged(val email: String) : EditProfileCommand()
    object OnSaveClick : EditProfileCommand()
    data class OnUploadAvatar(val imageUri: String) : EditProfileCommand()
    object OnDeleteAvatar : EditProfileCommand()
    object OnDeleteAccountClick : EditProfileCommand()
    data class OnConfirmDeleteAccount(val password: String) : EditProfileCommand()
    object OnDismissDeleteDialog : EditProfileCommand()
    object OnResetError : EditProfileCommand()
    data class OnUpdateDeletePassword(val password: String) : EditProfileCommand()
}